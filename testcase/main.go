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
package main

import (
	"testcase/cmd"
)

func init() {
	// Log as JSON instead of the default ASCII formatter.

	//log.SetFormatter(&log.JSONFormatter{})
	//
	//// Output to stdout instead of the default stderr
	//// Can be any io.Writer, see below for File example
	//log.SetOutput(os.Stdout)
	//
	//// Only log the warning severity or above.
	//log.SetLevel(log.WarnLevel)
}

func main() {
	cmd.Execute()
}
