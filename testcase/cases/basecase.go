package cases

import (
	"context"
	"errors"
	"github.com/go-redis/redis/v7"
	"github.com/panjf2000/ants/v2"
	"github.com/tidwall/gjson"
	"go.uber.org/zap"
	"gopkg.in/yaml.v2"
	"io/ioutil"
	"os"
	"runtime"
	"strconv"
	"strings"
	"sync"
	"testcase/commons"
	"testcase/compare"
	"testcase/generatedata"
	"testcase/globalzap"
	"testcase/synctaskhandle"
	"time"
)

var logger = globalzap.GetLogger() //logger.Println(err)

type CaseType int32

const (
	Case_Single2Single = iota
	Case_Single2SingleWithDBMap
	Case_Single2Cluster
	Case_Cluster2Single
)

var CaseTypeMap = map[int32]string{
	Case_Single2Single:          "Single2Single",
	Case_Single2SingleWithDBMap: "Single2SingleWithDBMap",
	Case_Single2Cluster:         "Single2Cluster",
}

func (ct CaseType) String() string {
	switch ct {
	case Case_Single2Single:
		return "Single2Single"
	case Case_Single2SingleWithDBMap:
		return "Single2SingleWithDBMap"
	case Case_Single2Cluster:
		return "Single2Cluster"
	default:
		return ""
	}
}

type TestCase struct {
	SyncServer              string   `yaml:"syncserver"`            //redissyncer server address
	CreateTaskFile          string   `yaml:"createtaskfile"`        //任务创建json文件路径
	GenDataDuration         int      `yaml:"gendataduration"`       //持续产生增量数据的时间,单位为秒
	DataGenInterval         int64    `yaml:"datageninterval"`       //线程内数据生成间隔，单位为毫秒
	GenDataThreads          int      `yaml:"gendatathreads"`        //持续生成数据的线程数量
	BigKV_KeySuffix_Len     int      `yaml:"bigkvkeysuffixlen"`     //大key后缀位数，按位数生成key后缀
	BigKV_Loopstep          int      `yaml:"bigkvloopstep"`         //大key循环次数，该参数决定大key value的长度
	BigKV_EXPIRE            int      `yaml:"bigkvexpire"`           //大key过期时间，单位为秒
	BigKV_ValuePrefix_Len   int      `yaml:"bigkvvalueprefixlen"`   //大key value前缀长度，按长度生成值的前缀
	Increment_KeySuffix_Len int      `yaml:"incrementkeysuffixlen"` //增量数据key后缀位数，按位生成key后缀
	Increment_Loopstep      int      `yaml:"incrementloopstep"`     //增量数据循环长度，影响增量数据value长度或操作次数
	Increment_EXPIRE        int      `yaml:"incrementexpire"`       //增量数据过期时间，单位为秒
	Increment_Threads       int      `yaml:"incrementthreads"`      //生成增量数据的线程数量
	Compare_BatchSize       int64    `yaml:"comparebatchsize"`      //比较List、Set、Zset类型时的每批次值的数量
	Compare_Threads         int      `yaml:"comparethreads"`        //比较db线程数量
	Compare_TTLDiff         float64  `yaml:"comparettldiff"`        //TTL最小差值
	CaseType                CaseType `yaml:"casetype"`              //案例类型编号，可以通过 listcases子命令查询对应的case编号
}

func NewTestCase() TestCase {
	tc := TestCase{
		SyncServer:              "127.0.0.1:8080",
		GenDataDuration:         60,
		DataGenInterval:         int64(300),
		GenDataThreads:          runtime.NumCPU(),
		BigKV_KeySuffix_Len:     4,
		BigKV_Loopstep:          20,
		BigKV_EXPIRE:            3600,
		BigKV_ValuePrefix_Len:   512,
		Increment_KeySuffix_Len: 4,
		Increment_Loopstep:      20,
		Increment_EXPIRE:        1800,
		Increment_Threads:       runtime.NumCPU(),
		Compare_BatchSize:       int64(50),
		Compare_Threads:         runtime.NumCPU(),
		Compare_TTLDiff:         float64(100000),
		CaseType:                Case_Single2Single,
	}

	return tc
}

//解析yaml文件获取testcase
func (tc *TestCase) ParseYamlFile(filepath string) error {
	yamlFile, err := ioutil.ReadFile(filepath)
	if err != nil {
		logger.Sugar().Error(err)
		return err
	}
	err = yaml.Unmarshal(yamlFile, tc)
	if err != nil {
		logger.Sugar().Error(err)
		return err
	}
	return nil
}

//解析同步任务的jsonfile
func (tc *TestCase) ParseJsonFile(casefile string) []byte {

	jsonFile, err := os.Open(casefile)
	defer jsonFile.Close()

	if err != nil {
		//logger.Println(err)
		logger.Info(err.Error())
		os.Exit(1)
	}

	jsonbytes, err := ioutil.ReadAll(jsonFile)
	if err != nil {
		//logger.Println(err)
		logger.Info(err.Error())
		os.Exit(1)
	}
	return jsonbytes
}

//验证任务状态是否可以关闭，并保证数据同步完成
func (tc *TestCase) CheckSyncTaskStatus(taskids []string) {
	//查看任务状态，直到COMMANDRUNING状态
	logger.Sugar().Info("Check task status begin...")
	for {
		iscommandrunning := true
		statusmap, err := synctaskhandle.GetTaskStatus(tc.SyncServer, taskids)
		if err != nil {
			logger.Sugar().Error(err)
			os.Exit(1)
		}

		for k, v := range statusmap {
			if v != "COMMANDRUNING" {
				iscommandrunning = false
			}
			if v == "BROKEN" || v == "STOP" {
				logger.Error("sync task broken! ", zap.String("taskid", k), zap.String("task_status", v))
				os.Exit(1)
			}
		}

		if iscommandrunning {
			break
		}
		time.Sleep(3 * time.Second)
	}

	time.Sleep(4 * time.Minute)
}

func (tc *TestCase) Exec() {
	switch tc.CaseType.String() {
	case "Single2Single":
		tc.Single2Single()
	case "Single2SingleWithDBMap":
		tc.Single2SingleWithDBMap()
	case "Single2Cluster":
		tc.Single2Cluster()
	default:
		logger.Sugar().Info("Nothing to be executed")
		return
	}
}

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

	//生成垫底数据
	bgkv := generatedata.GenBigKV{
		KeySuffix:   commons.RandString(tc.BigKV_KeySuffix_Len),
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
			bo.KeepExecBasicOpt(ctx, time.Duration(tc.DataGenInterval)*time.Millisecond)
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
			bo.KeepExecBasicOpt(ctx, time.Duration(tc.DataGenInterval)*time.Millisecond)
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
			bo.KeepExecBasicOpt(ctx, time.Duration(tc.DataGenInterval)*time.Millisecond)
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
