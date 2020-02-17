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
	"testcase/check"
	"testcase/common"
	"testcase/generatedata"
	"testcase/synctaskhandle"
	"time"
)

// testallCmd represents the testall command
var testallCmd = &cobra.Command{
	Use:   "testall",
	Short: "A brief description of your command",
	Long: `A longer description that spans multiple lines and likely contains examples
and usage of using your command. For example:

Cobra is a CLI library for Go that empowers applications.
This application is a tool to generate the needed files
to quickly create a Cobra application.`,
	Run: func(cmd *cobra.Command, args []string) {
		fmt.Println("testall called")
		report := &common.Report{ReportContent: make(map[string]interface{})}
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

		sclient := common.GetGoRedisClient(sopt)
		tclient := common.GetGoRedisClient(topt)

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

		fmt.Println(taskidsstrarray)
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
		stopreq.ExecRequest()

		//删除惹我
		removereq := &synctaskhandle.Request{
			Server: viper.GetViper().GetString("syncserver"),
			Api:    synctaskhandle.RemoveTaskPath,
			Body:   string(stopjson),
		}

		removereq.ExecRequest()

		//比较数据
		failkeys := check.Comparedata(sclient, tclient)

		if reportbool {
			report.ReportContent["FailKeys"] = failkeys
			report.JsonToFile()
		}

		//req := &synctaskhandle.Request{
		//	Server: viper.GetViper().GetString("syncserver"),
		//	Api:    synctaskhandle.ListTasksPath,
		//	Body:   string(byteValue),
		//}
		//resp := req.ExecRequest()
		//fmt.Println(viper.GetViper().GetString("syncserver"))
		//fmt.Println(resp)

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
