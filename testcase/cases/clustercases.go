package cases

import (
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
	"github.com/pkg/errors"
	"context"
)

//基本测试案例单实例2Cluster，无映射关系
func (tc *TestCase) Single2Cluster() {
	createjson := tc.ParseJsonFile(tc.CreateTaskFile)
	increment_pool, _ := ants.NewPool(tc.Increment_Threads)
	defer increment_pool.Release()

	saddr := gjson.Get(string(createjson), "sourceRedisAddress").String()
	taddrs := gjson.Get(string(createjson), "targetRedisAddress").String()
	spasswd := gjson.Get(string(createjson), "sourcePassword").String()
	tpasswd := gjson.Get(string(createjson), "targetPassword").String()
	taskname := gjson.Get(string(createjson), "taskName").String()

	taddrsarray := strings.Split(taddrs, ";")

	sopt := &redis.Options{
		Addr: saddr,
		DB:   0, // use default DB
	}

	if spasswd != "" {
		sopt.Password = spasswd
	}
	sclient := commons.GetGoRedisClient(sopt)

	topt := &redis.ClusterOptions{
		Addrs: taddrsarray,
	}

	if tpasswd != "" {
		topt.Password = tpasswd
	}

	tclient := redis.NewClusterClient(topt)

	defer sclient.Close()
	defer tclient.Close()

	//check redis 连通性
	if !commons.CheckRedisClientConnect(sclient) {
		logger.Sugar().Error(errors.New("Cannot connect source redis"))
		os.Exit(1)
	}
	if !commons.CheckRedisClusterClientConnect(tclient) {
		logger.Sugar().Error(errors.New("Cannot connect target redis"))
		os.Exit(1)
	}

	//check redissycner-server 是否可用

	//清理redis
	sclient.FlushAll()
	tclient.FlushAll()

	for _, v := range taddrsarray {
		opt := &redis.Options{
			Addr: v,
		}

		if tpasswd != "" {
			opt.Password = tpasswd
		}

		client := redis.NewClient(opt)
		defer client.Close()
		client.FlushAll()

	}

	//生成垫底数据
	bgkv := generatedata.GenBigKV{
		KeySuffix:
		commons.RandString(tc.BigKV_KeySuffix_Len),
		Loopstep:    tc.BigKV_Loopstep,
		EXPIRE:      time.Duration(tc.BigKV_EXPIRE) * time.Second,
		ValuePrefix: commons.RandString(tc.BigKV_ValuePrefix_Len),
	}
	bgkv.GenerateBaseDataParallel(sclient)

	//清理任务
	logger.Sugar().Info("Clean Task beging...")
	synctaskhandle.RemoveTaskByName(tc.SyncServer, taskname)
	logger.Sugar().Info("Clean Task end")

	//创建任务
	logger.Sugar().Info("Create Task beging...")
	taskids := synctaskhandle.CreateTask(tc.SyncServer, string(createjson))
	logger.Sugar().Info("Task Id is: ", taskids)

	//启动任务
	for _, v := range taskids {
		synctaskhandle.StartTask(tc.SyncServer, v)
	}

	logger.Sugar().Info("Create Task end")

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
			bo.KeepExecBasicOpt(ctx, time.Duration(tc.DataGenInterval)*time.Millisecond, true)
			wg.Done()
		})
	}
	wg.Wait()

	//查看任务状态，直到COMMANDRUNING状态
	tc.CheckSyncTaskStatus(taskids)
	logger.Sugar().Info("Check task status end")

	//停止任务
	synctaskhandle.StopTaskByIds(tc.SyncServer, taskids)

	//数据校验
	compare := &compare.CompareSingle2Cluster{
		Source:         sclient,
		Target:         tclient,
		BatchSize:      tc.Compare_BatchSize,
		TTLDiff:        tc.Compare_TTLDiff,
		CompareThreads: tc.Compare_Threads,
	}

	compare.CompareDB()
}

