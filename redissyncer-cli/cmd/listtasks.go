/*
Copyright © 2019 NAME HERE <EMAIL ADDRESS>

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
	"github.com/spf13/cobra"
	"github.com/spf13/viper"
	"io/ioutil"
	"log"
	"net/http"
	"os"
	"strings"
)

// listtasksCmd represents the listtasks command
var listtasksCmd = &cobra.Command{
	Use:   "listtasks",
	Short: "A brief description of your command",
	Long: `A longer description that spans multiple lines and likely contains examples
and usage of using your command. For example:

Cobra is a CLI library for Go that empowers applications.
This application is a tool to generate the needed files
to quickly create a Cobra application.`,
	Run: func(cmd *cobra.Command, args []string) {
		a, _ := cmd.Flags().GetBool("all")
		n, _ := cmd.Flags().GetString("bynames")
		i, _ := cmd.Flags().GetString("byids")
		s, _ := cmd.Flags().GetString("bystatus")

		if a {
			execlisttasks(viper.GetViper().GetString("server"), "all", "")
			os.Exit(0)
		}
		if n != "" {
			execlisttasks(viper.GetViper().GetString("server"), "bynames", n)
			os.Exit(0)
		}
		if i != "" {
			execlisttasks(viper.GetViper().GetString("server"), "byids", i)
			os.Exit(0)
		}
		if s != "" {
			execlisttasks(viper.GetViper().GetString("server"), "bystatus", s)
			os.Exit(0)
		}
		cmd.Help()

	},
}

func init() {
	rootCmd.AddCommand(listtasksCmd)
	listtasksCmd.Flags().BoolP("all", "a", false, "list all tasks")
	listtasksCmd.Flags().StringP("bynames", "n", "", "list tasks by names splite by ','")
	listtasksCmd.Flags().StringP("byids", "i", "", "list tasks by taskids splite by ','")
	listtasksCmd.Flags().StringP("bystatus", "s", "", "list tasks by status like 'running' 'broken' 'stop'")
}

func execlisttasks(server string, regulation string, arg string) {

	url := server + "/api/v2/listtasks"
	jsonmap := make(map[string]interface{})
	jsonmap["regulation"] = regulation
	switch strings.ToLower(regulation) {
	case "bynames":
		jsonmap["tasknames"] = strings.Split(arg, ",")
	case "byids":
		jsonmap["taskids"] = strings.Split(arg, ",")
	case "bystatus":
		jsonmap["taskstatus"] = arg
	}
	jsonStr, err := json.Marshal(jsonmap)
	if err != nil {
		log.Println(err)
		os.Exit(1)
	}

	log.Println(string(jsonStr))

	client := &http.Client{}

	req, err := http.NewRequest("POST", url, strings.NewReader(string(jsonStr)))
	if err != nil {
		log.Println(err)
		os.Exit(1)
	}

	req.Header.Set("Content-Type", "application/json")

	resp, err := client.Do(req)

	defer resp.Body.Close()

	body, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		log.Println(err)
	}
	var dat map[string]interface{}
	json.Unmarshal(body, &dat)
	result, _ := json.MarshalIndent(dat, "", " ")
	fmt.Println(string(result))
	os.Exit(0)
}
