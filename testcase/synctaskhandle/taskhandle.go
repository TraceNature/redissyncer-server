package synctaskhandle

import (
	"encoding/json"
	"io/ioutil"
	"net/http"
	"os"
	"strings"
	"testcase/global"
)

var logger = global.GetInstance()

const CreateTaskPath = "/api/v1/createtask"
const StartTaskPath = "/api/v1/starttask"
const StopTaskPath = "/api/v1/stoptask"
const RemoveTaskPath = "/api/v1/removetask"
const ListTasksPath = "/api/v1/listtasks"

type Request struct {
	Server string
	Api    string
	Body   string
}

func (r Request) ExecRequest() (result string) {

	client := &http.Client{}

	req, err := http.NewRequest("POST", r.Server+r.Api, strings.NewReader(r.Body))

	if err != nil {
		logger.Println(err)
		os.Exit(1)
	}

	req.Header.Set("Content-Type", "application/json")

	resp, err := client.Do(req)

	defer resp.Body.Close()

	body, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		logger.Println(err)
		os.Exit(1)
	}
	var dat map[string]interface{}
	json.Unmarshal(body, &dat)
	bodystr, _ := json.MarshalIndent(dat, "", " ")
	return string(bodystr)

}

func CreateTask(server string, createjson string) (result string) {

	url := server + "/api/v1/createtask"
	client := &http.Client{}

	req, err := http.NewRequest("POST", url, strings.NewReader(createjson))
	if err != nil {
		logger.Println(err)
		os.Exit(1)
	}

	req.Header.Set("Content-Type", "application/json")

	resp, err := client.Do(req)

	defer resp.Body.Close()

	body, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		logger.Println(err)
		os.Exit(1)
	}
	var dat map[string]interface{}
	json.Unmarshal(body, &dat)
	bodystr, _ := json.MarshalIndent(dat, "", " ")
	return string(bodystr)
}

func StopTask(server string, ids []string) (result string) {

	url := server + "/api/v1/stoptask"
	logger.Println(ids)

	client := &http.Client{}
	jsonmap := make(map[string]interface{})
	jsonmap["taskids"] = ids
	jsonStr, err := json.Marshal(jsonmap)

	if err != nil {
		logger.Println(err)
		os.Exit(1)
	}

	logger.Println(string(jsonStr))

	req, err := http.NewRequest("POST", url, strings.NewReader(string(jsonStr)))
	if err != nil {
		logger.Println(err)
	}

	req.Header.Set("Content-Type", "application/json")

	resp, err := client.Do(req)

	defer resp.Body.Close()

	body, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		logger.Println(err)
	}
	var dat map[string]interface{}
	json.Unmarshal(body, &dat)
	bodystr, _ := json.MarshalIndent(dat, "", " ")
	return string(bodystr)
}

func RemoveTask(server string, ids []string) (result string) {

	url := server + "/api/v1/removetask"
	logger.Println(ids)

	client := &http.Client{}
	jsonmap := make(map[string]interface{})
	jsonmap["taskids"] = ids
	jsonStr, err := json.Marshal(jsonmap)

	if err != nil {
		logger.Println(err)
		os.Exit(1)
	}

	logger.Println(string(jsonStr))

	req, err := http.NewRequest("POST", url, strings.NewReader(string(jsonStr)))
	if err != nil {
		logger.Println(err)
	}

	req.Header.Set("Content-Type", "application/json")

	resp, err := client.Do(req)

	defer resp.Body.Close()

	body, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		logger.Println(err)
	}
	var dat map[string]interface{}
	json.Unmarshal(body, &dat)
	bodystr, _ := json.MarshalIndent(dat, "", " ")
	return string(bodystr)
}

func StartTask(server string, id string, afresh bool) (result string) {
	url := server + "/api/v1/starttask"
	logger.Println(id)

	client := &http.Client{}
	jsonmap := make(map[string]interface{})
	jsonmap["taskid"] = id
	jsonmap["afresh"] = afresh
	jsonStr, err := json.Marshal(jsonmap)

	if err != nil {
		logger.Println(err)
		os.Exit(1)
	}

	logger.Println(string(jsonStr))

	req, err := http.NewRequest("POST", url, strings.NewReader(string(jsonStr)))
	if err != nil {
		logger.Println(err)
	}

	req.Header.Set("Content-Type", "application/json")

	resp, err := client.Do(req)

	defer resp.Body.Close()

	body, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		logger.Println(err)
	}
	var dat map[string]interface{}
	json.Unmarshal(body, &dat)
	bodystr, _ := json.MarshalIndent(dat, "", " ")
	return string(bodystr)
}
