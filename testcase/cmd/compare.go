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
	"github.com/go-redis/redis/v7"
	"os"
	"testcase/compare"
	"testcase/commons"

	"github.com/spf13/cobra"
)

// compareCmd represents the compare command
var compareCmd = &cobra.Command{
	Use:   "compare",
	Short: "A brief description of your command",
	Long: `A longer description that spans multiple lines and likely contains examples
and usage of using your command. For example:

Cobra is a CLI library for Go that empowers applications.
This application is a tool to generate the needed files
to quickly create a Cobra application.`,
	Run: func(cmd *cobra.Command, args []string) {
		fmt.Println("compare called")
		saddr, _ := cmd.Flags().GetString("saddr")
		spasswd, _ := cmd.Flags().GetString("spassword")
		taddr, _ := cmd.Flags().GetString("taddr")
		tpasswd, _ := cmd.Flags().GetString("spassword")

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

		logger.Println(sopt)
		logger.Println(topt)

		sclient := commons.GetGoRedisClient(sopt)
		tclient := commons.GetGoRedisClient(topt)

		_, serr := sclient.Ping().Result()
		_, terr := tclient.Ping().Result()

		if serr != nil {
			logger.Println(serr)
			os.Exit(1)
		}

		if terr != nil {
			logger.Println(terr)
			os.Exit(1)
		}

		compare.Comparedata(sclient, tclient)
	},
}

func init() {
	compareCmd.Flags().String("saddr", "127.0.0.1:6379", "Sourece redis address")
	compareCmd.Flags().String("spassword", "", "source redis password")
	compareCmd.Flags().String("taddr", "127.0.0.1:6379", "Target redis address")
	compareCmd.Flags().String("tpassword", "", "Target redis password")
	rootCmd.AddCommand(compareCmd)
}
