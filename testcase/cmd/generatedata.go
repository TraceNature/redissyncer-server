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

import "github.com/spf13/cobra"

import (
	"fmt"
	"github.com/go-redis/redis/v7"
	"os"
	"testcase/common"
	"testcase/generatedata"
)

// generatedataCmd represents the generatedata command
var generatedataCmd = &cobra.Command{
	Use:   "generatedata",
	Short: "A brief description of your command",
	Long: `A longer description that spans multiple lines and likely contains examples
and usage of using your command. For example:

Cobra is a CLI library for Go that empowers applications.
This application is a tool to generate the needed files
to quickly create a Cobra application.`,
	Run: func(cmd *cobra.Command, args []string) {
		fmt.Println("generatedata called")
		redisaddr, _ := cmd.Flags().GetString("redisaddr")
		passwd, _ := cmd.Flags().GetString("redispassword")
		redisopt := &redis.Options{
			Addr: redisaddr,
			DB:   0, // use default DB
		}

		if passwd != "" {
			redisopt.Password = passwd
		}

		client := common.GetGoRedisClient(redisopt)

		_, err := client.Ping().Result()

		if err != nil {
			logger.Println(err)
			os.Exit(1)
		}

		d, _ := cmd.Flags().GetInt64("basedatasize")
		i, _ := cmd.Flags().GetInt64("incrementdatasize")
		fmt.Println(d, i)

		generatedata.GenerateBase(client, d)
		for loops := int64(0); loops < i; loops++ {
			generatedata.GenerateIncrement(client)
		}

	},
}

func init() {
	generatedataCmd.Flags().Int64P("basedatasize", "b", 0, "Generate base data loopstep")
	generatedataCmd.Flags().Int64P("incrementdatasize", "i", 0, "Increment data loopsizes")
	generatedataCmd.Flags().StringP("redisaddr", "a", "127.0.0.1:6379", "Redis address like '10.0.0.0:6379'")
	generatedataCmd.Flags().StringP("redispassword", "p", "", "Redis password")
	rootCmd.AddCommand(generatedataCmd)

}
