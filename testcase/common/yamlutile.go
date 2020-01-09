package common

import (
	"gopkg.in/yaml.v2"
	"io/ioutil"
	"log"
	"os"
	"sync"
)

var lock sync.Mutex

//YamlFileToMap Convert yaml fil to map
func YamlFileToMap(configfile string) *map[interface{}]interface{} {
	yamlmap := make(map[interface{}]interface{})
	yamlFile, err := ioutil.ReadFile(configfile)
	if err != nil {
		log.Printf("yamlFile.Get err   #%v ", err)
		os.Exit(1)
	}
	err = yaml.Unmarshal(yamlFile, yamlmap)
	if err != nil {
		log.Fatalf("Unmarshal: %v", err)
		os.Exit(1)
	}
	return &yamlmap
}

//MapToYamlString conver map to yaml
func MapToYamlString(yamlmap map[string]interface{}) string {
	lock.Lock()
	defer lock.Unlock()
	d, err := yaml.Marshal(&yamlmap)
	if err != nil {
		log.Fatalf("error: %v", err)
	}
	return string(d)
}
