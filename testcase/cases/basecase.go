package cases

import (
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"github.com/go-redis/redis/v7"
	"github.com/panjf2000/ants/v2"
	"github.com/tidwall/gjson"
	"io/ioutil"
	"os"
	"runtime"
	"strings"
	"sync"
	"testcase/compare"
	"testcase/commons"
	"testcase/generatedata"
	"testcase/globalzap"
	"testcase/synctaskhandle"
	"time"
)

var logger = globalzap.GetLogger() //logger.Println(err)

type CaseType int32

const (
	Case_Single2Single = iota
)

var CaseTypeArray = []CaseType{
	Case_Single2Single,
}

func (ct CaseType) String() string {
	switch ct {
	case Case_Single2Single:
		return "Single2Single"
	default:
		return "UNKNOWN"
	}
}

type TestCase struct {
	SyncServer      string `yaml:"syncserver"`      //redissyncer server address
	CreateTaskFile  string `yaml:"createtaskfile"`  //任务创建json文件路径
	GenDataDuration int    `yaml:"gendataduration"` //持续产生增量数据的时间,单位为秒
	DataGenInterval int64  `yaml:"datageninterval"` //线程内数据生成间隔，单位为毫秒
	GenDataThreads  int    `yaml:"gendatathreads"`  //持续生成数据的线程数量
	CaseType        CaseType
}

