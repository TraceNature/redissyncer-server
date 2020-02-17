package check

import (
	"github.com/go-redis/redis/v7"
	log "github.com/sirupsen/logrus"
	"testcase/global"
)

var logger = global.GetInstance()

func Comparedata(sourceclient *redis.Client, targetclient *redis.Client) (failkeys map[string]interface{}) {
	cursor := uint64(0)
	logger.SetFormatter(&log.JSONFormatter{})
	logger.Info("Compare data begin")
	failkeys = make(map[string]interface{})
	for {
		result, c, err := sourceclient.Scan(cursor, "*", 10).Result()
		for _, v := range result {
			keytype, _ := sourceclient.Type(v).Result()
			switch {
			case keytype == "string":
				compareresult := comparestring(v, sourceclient, targetclient)
				if compareresult == false {
					logger.WithFields(log.Fields{
						"key":            v,
						"type":           "string",
						"compare_result": compareresult,
					}).Info()
					failkeys[v] = "string"
				}
			case keytype == "list":
				compareresult := comparelist(v, sourceclient, targetclient)
				if compareresult == false {
					logger.WithFields(log.Fields{
						"key":            v,
						"type":           "list",
						"compare_result": compareresult,
					}).Info()
					failkeys[v] = "list"
				}
			case keytype == "set":
				compareresult := compareset(v, sourceclient, targetclient)
				if compareresult == false {
					logger.WithFields(log.Fields{
						"key":            v,
						"type":           "set",
						"compare_result": result,
					}).Info()
					failkeys[v] = "set"
				}
			case keytype == "zset":
				compareresult := comparezset(v, sourceclient, targetclient)
				if compareresult == false {
					logger.WithFields(log.Fields{
						"key":            v,
						"type":           "zset",
						"compare_result": compareresult,
					}).Info()
					failkeys[v] = "zset"
				}
			case keytype == "hash":
				compareresult := comparehset(v, sourceclient, targetclient)
				if compareresult == false {
					logger.WithFields(log.Fields{
						"key":            v,
						"type":           "hash",
						"compare_result": compareresult,
					}).Info()
					failkeys[v] = "hash"
				}
				//logger.Info("compare key:", v, compareresult)

			}
		}
		cursor = c
		if err != nil {
			logger.Info(result, c, err)
		}

		if c == 0 {
			break
		}
	}
	logger.Info("Compare data end")
	return failkeys

}

func comparestring(key string, source *redis.Client, target *redis.Client) (compareresult bool) {
	sourcelen, _ := source.Get(key).Result()
	targetlen, _ := target.Get(key).Result()
	return sourcelen == targetlen
}

func comparelist(key string, source *redis.Client, target *redis.Client) (compareresult bool) {
	sourcelen, _ := source.LLen(key).Result()
	targetlen, _ := target.LLen(key).Result()
	return sourcelen == targetlen
}

func compareset(key string, source *redis.Client, target *redis.Client) (compareresult bool) {
	sourcelen, _ := source.SCard(key).Result()
	targetlen, _ := target.SCard(key).Result()
	return sourcelen == targetlen
}
func comparehset(key string, source *redis.Client, target *redis.Client) (compareresult bool) {
	sourcelen, _ := source.HLen(key).Result()
	targetlen, _ := target.HLen(key).Result()
	return sourcelen == targetlen
}

func comparezset(key string, source *redis.Client, target *redis.Client) (compareresult bool) {
	sourcelen, _ := source.ZCard(key).Result()
	targetlen, _ := target.ZCard(key).Result()
	return sourcelen == targetlen
}
