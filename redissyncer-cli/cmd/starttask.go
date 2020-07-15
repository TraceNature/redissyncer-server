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
	"github.com/spf13/viper"
	"io/ioutil"
	"log"
	"net/http"
	"os"
	"strings"

	"github.com/spf13/cobra"
)

// starttaskCmd represents the starttask command
var starttaskCmd = &cobra.Command{
	Use:   "starttask",
	Short: "A brief description of your command",
	Long: `A longer description that spans multiple lines and likely contains examples
and usage of using your command. For example:

Cobra is a CLI library for Go that empowers applications.
This application is a tool to generate the needed files
to quickly create a Cobra application.`,
	Run: func(cmd *cobra.Command, args []string) {
		fmt.Println("starttask called")
		taskid, _ := cmd.Flags().GetString("taskid")
		afresh, _ := cmd.Flags().GetBool("afresh")
		execstarttask(viper.GetViper().GetString("server"), taskid, afresh)
	},
}

func init() {
	rootCmd.AddCommand(starttaskCmd)
	starttaskCmd.Flags().StringP("taskid", "i", "", "taskid to start")
	starttaskCmd.Flags().BoolP("afresh", "a", true, "whither sync from beging")
	starttaskCmd.MarkFlagRequired("taskid")
}

func execstarttask(server string, id string, afresh bool) {
	url := server + "/api/v2/starttask"
	log.Println(id)

	client := &http.Client{}
	jsonmap := make(map[string]interface{})
	jsonmap["taskid"] = id
	jsonmap["afresh"] = afresh
	jsonStr, err := json.Marshal(jsonmap)

	if err != nil {
		log.Println(err)
		os.Exit(1)
	}

	log.Println(string(jsonStr))

	req, err := http.NewRequest("POST", url, strings.NewReader(string(jsonStr)))
	if err != nil {
		log.Println(err)
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
}