//解析同步任务的jsonfile
func (tt *TestCase) ParseJsonFile() []byte {
	jsonFile, err := os.Open(tt.CreateTaskFile)
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

//基本测试案例单实例2单实例，无映射关系
func (tc *TestCase) Single2Single() {

	createjson := tc.ParseJsonFile()

	threads := tc.GenDataThreads

	if threads <= 0 {
		threads = runtime.NumCPU()
	}

	p, _ := ants.NewPool(10000)

	defer p.Release()

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
	if !commons.CheckRedisConnect(sclient) {
		logger.Sugar().Error(errors.New("Cannot connect source redis"))
		os.Exit(1)
	}
	if !commons.CheckRedisConnect(tclient) {
		logger.Sugar().Error(errors.New("Cannot connect source redis"))
		os.Exit(1)
	}

	//check redissycner-server 是否可用

	//清理redis
	sclient.FlushAll()
	tclient.FlushAll()

	//生成垫底数据

	bgkv := generatedata.GenBigKV{
		KeySuffix:   commons.RandString(4),
		Loopstep:    10,
		EXPIRE:      3600 * time.Second,
		ValuePrefix: commons.RandString(256 * 1024),
	}
	bgkv.GenerateBaseDataParallel(sclient)

	//清理任务
	logger.Sugar().Info("Clean Task beging...")
	synctaskhandle.RemoveTaskByName(tc.SyncServer, taskname)
	logger.Sugar().Info("Clean Task end")

	//创建任务
	logger.Sugar().Info("Create Task beging...")
	taskids := synctaskhandle.CreateTask(tc.SyncServer, string(createjson))
	logger.Sugar().Info("Create Task end")

	//启动任务
	for _, v := range taskids {
		synctaskhandle.StartTask(tc.SyncServer, v)
	}

	//生成增量数据
	d := time.Now().Add(time.Duration(tc.GenDataDuration) * time.Second)
	ctx, cancel := context.WithDeadline(context.Background(), d)
	defer cancel()

	wg := sync.WaitGroup{}

	for i := 0; i < threads; i++ {
		bo := &generatedata.BaseOpt{
			RedisConn: sclient.Conn(),
			KeySuffix: commons.RandString(4),
			Loopstep:  20,
			EXPIRE:    600 * time.Second,
		}
		wg.Add(1)
		go func() {
			bo.KeepExecBasicOpt(ctx, time.Duration(tc.DataGenInterval)*time.Millisecond)
			wg.Done()
		}()
	}
	wg.Wait()

	//查看任务状态，直到COMMANDRUNING状态
	logger.Sugar().Info("Check task status begin...")
	for {
		iscommandrunning := true
		statusmap, err := synctaskhandle.GetTaskStatus(tc.SyncServer, taskids)
		if err != nil {
			logger.Sugar().Error(err)
			os.Exit(1)
		}

		for _, v := range statusmap {
			if v != "COMMANDRUNING" {
				iscommandrunning = false
			}
		}

		if iscommandrunning {
			break
		}
		time.Sleep(3 * time.Second)
	}

	time.Sleep(5 * time.Minute)

	logger.Sugar().Info("Check task status end")

	//停止任务
	synctaskhandle.StopTaskByIds(tc.SyncServer, taskids)

	//数据校验
	compare := &compare.Compare{
		Source:    sclient,
		Target:    tclient,
		BatchSize: int64(50),
	}

	compare.CompareDB()
}

func BaseCase(duration time.Duration, genreport bool, createtaskfile string, loopstep int64, syncserver string) {
	fmt.Println("Executing BaseCase ...")

	report := &commons.Report{ReportContent: make(map[string]interface{})}
	//解析创建任务文件
	jsonFile, err := os.Open(createtaskfile)
	defer jsonFile.Close()

	if err != nil {
		//logger.Println(err)
		logger.Info(err.Error())
		os.Exit(1)
	}

	jsonbytes, _ := ioutil.ReadAll(jsonFile)
	saddr := gjson.Get(string(jsonbytes), "sourceRedisAddress").String()
	taddr := gjson.Get(string(jsonbytes), "targetRedisAddress").String()
	spasswd := gjson.Get(string(jsonbytes), "sourcePassword").String()
	tpasswd := gjson.Get(string(jsonbytes), "targetPassword").String()
	taskname := gjson.Get(string(jsonbytes), "taskName").String()

	sopt := &redis.Options{
		Addr: saddr,
		DB:   0, // use default DB
	}

	topt := &redis.Options{
		Addr: taddr,
		DB:   0, // use default DB
	}
	if spasswd != "" {
		sopt.Password = spasswd
	}

	if tpasswd != "" {
		topt.Password = tpasswd
	}

	sclient := commons.GetGoRedisClient(sopt)
	tclient := commons.GetGoRedisClient(topt)

	//校验连通性
	_, serr := sclient.Ping().Result()
	_, terr := tclient.Ping().Result()

	if serr != nil {
		logger.Error(serr.Error())
		if genreport {
			report.ReportContent["Check source error"] = serr
			report.JsonToFile()
		}
		os.Exit(1)
	}

	if terr != nil {
		logger.Info(terr.Error())
		if genreport {
			report.ReportContent["Check source error"] = terr
			report.JsonToFile()
		}
		os.Exit(1)
	}

	//flushdb
	sclient.FlushAll()
	tclient.FlushAll()

	//生成基础数据
	//d, _ := cmd.Flags().GetInt64("basedatasize")
	generatedata.GenerateBase(sclient, loopstep)

	//查看同名任务是否存在
	listjsonmap := make(map[string]interface{})
	listjsonmap["regulation"] = "bynames"
	listjsonmap["tasknames"] = strings.Split(taskname, ",")
	listjsonStr, err := json.Marshal(listjsonmap)
	if err != nil {
		logger.Info(err.Error())
		os.Exit(1)
	}

	listtaskreq := &synctaskhandle.Request{
		Server: syncserver,
		Api:    synctaskhandle.ListTasksPath,
		Body:   string(listjsonStr),
	}
	listresp := listtaskreq.ExecRequest()

	tasklist := gjson.Get(listresp, "data").Array()
	existstaskids := []string{}

	if len(tasklist) > 0 {
		for _, v := range tasklist {
			existstaskids = append(existstaskids, gjson.Get(v.String(), "taskId").String())
		}
		//停止任务
		stopexitsmap := make(map[string]interface{})
		stopexitsmap["taskids"] = existstaskids
		stopexistsjson, _ := json.MarshalIndent(stopexitsmap, "", " ")
		stopexistsreq := &synctaskhandle.Request{
			Server: syncserver,
			Api:    synctaskhandle.StopTaskPath,
			Body:   string(stopexistsjson),
		}
		stopexistsreq.ExecRequest()

		//删除任务
		removeexistsreq := &synctaskhandle.Request{
			Server: syncserver,
			Api:    synctaskhandle.RemoveTaskPath,
			Body:   string(stopexistsjson),
		}

		stopresult := removeexistsreq.ExecRequest()

		fmt.Println(stopresult)

	}

	//创建任务
	createreq := &synctaskhandle.Request{
		Server: syncserver,
		Api:    synctaskhandle.CreateTaskPath,
		Body:   string(jsonbytes),
	}

	resp := createreq.ExecRequest()
	taskids := gjson.Get(resp, "data.taskids").Array()

	taskidsstrarray := []string{}
	for _, v := range taskids {
		taskidsstrarray = append(taskidsstrarray, v.String())
	}

	//校验任务是否启动，若没有启动则启动任务
	taskerrors := gjson.Get(resp, "data.errors").Array()
	if len(taskerrors) != 0 {
		//logger.Println("task create faile:", resp)
		logger.Sugar().Info("task create faile:", resp)
		os.Exit(1)
	}

	//生成增量数据
	i := int64(10)
	for loops := int64(0); loops < i; loops++ {
		generatedata.GenerateIncrement(sclient)
	}

	time.Sleep(5 * time.Second)

	//停止任务
	stopmap := make(map[string]interface{})
	stopmap["taskids"] = taskidsstrarray
	stopjson, _ := json.MarshalIndent(stopmap, "", " ")
	stopreq := &synctaskhandle.Request{
		Server: syncserver,
		Api:    synctaskhandle.StopTaskPath,
		Body:   string(stopjson),
	}
	stopresult := stopreq.ExecRequest()
	fmt.Println(stopresult)
	time.Sleep(5 * time.Second)

	//删除任务
	removereq := &synctaskhandle.Request{
		Server: syncserver,
		Api:    synctaskhandle.RemoveTaskPath,
		Body:   string(stopjson),
	}

	removereq.ExecRequest()

	//比较数据
	failkeys := compare.Comparedata(sclient, tclient)

	if genreport {
		report.ReportContent["FailKeys"] = failkeys
		report.JsonToFile()
	}

}
