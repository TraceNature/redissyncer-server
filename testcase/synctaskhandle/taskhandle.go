package synctaskhandle

import (
	"encoding/json"
	"errors"
	"github.com/tidwall/gjson"
	"io/ioutil"
	"net/http"
	"os"
	"strings"
	"testcase/globalzap"
)

var logger = globalzap.GetLogger()

const CreateTaskPath = "/api/v2/createtask"
const StartTaskPath = "/api/v2/starttask"
const StopTaskPath = "/api/v2/stoptask"
const RemoveTaskPath = "/api/v2/removetask"
const ListTasksPath = "/api/v2/listtasks"

type Request struct {
	Server string
	Api    string
	Body   string
}

func (r Request) ExecRequest() (result string) {
	client := &http.Client{}
	req, err := http.NewRequest("POST", r.Server+r.Api, strings.NewReader(r.Body))

	if err != nil {
		logger.Sugar().Error(err)
		os.Exit(1)
	}

	req.Header.Set("Content-Type", "application/json")

	resp, err := client.Do(req)

	if err != nil {
		logger.Sugar().Error(err)
	}

	defer resp.Body.Close()

	body, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		//logger.Sugar().Error(err)
		logger.Sugar().Error(err)
		os.Exit(1)
	}
	var dat map[string]interface{}
	json.Unmarshal(body, &dat)
	bodystr, _ := json.MarshalIndent(dat, "", " ")
	return string(bodystr)
}

//创建同步任务
func CreateTask(syncserver string, createjson string) []string {
	createreq := &Request{
		Server: syncserver,
		Api:    CreateTaskPath,
		Body:   createjson,
	}

	resp := createreq.ExecRequest()
	taskids := gjson.Get(resp, "data").Array()
	if len(taskids) == 0 {
		logger.Sugar().Error(errors.New("task create faile"))
		logger.Sugar().Info(resp)
		os.Exit(1)
	}
	taskidsstrarray := []string{}
	for _, v := range taskids {
		//fmt.Println(gjson.Get(v.String(), "taskId").String())
		taskidsstrarray = append(taskidsstrarray, gjson.Get(v.String(), "taskId").String())
	}

	return taskidsstrarray

}

//Start task
func StartTask(syncserver string, taskid string) {
	jsonmap := make(map[string]interface{})
	jsonmap["taskid"] = taskid
	startjson, err := json.Marshal(jsonmap)
	if err != nil {
		logger.Sugar().Error(err)
		os.Exit(1)
	}
	startreq := &Request{
		Server: syncserver,
		Api:    StartTaskPath,
		Body:   string(startjson),
	}
	startreq.ExecRequest()

}

//Stop task by task ids
func StopTaskByIds(syncserver string, ids []string) {
	jsonmap := make(map[string]interface{})

	jsonmap["taskids"] = ids
	stopjsonStr, err := json.Marshal(jsonmap)
	if err != nil {
		logger.Sugar().Error(err)
		os.Exit(1)
	}
	stopreq := &Request{
		Server: syncserver,
		Api:    StopTaskPath,
		Body:   string(stopjsonStr),
	}
	stopreq.ExecRequest()

}

//Remove task by name
func RemoveTaskByName(syncserver string, taskname string) {
	jsonmap := make(map[string]interface{})

	taskids, err := GetSameTaskNameIds(syncserver, taskname)
	if err != nil {
		logger.Sugar().Error(err)
		os.Exit(1)
	}

	if len(taskids) == 0 {
		return
	}

	jsonmap["taskids"] = taskids
	stopjsonStr, err := json.Marshal(jsonmap)
	if err != nil {
		logger.Sugar().Error(err)
		os.Exit(1)
	}
	stopreq := &Request{
		Server: syncserver,
		Api:    StopTaskPath,
		Body:   string(stopjsonStr),
	}
	stopreq.ExecRequest()

	removereq := &Request{
		Server: syncserver,
		Api:    RemoveTaskPath,
		Body:   string(stopjsonStr),
	}

	removereq.ExecRequest()

}

//获取同步任务状态
func GetTaskStatus(syncserver string, ids []string) (map[string]string, error) {
	jsonmap := make(map[string]interface{})

	jsonmap["regulation"] = "byids"
	jsonmap["taskids"] = ids

	listtaskjsonStr, err := json.Marshal(jsonmap)
	if err != nil {
		return nil, err
	}
	listreq := &Request{
		Server: syncserver,
		Api:    ListTasksPath,
		Body:   string(listtaskjsonStr),
	}
	listresp := listreq.ExecRequest()
	taskarray := gjson.Get(listresp, "data").Array()

	if len(taskarray) == 0 {
		return nil, errors.New("No status return")
	}

	statusmap := make(map[string]string)

	for _, v := range taskarray {
		id := gjson.Get(v.String(), "taskId").String()
		status := gjson.Get(v.String(), "status").String()
		statusmap[id] = status
	}

	return statusmap, nil
}

// @title    GetSameTaskNameIds
// @description   获取同名任务列表
// @auth      Jsw             时间（2020/7/1   10:57 ）
// @param     syncserver        string         "redissyncer ip:port"
// @param    taskname        string         "任务名称"
// @return    taskids        []string         "任务id数组"
func GetSameTaskNameIds(syncserver string, taskname string) ([]string, error) {

	existstaskids := []string{}
	listjsonmap := make(map[string]interface{})
	listjsonmap["regulation"] = "bynames"
	listjsonmap["tasknames"] = strings.Split(taskname, ",")
	listjsonStr, err := json.Marshal(listjsonmap)
	if err != nil {
		logger.Info(err.Error())
		return nil, err
	}
	listtaskreq := &Request{
		Server: syncserver,
		Api:    ListTasksPath,
		Body:   string(listjsonStr),
	}
	listresp := listtaskreq.ExecRequest()

	tasklist := gjson.Get(listresp, "data").Array()

	if len(tasklist) > 0 {
		for _, v := range tasklist {
			existstaskids = append(existstaskids, gjson.Get(v.String(), "taskId").String())
		}
	}
	return existstaskids, nil
}
