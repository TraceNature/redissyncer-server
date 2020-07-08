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
	"fmt"
	"github.com/spf13/cobra"
)

// testallCmd represents the testall command
var testallCmd = &cobra.Command{
	Use:   "testall",
	Short: "基础测试",
	Long:  `用于测试任务从创建到销毁的全部流程，测试流程：环境预处理->生成全量数据->生成任务->启动任务->生成增量数据->停止任务->核对数据->删除任务`,
	Run: func(cmd *cobra.Command, args []string) {
		fmt.Println("testall called")

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
