package common

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"os"
	"testcase/global"
	"time"
)

var logger = global.GetInstance()

type Report struct {
	ReportContent map[string]interface{}
}

func (r Report) Json() (jsonresult string, err error) {
	bodystr, err := json.MarshalIndent(r, "", " ")
	return string(bodystr), err
}

func (r Report) JsonToFile() {

	now := time.Now().Format("20060102150405000")
	filename := "report_" + now + ".json"
	bodystr, err := json.MarshalIndent(r.ReportContent, "", " ")

	if err != nil {
		logger.Error(err)
		os.Exit(1)
	}
	writeerr := ioutil.WriteFile(filename, bodystr, 0666)
	if writeerr != nil {
		logger.Error(writeerr)
	}

}