//基本测试案例Cluster2Cluster，无映射关系
func (tc *TestCase) Cluster2Cluster() {
	createjson := tc.ParseJsonFile(tc.CreateTaskFile)
	increment_pool, _ := ants.NewPool(tc.Increment_Threads)
	defer increment_pool.Release()

	saddrs := gjson.Get(string(createjson), "sourceRedisAddress").String()
	taddrs := gjson.Get(string(createjson), "targetRedisAddress").String()
	spasswd := gjson.Get(string(createjson), "sourcePassword").String()
	tpasswd := gjson.Get(string(createjson), "targetPassword").String()
	taskname := gjson.Get(string(createjson), "taskName").String()

	saddrsarray := strings.Split(saddrs, ";")
	taddrsarray := strings.Split(taddrs, ";")

	sopt := &redis.ClusterOptions{
		Addrs: taddrsarray,
	}

	if spasswd != "" {
		sopt.Password = spasswd
	}

	sclient := redis.NewClusterClient(sopt)

	topt := &redis.ClusterOptions{
		Addrs: taddrsarray,
	}

	if tpasswd != "" {
		topt.Password = tpasswd
	}

	tclient := redis.NewClusterClient(topt)

	defer sclient.Close()
	defer tclient.Close()

	//check redis 连通性
	if !commons.CheckRedisClusterClientConnect(sclient) {
		logger.Sugar().Error(errors.New("Cannot connect source redis"))
		os.Exit(1)
	}
	if !commons.CheckRedisClusterClientConnect(tclient) {
		logger.Sugar().Error(errors.New("Cannot connect target redis"))
		os.Exit(1)
	}

	//check redissycner-server 是否可用

	//清理redis
	for _, v := range saddrsarray {
		opt := &redis.Options{
			Addr: v,
		}

		if tpasswd != "" {
			opt.Password = tpasswd
		}

		client := redis.NewClient(opt)
		defer client.Close()
		client.FlushAll()

	}

	for _, v := range taddrsarray {
		opt := &redis.Options{
			Addr: v,
		}

		if tpasswd != "" {
			opt.Password = tpasswd
		}

		client := redis.NewClient(opt)
		defer client.Close()
		client.FlushAll()

	}

	//生成垫底数据
	bgkv := generatedata.GenBigKVCluster{
		RedisClusterClient: sclient,
		KeySuffix:          commons.RandString(tc.BigKV_KeySuffix_Len),
		Loopstep:           tc.BigKV_Loopstep,
		EXPIRE:             time.Duration(tc.BigKV_EXPIRE) * time.Second,
		ValuePrefix:        commons.RandString(tc.BigKV_ValuePrefix_Len),
	}
	bgkv.GenerateBaseDataParallelCluster()

	//清理任务
	logger.Sugar().Info("Clean Task beging...")
	synctaskhandle.RemoveTaskByName(tc.SyncServer, taskname)
	logger.Sugar().Info("Clean Task end")

	//创建任务
	logger.Sugar().Info("Create Task beging...")
	taskids := synctaskhandle.CreateTask(tc.SyncServer, string(createjson))
	logger.Sugar().Info("Task Id is: ", taskids)

	//启动任务
	for _, v := range taskids {
		synctaskhandle.StartTask(tc.SyncServer, v)
	}

	logger.Sugar().Info("Create Task end")

	//生成增量数据
	d := time.Now().Add(time.Duration(tc.GenDataDuration) * time.Second)
	ctx, cancel := context.WithDeadline(context.Background(), d)
	defer cancel()

	wg := sync.WaitGroup{}

	for i := 0; i < tc.GenDataThreads; i++ {
		bo := &generatedata.OptCluster{
			ClusterClient: sclient,
			KeySuffix:     commons.RandString(tc.Increment_KeySuffix_Len),
			Loopstep:      tc.Increment_Loopstep,
			EXPIRE:        time.Duration(tc.Increment_EXPIRE) * time.Second,
		}
		wg.Add(1)
		increment_pool.Submit(func() {
			bo.KeepExecBasicOptCluster(ctx, time.Duration(tc.DataGenInterval)*time.Millisecond)
			wg.Done()
		})
	}
	wg.Wait()

	//查看任务状态，直到COMMANDRUNING状态
	tc.CheckSyncTaskStatus(taskids)
	logger.Sugar().Info("Check task status end")

	//停止任务
	synctaskhandle.StopTaskByIds(tc.SyncServer, taskids)

	//数据校验
	for _, v := range saddrsarray {
		opt := &redis.Options{
			Addr: v,
		}

		if tpasswd != "" {
			opt.Password = tpasswd
		}

		client := redis.NewClient(opt)
		defer client.Close()

		compare := &compare.CompareSingle2Cluster{
			Source:         client,
			Target:         tclient,
			BatchSize:      tc.Compare_BatchSize,
			TTLDiff:        tc.Compare_TTLDiff,
			CompareThreads: tc.Compare_Threads,
		}
		compare.CompareDB()

	}

}
