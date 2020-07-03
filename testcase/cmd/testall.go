/*
Copyright © 2020 NAME HERE <EMAIL ADDRESS>

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package cmd

import (
	"encoding/json"
	"fmt"
	"github.com/go-redis/redis/v7"
	"github.com/spf13/cobra"
	"github.com/spf13/viper"
	"github.com/tidwall/gjson"
	"io/ioutil"
	"os"
	"strings"
	"testcase/compare"
	"testcase/commons"
	"testcase/generatedata"
	"testcase/synctaskhandle"
	"time"
)

// testallCmd represents the testall command
var testallCmd = &cobra.Command{
	Use:   "testall",
	Short: "基础测试",
	Long:  `用于测试任务从创建到销毁的全部流程，测试流程：环境预处理->生成全量数据->生成任务->启动任务->生成增量数据->停止任务->核对数据->删除任务`,
	Run: func(cmd *cobra.Command, args []string) {
		fmt.Println("testall called")

		report := &commons.Report{ReportContent: make(map[string]interface{})}
		//解析创建任务文件
		execfile, _ := cmd.Flags().GetString("createjson")
		jsonFile, err := os.Open(execfile)
		defer jsonFile.Close()

		if err != nil {
			logger.Println(err)
			os.Exit(1)
		}

		jsonbytes, _ := ioutil.ReadAll(jsonFile)
		saddr := gjson.Get(string(jsonbytes), "sourceRedisAddress").String()
		taddr := gjson.Get(string(jsonbytes), "targetRedisAddress").String()
		spasswd := gjson.Get(string(jsonbytes), "sourcePassword").String()
		tpasswd := gjson.Get(string(jsonbytes), "targetPassword").String()
		taskname := gjson.Get(string(jsonbytes), "taskName").String()
		reportbool, _ := cmd.Flags().GetBool("report")

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
			logger.Println(serr)
			if reportbool {
				report.ReportContent["Check source error"] = serr
				report.JsonToFile()
			}
			os.Exit(1)
		}

		if terr != nil {
			logger.Println(terr)
			logger.Println(terr)
			if reportbool {
				report.ReportContent["Check source error"] = terr
				report.JsonToFile()
			}
			os.Exit(1)
		}

		//flushdb
		sclient.FlushAll()
		tclient.FlushAll()

		//生成基础数据
		d, _ := cmd.Flags().GetInt64("basedatasize")
		generatedata.GenerateBase(sclient, d)

		//查看同名任务是否存在
		listjsonmap := make(map[string]interface{})
		listjsonmap["regulation"] = "bynames"
		listjsonmap["tasknames"] = strings.Split(taskname, ",")
		listjsonStr, err := json.Marshal(listjsonmap)
		if err != nil {
			logger.Info(err)
			os.Exit(1)
		}

		listtaskreq := &synctaskhandle.Request{
			Server: viper.GetViper().GetString("syncserver"),
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
				Server: viper.GetViper().GetString("syncserver"),
				Api:    synctaskhandle.StopTaskPath,
				Body:   string(stopexistsjson),
			}
			stopexistsreq.ExecRequest()

			//删除任务
			removeexistsreq := &synctaskhandle.Request{
				Server: viper.GetViper().GetString("syncserver"),
				Api:    synctaskhandle.RemoveTaskPath,
				Body:   string(stopexistsjson),
			}

			stopresult := removeexistsreq.ExecRequest()

			fmt.Println(stopresult)

		}

		//创建任务
		createreq := &synctaskhandle.Request{
			Server: viper.GetViper().GetString("syncserver"),
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
			logger.Println("task create faile:", resp)
			os.Exit(1)
		}

		//生成增量数据
		i, _ := cmd.Flags().GetInt64("incrementdatasize")
		for loops := int64(0); loops < i; loops++ {
			generatedata.GenerateIncrement(sclient)
		}

		time.Sleep(5 * time.Second)

		//停止任务
		stopmap := make(map[string]interface{})
		stopmap["taskids"] = taskidsstrarray
		stopjson, _ := json.MarshalIndent(stopmap, "", " ")
		stopreq := &synctaskhandle.Request{
			Server: viper.GetViper().GetString("syncserver"),
			Api:    synctaskhandle.StopTaskPath,
			Body:   string(stopjson),
		}
		stopresult := stopreq.ExecRequest()
		fmt.Println(stopresult)
		time.Sleep(5 * time.Second)

		//删除任务
		removereq := &synctaskhandle.Request{
			Server: viper.GetViper().GetString("syncserver"),
			Api:    synctaskhandle.RemoveTaskPath,
			Body:   string(stopjson),
		}

		removereq.ExecRequest()

		//比较数据
		failkeys := compare.Comparedata(sclient, tclient)

		if reportbool {
			report.ReportContent["FailKeys"] = failkeys
			report.JsonToFile()
		}

	},
}

func init() {
	testallCmd.Flags().String("createjson", "", "Create task json file")
	testallCmd.MarkFlagRequired("createjson")
	testallCmd.Flags().Int64P("basedatasize", "b", 1, "Generate base data loopstep")
	testallCmd.Flags().Int64P("incrementdatasize", "i", 1, "Increment data loopsizes")
	testallCmd.Flags().BoolP("report", "r", false, "Generate report file")
	rootCmd.AddCommand(testallCmd)

}
