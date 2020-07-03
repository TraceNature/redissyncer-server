package compare

import (
	"github.com/go-redis/redis/v7"
	"github.com/sirupsen/logrus"
	"go.uber.org/zap"
	"math"
	"strconv"
	"testcase/global"
	"testcase/globalzap"
	"time"
)

var logger = global.GetInstance()
var zaplogger = globalzap.GetLogger()

type Compare struct {
	Source     *redis.Client //源redis
	Target     *redis.Client //目标redis
	LogOut     bool          //是否输出日志
	LogOutPath string        //日志输出路径
	BatchSize  int64         //比较List、Set、Zset类型时的每批次值的数量
}

func Comparedata(sourceclient *redis.Client, targetclient *redis.Client) (failkeys map[string]interface{}) {
	cursor := uint64(0)
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
					logger.WithFields(logrus.Fields{
						"key":            v,
						"type":           "string",
						"compare_result": compareresult,
					}).Info()
					failkeys[v] = "string"
				}
			case keytype == "list":
				compareresult := comparelist(v, sourceclient, targetclient)
				if compareresult == false {
					logger.WithFields(logrus.Fields{
						"key":            v,
						"type":           "list",
						"compare_result": compareresult,
					}).Info()
					failkeys[v] = "list"
				}
			case keytype == "set":
				compareresult := compareset(v, sourceclient, targetclient)
				if compareresult == false {
					logger.WithFields(logrus.Fields{
						"key":            v,
						"type":           "set",
						"compare_result": result,
					}).Info()
					failkeys[v] = "set"
				}
			case keytype == "zset":
				compareresult := comparezset(v, sourceclient, targetclient)
				if compareresult == false {
					logger.WithFields(logrus.Fields{
						"key":            v,
						"type":           "zset",
						"compare_result": compareresult,
					}).Info()
					failkeys[v] = "zset"
				}
			case keytype == "hash":
				compareresult := comparehset(v, sourceclient, targetclient)
				if compareresult == false {
					logger.WithFields(logrus.Fields{
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

func (compare *Compare) CompareDB() {
	cursor := uint64(0)
	zaplogger.Sugar().Info("Compare DB beging")
	ticker := time.NewTicker(time.Second * 10)
	defer ticker.Stop()

	for {
		result, c, err := compare.Source.Scan(cursor, "*", 10).Result()
		for _, v := range result {
			keytype, _ := compare.Source.Type(v).Result()
			switch {
			case keytype == "string":
				compare.CompareString(v)
			case keytype == "list":
				compare.CompareList(v)
			case keytype == "set":
				compare.CompareSet(v)
			case keytype == "zset":
				compare.CompareZset(v)
			case keytype == "hash":
				compare.CompareHash(v)
			default:
				zaplogger.Sugar().Info("No type find")
			}
		}
		cursor = c
		if err != nil {
			logger.Info(result, c, err)
		}

		if c == 0 {
			break
		}
		select {
		case <-ticker.C:
			zaplogger.Sugar().Info("Comparing...")
		default:
			continue
		}
	}
	logger.Info("Compare data end")
}

func (compare *Compare) CompareString(key string) {

	sourceval, serr := compare.Source.Get(key).Result()
	if serr != nil {
		zaplogger.Sugar().Error(serr)
		return
	}
	targetval, terr := compare.Target.Get(key).Result()
	if terr != nil {
		zaplogger.Sugar().Error(serr)
		return
	}

	if sourceval != targetval {
		zaplogger.Info("Compare_Result", zap.String("key", key), zap.String("keytype", "string"), zap.String("Not_Equal_Reason", "Value not equal"))
		return
	}

	sourcettl := compare.Source.PTTL(key).Val().Milliseconds()
	targetttl := compare.Target.PTTL(key).Val().Milliseconds()

	sub := sourcettl - targetttl
	if math.Abs(float64(sub)) > 10000 {
		zaplogger.Info("Compare_Result", zap.String("key", key), zap.String("keytype", "string"), zap.String("Not_Equal_Reason", "Key ttl difference is too large"))
		return
	}

}

func (compare *Compare) CompareList(key string) {
	sourcelen := compare.Source.LLen(key).Val()
	targetlen := compare.Target.LLen(key).Val()

	if sourcelen != targetlen {
		zaplogger.Info("Compare_Result", zap.String("key", key), zap.String("keytype", "list"), zap.String("Not_Equal_Reason", "Key length not equal"))
		return
	}

	sourcettl := compare.Source.PTTL(key).Val().Milliseconds()
	targetttl := compare.Target.PTTL(key).Val().Milliseconds()

	sub := sourcettl - targetttl
	if math.Abs(float64(sub)) > 10000 {
		zaplogger.Info("Compare_Result", zap.String("key", key), zap.String("keytype", "string"), zap.String("Not_Equal_Reason", "Key ttl difference is too large"))
		return
	}

	quotient := sourcelen / compare.BatchSize // integer division, decimals are truncated
	remainder := sourcelen % compare.BatchSize

	if quotient == 0 && remainder != 0 {
		sourcevaluse := compare.Source.LRange(key, int64(0), remainder).Val()
		targetvaluse := compare.Target.LRange(key, int64(0), remainder).Val()
		for k, v := range sourcevaluse {
			if targetvaluse[k] != v {
				zaplogger.Info("Compare_Result", zap.String("key", key), zap.String("keytype", "list"), zap.String("index", strconv.Itoa(k)), zap.String("Not_Equal_Reason", "value not equal"))
				continue
			}
		}
		return
	}

	for i := int64(0); i < quotient; i++ {
		sourcevaluse := compare.Source.LRange(key, int64(0)+int64(i)*compare.BatchSize, (compare.BatchSize-1)+i*compare.BatchSize).Val()
		targetvaluse := compare.Target.LRange(key, int64(0)+int64(i)*compare.BatchSize, (compare.BatchSize-1)+i*compare.BatchSize).Val()
		for k, v := range sourcevaluse {
			if targetvaluse[k] != v {
				zaplogger.Info("Compare_Result", zap.String("key", key), zap.String("keytype", "list"), zap.String("index", strconv.Itoa(k)), zap.String("Not_Equal_Reason", "value not equal"))
				continue
			}
		}
	}

	sourcevaluse := compare.Source.LRange(key, quotient*compare.BatchSize, quotient*compare.BatchSize).Val()
	targetvaluse := compare.Target.LRange(key, quotient*compare.BatchSize, quotient*compare.BatchSize).Val()
	for k, v := range sourcevaluse {
		if targetvaluse[k] != v {
			zaplogger.Info("Compare_Result", zap.String("key", key), zap.String("keytype", "list"), zap.String("index", strconv.Itoa(k)), zap.String("Not_Equal_Reason", "value not equal"))
			continue
		}
	}

	if remainder != 0 {
		sourcevaluse := compare.Source.LRange(key, quotient*compare.BatchSize, remainder+quotient*compare.BatchSize).Val()
		targetvaluse := compare.Target.LRange(key, quotient*compare.BatchSize, remainder+quotient*compare.BatchSize).Val()
		for k, v := range sourcevaluse {
			if targetvaluse[k] != v {
				zaplogger.Info("Compare_Result", zap.String("key", key), zap.String("keytype", "list"), zap.String("index", strconv.Itoa(k)), zap.String("Not_Equal_Reason", "value not equal"))
				continue
			}
		}
	}

}

func (compare *Compare) CompareHash(key string) {
	sourcelen := compare.Source.HLen(key).Val()
	targetlen := compare.Target.HLen(key).Val()
	if sourcelen != targetlen {
		zaplogger.Info("Compare_Result", zap.String("key", key), zap.String("keytype", "hash"), zap.String("Not_Equal_Reason", "Key length not equal"))
		return
	}

	sourcettl := compare.Source.PTTL(key).Val().Milliseconds()
	targetttl := compare.Target.PTTL(key).Val().Milliseconds()

	sub := sourcettl - targetttl
	if math.Abs(float64(sub)) > 10000 {
		zaplogger.Info("Compare_Result", zap.String("key", key), zap.String("keytype", "hash"), zap.String("Not_Equal_Reason", "Key ttl difference is too large"))
		return
	}

	cursor := uint64(0)
	for {
		sourceresult, c, err := compare.Source.HScan(key, cursor, "*", compare.BatchSize).Result()
		if err != nil {
			zaplogger.Info("Compare_Result", zap.String("key", key), zap.String("keytype", "hash"), zap.String("Not_Equal_Reason", "Source hscan error:"+err.Error()))
			return
		}

		for i := 0; i < len(sourceresult); i = +2 {
			if compare.Target.HGet(key, sourceresult[i]).Val() != sourceresult[i+1] {
				zaplogger.Info("Compare_Result", zap.String("key", key), zap.String("keytype", "hash"), zap.String("field", sourceresult[i]), zap.String("Not_Equal_Reason", "Field value not equal"))
				continue
			}
		}

		cursor = c
		if c == 0 {
			break
		}
	}

}

func (compare *Compare) CompareSet(key string) {
	sourcelen := compare.Source.SCard(key).Val()
	targetlen := compare.Target.SCard(key).Val()
	if sourcelen != targetlen {
		zaplogger.Info("Compare_Result", zap.String("key", key), zap.String("keytype", "set"), zap.String("Not_Equal_Reason", "Key length not equal"))
		return
	}

	sourcettl := compare.Source.PTTL(key).Val().Milliseconds()
	targetttl := compare.Target.PTTL(key).Val().Milliseconds()

	sub := sourcettl - targetttl
	if math.Abs(float64(sub)) > 10000 {
		zaplogger.Info("Compare_Result", zap.String("key", key), zap.String("keytype", "set"), zap.String("Not_Equal_Reason", "Key ttl difference is too large"))
		return
	}

	cursor := uint64(0)
	for {
		sourceresult, c, err := compare.Source.SScan(key, cursor, "*", compare.BatchSize).Result()
		if err != nil {
			zaplogger.Info("Compare_Result", zap.String("key", key), zap.String("keytype", "set"), zap.String("Not_Equal_Reason", "Source sscan error:"+err.Error()))
			return
		}

		for _, v := range sourceresult {
			if !compare.Target.SIsMember(key, v).Val() {
				zaplogger.Info("Compare_Result", zap.String("key", key), zap.String("keytype", "set"), zap.String("member", v), zap.String("Not_Equal_Reason", "Source member not exists in Target "))
				continue
			}
		}

		cursor = c
		if c == 0 {
			break
		}
	}
}

func (compare *Compare) CompareZset(key string) {
	sourcelen := compare.Source.ZCard(key).Val()
	targetlen := compare.Target.ZCard(key).Val()
	if sourcelen != targetlen {
		zaplogger.Info("Compare_Result", zap.String("key", key), zap.String("keytype", "zset"), zap.String("Not_Equal_Reason", "Key length not equal"))
		return
	}

	sourcettl := compare.Source.PTTL(key).Val().Milliseconds()
	targetttl := compare.Target.PTTL(key).Val().Milliseconds()

	sub := sourcettl - targetttl
	if math.Abs(float64(sub)) > 10000 {
		zaplogger.Info("Compare_Result", zap.String("key", key), zap.String("keytype", "zset"), zap.String("Not_Equal_Reason", "Key ttl difference is too large"))
		return
	}

	cursor := uint64(0)
	for {
		sourceresult, c, err := compare.Source.ZScan(key, cursor, "*", compare.BatchSize).Result()
		if err != nil {
			zaplogger.Info("Compare_Result", zap.String("key", key), zap.String("keytype", "zset"), zap.String("Not_Equal_Reason", "Source zscan error:"+err.Error()))
			return
		}

		for i := 0; i < len(sourceresult); i = + 2 {
			sourecemember := sourceresult[i]
			sourcescore := sourceresult[i+1]

			intcmd := compare.Target.ZRank(key, sourecemember)
			if intcmd == nil {
				zaplogger.Info("Compare_Result", zap.String("key", key), zap.String("keytype", "zset"), zap.String("member", sourecemember), zap.String("Not_Equal_Reason", "Source member not exists in Target"))
				continue
			}

			if strconv.FormatInt(intcmd.Val(), 10) != sourcescore {
				zaplogger.Info("Compare_Result", zap.String("key", key), zap.String("keytype", "zset"), zap.String("member", sourecemember), zap.String("Not_Equal_Reason", "member score not equal"))
			}

		}

		cursor = c
		if c == 0 {
			break
		}
	}

}
