package cases

import (
	"github.com/go-redis/redis/v7"
	"github.com/panjf2000/ants/v2"
	"github.com/tidwall/gjson"
	"os"
	"strconv"
	"sync"
	"testcase/commons"
	"testcase/compare"
	"testcase/generatedata"
	"testcase/synctaskhandle"
	"time"
	"context"
	"errors"
)

//基本测试案例单实例2单实例，无映射关系
func (tc *TestCase) Single2Single() {
	createjson := tc.ParseJsonFile(tc.CreateTaskFile)
	increment_pool, _ := ants.NewPool(tc.Increment_Threads)
	defer increment_pool.Release()

	saddr := gjson.Get(string(createjson), "sourceRedisAddress").String()
	taddr := gjson.Get(string(createjson), "targetRedisAddress").String()
	spasswd := gjson.Get(string(createjson), "sourcePassword").String()
	tpasswd := gjson.Get(string(createjson), "targetPassword").String()
	taskname := gjson.Get(string(createjson), "taskName").String()

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
			bo.KeepExecBasicOpt(ctx, time.Duration(tc.DataGenInterval)*time.Millisecond, false)
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
	compare := &compare.CompareSingle2Single{
		Source:         sclient,
		Target:         tclient,
		BatchSize:      tc.Compare_BatchSize,
		TTLDiff:        tc.Compare_TTLDiff,
		CompareThreads: tc.Compare_Threads,
	}

	compare.CompareDB()
}

//Single2SingleWithDBMap,基本测试案例单实例2单实例，有映射关系
func (tc TestCase) Single2SingleWithDBMap() {
	createjson := tc.ParseJsonFile(tc.CreateTaskFile)

	saddr := gjson.Get(string(createjson), "sourceRedisAddress").String()
	taddr := gjson.Get(string(createjson), "targetRedisAddress").String()
	spasswd := gjson.Get(string(createjson), "sourcePassword").String()
	tpasswd := gjson.Get(string(createjson), "targetPassword").String()
	taskname := gjson.Get(string(createjson), "taskName").String()
	dbmap := gjson.Get(string(createjson), "dbMapper").Map()

	increment_pool, err := ants.NewPool(len(dbmap))
	if err != nil {
		logger.Sugar().Error(err)
		return
	}

	defer increment_pool.Release()

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
	for k, _ := range dbmap {
		db, err := strconv.Atoi(k)
		if err != nil {
			logger.Sugar().Error(err)
			return
		}
		sopt.DB = db
		client := commons.GetGoRedisClient(sopt)
		defer client.Close()
		bgkv := generatedata.GenBigKV{
			KeySuffix:   commons.RandString(tc.BigKV_KeySuffix_Len),
			Loopstep:    tc.BigKV_Loopstep,
			EXPIRE:      time.Duration(tc.BigKV_EXPIRE) * time.Second,
			ValuePrefix: commons.RandString(tc.BigKV_ValuePrefix_Len),
			DB:          db,
		}
		bgkv.GenerateBaseDataParallel(client)
	}

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

	for k, _ := range dbmap {
		db, err := strconv.Atoi(k)
		if err != nil {
			logger.Sugar().Error(err)
			return
		}
		sopt.DB = db
		client := commons.GetGoRedisClient(sopt)
		defer client.Close()

		bo := &generatedata.OptSingle{
			RedisConn: client.Conn(),
			KeySuffix: commons.RandString(tc.Increment_KeySuffix_Len),
			Loopstep:  tc.Increment_Loopstep,
			EXPIRE:    time.Duration(tc.Increment_EXPIRE) * time.Second,
			DB:        db,
		}

		wg.Add(1)
		increment_pool.Submit(func() {
			bo.KeepExecBasicOpt(ctx, time.Duration(tc.DataGenInterval)*time.Millisecond, false)
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
	for k, v := range dbmap {
		sdb, err := strconv.Atoi(k)
		if err != nil {
			logger.Sugar().Error(err)
			return
		}

		tdb, err := strconv.Atoi(v.Raw)
		if err != nil {
			logger.Sugar().Error(err)
			return
		}

		sopt.DB = sdb
		topt.DB = tdb

		sclient := commons.GetGoRedisClient(sopt)
		tclient := commons.GetGoRedisClient(topt)
		defer sclient.Close()
		defer tclient.Close()

		compare := &compare.CompareSingle2Single{
			Source:         sclient,
			Target:         tclient,
			BatchSize:      tc.Compare_BatchSize,
			TTLDiff:        tc.Compare_TTLDiff,
			CompareThreads: tc.Compare_Threads,
			SourceDB:       sdb,
			TargetDB:       tdb,
		}

		compare.CompareDB()
	}
}
