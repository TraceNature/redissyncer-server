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
	"errors"
	"fmt"
	"github.com/spf13/cobra"
	"strings"
	"testcase/cases"
	"testcase/commons"
)

// execcaseCmd represents the execcase command
var execcaseCmd = &cobra.Command{
	Use:   "execcase",
	Short: "A brief description of your command",
	Long: `A longer description that spans multiple lines and likely contains examples
and usage of using your command. For example:

Cobra is a CLI library for Go that empowers applications.
This application is a tool to generate the needed files
to quickly create a Cobra application.`,
	Run: func(cmd *cobra.Command, args []string) {
		fmt.Println("execcase called")

		filepath, err := cmd.Flags().GetString("filepath")
		if err != nil {
			logger.Sugar().Error(err)
			return
		}

		if filepath != "" {
			tc := cases.NewTestCase()
			tc.ParseYamlFile(filepath)
			fmt.Println(tc)
			tc.Exec()
			return
		}

		dir, err := cmd.Flags().GetString("yamldir")
		if err != nil {
			logger.Sugar().Error(err)
			return
		}

		if !commons.FileExists(dir) {
			logger.Sugar().Error(errors.New("Director not exists"))
			return
		}

		if !commons.IsDir(dir) {
			logger.Sugar().Error(errors.New("Path not Director "))
			return
		}

		files, err := commons.GetAllFiles(dir)
		if err != nil {
			logger.Sugar().Error(err)
			return
		}

		fmt.Println(files)
		yamlfiles := []string{}
		for _, v := range files {
			//过滤指定格式
			ok := strings.HasSuffix(v, ".yml")
			if ok {
				yamlfiles = append(yamlfiles, v)
			}
		}

		if len(yamlfiles) != 0 {
			for _, v := range yamlfiles {
				tc := cases.NewTestCase()
				tc.ParseYamlFile(v)
				fmt.Println(tc)
				tc.Exec()
			}
		}
	},
}

func init() {

	execcaseCmd.Flags().StringP("filepath", "f", "", "Case yaml file to describe case config")
	execcaseCmd.Flags().StringP("yamldir", "d", "", "Director fo case yaml file ,exec all yamlfile in the directory")

	rootCmd.AddCommand(execcaseCmd)

}
