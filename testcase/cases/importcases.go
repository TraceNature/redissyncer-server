package cases

import (
	"fmt"
	"github.com/go-redis/redis/v7"
	"github.com/panjf2000/ants/v2"
	"github.com/tidwall/gjson"
	"os"
	"strings"
	"sync"
	"testcase/commons"
	"testcase/compare"
	"testcase/generatedata"
	"testcase/synctaskhandle"
	"time"
	"context"
	"errors"
)

//rdb文件导入功能测试用例
func (tc *TestCase) ImportRdb2Single() {
	createjson := tc.ParseJsonFile(tc.CreateTaskFile)
	increment_pool, _ := ants.NewPool(tc.Increment_Threads)
	defer increment_pool.Release()

	saddr := tc.GenRdbRedis
	spasswd := tc.GenRdbRedisPassword
	taddr := gjson.Get(string(createjson), "targetRedisAddress").String()
	tpasswd := gjson.Get(string(createjson), "targetPassword").String()
	taskname := gjson.Get(string(createjson), "taskName").String()
	//fileaddr := gjson.Get(string(createjson), "fileAddress").String()

	sopt := &redis.Options{
		Addr: saddr,
		DB:   0, // use default DB
	}

	if spasswd != "" {
		sopt.Password = spasswd
	}
	sclient := commons.GetGoRedisClient(sopt)

	topt := &redis.Options{
		Addr: taddr,
		DB:   0, // use default DB
	}

	if tpasswd != "" {
		topt.Password = tpasswd
	}

	tclient := commons.GetGoRedisClient(topt)

	defer sclient.Close()
	defer tclient.Close()

	//check redis 连通性
	if !commons.CheckRedisClientConnect(sclient) {
		logger.Sugar().Error(errors.New("Cannot connect source redis"))
		os.Exit(1)
	}
	if !commons.CheckRedisClientConnect(tclient) {
		logger.Sugar().Error(errors.New("Cannot connect target redis"))
		os.Exit(1)
	}

	//check redissycner-server 是否可用

	//清理redis
	sclient.FlushAll()
	tclient.FlushAll()

	//生成垫底数据
	bgkv := generatedata.GenBigKV{
		KeySuffix:   commons.RandString(tc.BigKV_KeySuffix_Len),
		Loopstep:    tc.BigKV_Loopstep,
		EXPIRE:      time.Duration(tc.BigKV_EXPIRE) * time.Second,
		ValuePrefix: commons.RandString(tc.BigKV_ValuePrefix_Len),
	}
	bgkv.GenerateBaseDataParallel(sclient)

	//生成增量数据
	d := time.Now().Add(time.Duration(tc.GenDataDuration) * time.Second)
	ctx, cancel := context.WithDeadline(context.Background(), d)
	defer cancel()

	wg := sync.WaitGroup{}

	for i := 0; i < tc.GenDataThreads; i++ {
		bo := &generatedata.OptSingle{
			RedisConn: sclient.Conn(),
			KeySuffix: commons.RandString(tc.Increment_KeySuffix_Len),
			Loopstep:  tc.Increment_Loopstep,
			EXPIRE:    time.Duration(tc.Increment_EXPIRE) * time.Second,
		}

		wg.Add(1)
		increment_pool.Submit(func() {
			bo.KeepExecBasicOpt(ctx, time.Duration(tc.DataGenInterval)*time.Millisecond, false)
			wg.Done()
		})
	}
	wg.Wait()

	//生成rdb文件
	sclient.Save()
	time.Sleep(10 * time.Second)

	//复制rdb文件到redissyncer所在服务器指定目录
	syncserverip := strings.Split(strings.Split(tc.SyncServer, "//")[1], ":")[0]
	sshclient, err := commons.GenSshClient(tc.SyncServerOsUser, tc.SyncServerOsUserPassword, syncserverip+tc.SyncServerSshPort)
	if err != nil {
		logger.Sugar().Error(err)
		return
	}
	defer sshclient.Close()
	session, err := sshclient.NewSession()
	if err != nil {
		logger.Sugar().Error(err)
		return
	}
	defer session.Close()

	rdbip := strings.Split(tc.GenRdbRedis, ":")[0]
	sshcmd := "ssh-keyscan " + rdbip + " >> ~/.ssh/known_hosts;" +
		"sshpass -p " + tc.GenRdbRedisOsUserPassword + " scp " + tc.GenRdbRedisOsUser + "@" + rdbip + ":" + tc.DumpFilePath + " " + tc.SyncOsFilePath + ";"
	fmt.Println(sshcmd)

	cprdbtosyncserver, err := session.CombinedOutput(sshcmd)
	if err != nil {
		logger.Sugar().Error(err)
		return
	}
	logger.Sugar().Info(string(cprdbtosyncserver))

	//清理任务
	logger.Sugar().Info("Clean Task beging...")
	synctaskhandle.RemoveTaskByName(tc.SyncServer, taskname)
	logger.Sugar().Info("Clean Task end")

	//创建任务
	logger.Sugar().Info("Create Task beging...")
	taskids := synctaskhandle.Import(tc.SyncServer, string(createjson))
	logger.Sugar().Info("Task Id is: ", taskids)

	//启动任务
	for _, v := range taskids {
		synctaskhandle.StartTask(tc.SyncServer, v)
	}

	logger.Sugar().Info("Create Task end")

	//查看任务状态，直到COMMANDRUNING状态
	tc.CheckSyncTaskStatus(taskids)
	logger.Sugar().Info("Check task status end")

	//停止任务
	synctaskhandle.StopTaskByIds(tc.SyncServer, taskids)

	//数据校验
	compare := &compare.CompareSingle2Single{
		Source:         sclient,
		Target:         tclient,
		BatchSize:      tc.Compare_BatchSize,
		TTLDiff:        tc.Compare_TTLDiff,
		CompareThreads: tc.Compare_Threads,
	}

	compare.CompareDB()
}

