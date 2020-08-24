package cases

import (
	"fmt"
	"github.com/tidwall/gjson"
	"go.uber.org/zap"
	"gopkg.in/yaml.v2"
	"io/ioutil"
	"os"
	"runtime"
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
	Case_Cluster2Cluster
	Case_ImportRdb2Single
	Case_ImportAof2Single
	Case_ImportRdb2Cluster
	Case_ImportAof2Cluster
)

var CaseTypeMap = map[int32]string{
	Case_Single2Single:          "Single2Single",
	Case_Single2SingleWithDBMap: "Single2SingleWithDBMap",
	Case_Single2Cluster:         "Single2Cluster",
	Case_Cluster2Cluster:        "Cluster2Cluster",
	Case_ImportRdb2Single:       "ImportRdb2Single",
	Case_ImportAof2Single:       "ImportAof2Single",
	Case_ImportRdb2Cluster:      "ImportRdb2Cluster",
	Case_ImportAof2Cluster:      "ImportAof2Cluster",
}

func (ct CaseType) String() string {
	switch ct {
	case Case_Single2Single:
		return "Single2Single"
	case Case_Single2SingleWithDBMap:
		return "Single2SingleWithDBMap"
	case Case_Single2Cluster:
		return "Single2Cluster"
	case Case_Cluster2Cluster:
		return "Cluster2Cluster"
	case Case_ImportRdb2Single:
		return "ImportRdb2Single"
	case Case_ImportAof2Single:
		return "ImportAof2Single"
	case Case_ImportRdb2Cluster:
		return "ImportRdb2Cluster"
	case Case_ImportAof2Cluster:
		return "ImportAof2Cluster"
	default:
		return ""
	}
}

type TestCase struct {
	SyncServer                string   `yaml:"syncserver"` //redissyncer server address
	SyncServerSshPort         string   `yaml:"syncserversshport"`
	SyncServerOsUser          string   `yaml:"syncserverosuser"`          //redissyncer server操作系统用户
	SyncServerOsUserPassword  string   `yaml:"syncserverosuserpassword"`  //redissyncer server操作系统用户密码
	CreateTaskFile            string   `yaml:"createtaskfile"`            //任务创建json文件路径
	GenDataDuration           int      `yaml:"gendataduration"`           //持续产生增量数据的时间,单位为秒
	DataGenInterval           int64    `yaml:"datageninterval"`           //线程内数据生成间隔，单位为毫秒
	GenDataThreads            int      `yaml:"gendatathreads"`            //持续生成数据的线程数量
	BigKV_KeySuffix_Len       int      `yaml:"bigkvkeysuffixlen"`         //大key后缀位数，按位数生成key后缀
	BigKV_Loopstep            int      `yaml:"bigkvloopstep"`             //大key循环次数，该参数决定大key value的长度
	BigKV_EXPIRE              int      `yaml:"bigkvexpire"`               //大key过期时间，单位为秒
	BigKV_ValuePrefix_Len     int      `yaml:"bigkvvalueprefixlen"`       //大key value前缀长度，按长度生成值的前缀
	Increment_KeySuffix_Len   int      `yaml:"incrementkeysuffixlen"`     //增量数据key后缀位数，按位生成key后缀
	Increment_Loopstep        int      `yaml:"incrementloopstep"`         //增量数据循环长度，影响增量数据value长度或操作次数
	Increment_EXPIRE          int      `yaml:"incrementexpire"`           //增量数据过期时间，单位为秒
	Increment_Threads         int      `yaml:"incrementthreads"`          //生成增量数据的线程数量
	Compare_BatchSize         int64    `yaml:"comparebatchsize"`          //比较List、Set、Zset类型时的每批次值的数量
	Compare_Threads           int      `yaml:"comparethreads"`            //比较db线程数量
	Compare_TTLDiff           float64  `yaml:"comparettldiff"`            //TTL最小差值
	GenRdbRedis               string   `yaml:"genrdbredis"`               //导入文件时生成rdb文件的redis服务器地址
	GenRdbRedisPassword       string   `yaml:"genrdbredispassword"`       //导入文件案例中生成rdb文件的redis服务器密码
	GenRdbRedisOsUser         string   `yaml:"genrdbredisosuser"`         //产生rdb文件的服务器操作系统user
	GenRdbRedisOsUserPassword string   `yaml:"genrdbredisosuserpassword"` //产生rdb文件的服务器操作系统user's password
	DumpFilePath              string   `yaml:"dumpfilepath"`              //rdb文件路径
	SyncOsFilePath            string   `yaml:"syncosfilepath"`            //dump 或 aof文件或目录的操作系统路径
	CaseType                  CaseType `yaml:"casetype"`                  //案例类型编号，可以通过 listcases子命令查询对应的case编号
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

func (tc *TestCase) Exec() {
	switch tc.CaseType.String() {
	case "Single2Single":
		logger.Sugar().Info("Execute " + tc.CaseType.String())
		tc.Single2Single()
	case "Single2SingleWithDBMap":
		logger.Sugar().Info("Execute " + tc.CaseType.String())
		tc.Single2SingleWithDBMap()
	case "Single2Cluster":
		logger.Sugar().Info("Execute " + tc.CaseType.String())
		tc.Single2Cluster()
	case "Cluster2Cluster":
		logger.Sugar().Info("Execute " + tc.CaseType.String())
		tc.Cluster2Cluster()
	case "ImportRdb2Single":
		logger.Sugar().Info("Execute " + tc.CaseType.String())
		tc.ImportRdb2Single()
	case "ImportAof2Single":
		logger.Sugar().Info("Execute " + tc.CaseType.String())
		tc.ImportAof2Single()
	case "ImportRdb2Cluster":
		logger.Sugar().Info("Execute " + tc.CaseType.String())
		tc.ImportRdb2Cluster()
	case "ImportAof2Cluster":
		logger.Sugar().Info("Execute " + tc.CaseType.String())
		tc.ImportAof2Cluster()
	default:
		logger.Sugar().Info("Nothing to be executed")
		return
	}

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

		if len(statusmap) == 0 {
			logger.Error("No status return")
			os.Exit(1)
		}

		for k, v := range statusmap {

			//fmt.Println(gjson.Get(v, "lastDataCommitIntervalTime").String())
			if v == "" {
				logger.Error("Task not exists ", zap.String("taskid", k))
				os.Exit(1)
			}

			if gjson.Get(v, "status").String() == "COMMANDRUNING" {
				if gjson.Get(v, "lastDataCommitIntervalTime").Int() < int64(60000) && gjson.Get(v, "lastDataUpdateIntervalTime").Int() < int64(60000) {
					iscommandrunning = false
				}
			}
			if gjson.Get(v, "status").String() == "BROKEN" {
				logger.Error("sync task broken! ", zap.String("taskid", k), zap.String("task_status", v))
				os.Exit(1)
			}

			if gjson.Get(v, "status").String() == "STOP" {
				fmt.Println("status is STOP")
				time.Sleep(20 * time.Second)
			}
		}

		if iscommandrunning {
			break
		}
		time.Sleep(3 * time.Second)
	}
}