//Aof文件导入功能测试用例
func (tc *TestCase) ImportAof2Single() {
	createjson := tc.ParseJsonFile(tc.CreateTaskFile)
	increment_pool, _ := ants.NewPool(tc.Increment_Threads)
	defer increment_pool.Release()

	saddr := tc.GenRdbRedis
	spasswd := tc.GenRdbRedisPassword
	taddr := gjson.Get(string(createjson), "targetRedisAddress").String()
	tpasswd := gjson.Get(string(createjson), "targetPassword").String()
	taskname := gjson.Get(string(createjson), "taskName").String()
	//fileaddr := gjson.Get(string(createjson), "fileAddress").String()

	sopt := &redis.Options{
		Addr: saddr,
		DB:   0, // use default DB
	}

	if spasswd != "" {
		sopt.Password = spasswd
	}
	sclient := commons.GetGoRedisClient(sopt)

	topt := &redis.Options{
		Addr: taddr,
		DB:   0, // use default DB
	}

	if tpasswd != "" {
		topt.Password = tpasswd
	}

	tclient := commons.GetGoRedisClient(topt)

	defer sclient.Close()
	defer tclient.Close()

	//check redis 连通性
	if !commons.CheckRedisClientConnect(sclient) {
		logger.Sugar().Error(errors.New("Cannot connect source redis"))
		os.Exit(1)
	}
	if !commons.CheckRedisClientConnect(tclient) {
		logger.Sugar().Error(errors.New("Cannot connect target redis"))
		os.Exit(1)
	}

	//check redissycner-server 是否可用

	//清理redis
	sclient.FlushAll()
	tclient.FlushAll()

	//清理任务
	logger.Sugar().Info("Clean Task beging...")
	synctaskhandle.RemoveTaskByName(tc.SyncServer, taskname)
	logger.Sugar().Info("Clean Task end")

	//生成垫底数据
	bgkv := generatedata.GenBigKV{
		KeySuffix:   commons.RandString(tc.BigKV_KeySuffix_Len),
		Loopstep:    tc.BigKV_Loopstep,
		EXPIRE:      time.Duration(tc.BigKV_EXPIRE) * time.Second,
		ValuePrefix: commons.RandString(tc.BigKV_ValuePrefix_Len),
	}
	bgkv.GenerateBaseDataParallel(sclient)

	//生成增量数据
	d := time.Now().Add(time.Duration(tc.GenDataDuration) * time.Second)
	ctx, cancel := context.WithDeadline(context.Background(), d)
	defer cancel()

	wg := sync.WaitGroup{}

	for i := 0; i < tc.GenDataThreads; i++ {
		bo := &generatedata.OptSingle{
			RedisConn: sclient.Conn(),
			KeySuffix: commons.RandString(tc.Increment_KeySuffix_Len),
			Loopstep:  tc.Increment_Loopstep,
			EXPIRE:    time.Duration(tc.Increment_EXPIRE) * time.Second,
		}

		wg.Add(1)
		increment_pool.Submit(func() {
			bo.KeepExecBasicOpt(ctx, time.Duration(tc.DataGenInterval)*time.Millisecond, false)
			wg.Done()
		})
	}
	wg.Wait()
	time.Sleep(10 * time.Second)

	//复制aof文件到redissyncer所在服务器指定目录
	syncserverip := strings.Split(strings.Split(tc.SyncServer, "//")[1], ":")[0]
	sshclient, err := commons.GenSshClient(tc.SyncServerOsUser, tc.SyncServerOsUserPassword, syncserverip+tc.SyncServerSshPort)
	if err != nil {
		logger.Sugar().Error(err)
		return
	}
	defer sshclient.Close()
	session, err := sshclient.NewSession()
	if err != nil {
		logger.Sugar().Error(err)
		return
	}
	defer session.Close()

	rdbip := strings.Split(tc.GenRdbRedis, ":")[0]
	sshcmd := "ssh-keyscan " + rdbip + " >> ~/.ssh/known_hosts;" +
		"cd " + tc.SyncOsFilePath + ";" + "rm -fr *.aof ;" +
		"sshpass -p " + tc.GenRdbRedisOsUserPassword + " scp " + tc.GenRdbRedisOsUser + "@" + rdbip + ":" + tc.DumpFilePath + " " + tc.SyncOsFilePath + ";"
	fmt.Println(sshcmd)

	cprdbtosyncserver, err := session.CombinedOutput(sshcmd)

	if err != nil {
		logger.Sugar().Error(err)
		return
	}
	logger.Sugar().Info(string(cprdbtosyncserver))

	//创建任务
	logger.Sugar().Info("Create Task beging...")
	taskids := synctaskhandle.Import(tc.SyncServer, string(createjson))
	logger.Sugar().Info("Task Id is: ", taskids)

	//启动任务
	for _, v := range taskids {
		synctaskhandle.StartTask(tc.SyncServer, v)
	}

	logger.Sugar().Info("Create Task end")

	//查看任务状态，直到COMMANDRUNING状态
	tc.CheckSyncTaskStatus(taskids)
	logger.Sugar().Info("Check task status end")

	//停止任务
	synctaskhandle.StopTaskByIds(tc.SyncServer, taskids)

	//数据校验
	compare := &compare.CompareSingle2Single{
		Source:         sclient,
		Target:         tclient,
		BatchSize:      tc.Compare_BatchSize,
		TTLDiff:        tc.Compare_TTLDiff,
		CompareThreads: tc.Compare_Threads,
	}

	compare.CompareDB()
}

func (tc *TestCase) ImportRdb2Cluster() {

}

func (tc *TestCase) ImportAof2Cluster() {}
