package compare

import (
	"fmt"
	"github.com/go-redis/redis/v7"
	"github.com/panjf2000/ants/v2"
	"go.uber.org/zap"
	"math"
	"runtime"
	"strconv"
	"sync"
	"time"
)

type CompareSingle2Cluster struct {
	Source         *redis.Client        //源redis single
	Target         *redis.ClusterClient //目标redis single
	LogOut         bool                 //是否输出日志
	LogOutPath     string               //日志输出路径
	BatchSize      int64                //比较List、Set、Zset类型时的每批次值的数量
	CompareThreads int                  //比较db线程数量
	TTLDiff        float64              //TTL最小差值
	SourceDB       int                  //源redis DB number
	TargetDB       int                  //目标redis DB number
}

func (compare *CompareSingle2Cluster) CompareDB() {

	wg := sync.WaitGroup{}
	threads := runtime.NumCPU()
	if compare.CompareThreads > 0 {
		threads = compare.CompareThreads
	}
	cursor := uint64(0)
	zaplogger.Sugar().Info("CompareSingle2Cluster DB beging")
	ticker := time.NewTicker(time.Second * 20)
	defer ticker.Stop()

	pool, err := ants.NewPool(threads)

	if err != nil {
		zaplogger.Sugar().Error(err)
		return
	}
	defer pool.Release()

	for {
		result, c, err := compare.Source.Scan(cursor, "*", compare.BatchSize).Result()

		if err != nil {
			zaplogger.Sugar().Info(result, c, err)
			return
		}

		//当pool有活动worker时提交异步任务
		for {
			if pool.Free() > 0 {
				wg.Add(1)
				pool.Submit(func() {
					compare.CompareKeys(result)
					wg.Done()
				})
				break
			}
		}
		cursor = c

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
	wg.Wait()
	zaplogger.Sugar().Info("CompareSingle2Cluster End")
}

func (compare *CompareSingle2Cluster) CompareKeys(keys []string) {
	var result *CompareResult
	for _, v := range keys {
		keytype, err := compare.Source.Type(v).Result()
		if err != nil {
			zaplogger.Sugar().Error(err)
			continue
		}
		result = nil
		switch {
		case keytype == "string":
			result = compare.CompareString(v)
		case keytype == "list":
			result = compare.CompareList(v)
		case keytype == "set":
			result = compare.CompareSet(v)
		case keytype == "zset":
			result = compare.CompareZset(v)
		case keytype == "hash":
			result = compare.CompareHash(v)
		default:
			zaplogger.Info("No type find in compare list", zap.String("key", v), zap.String("type", keytype))
		}

		if result != nil && !result.IsEqual {
			zaplogger.Info("", zap.Any("CompareResult", result))
		}
	}
}

func (compare *CompareSingle2Cluster) CompareString(key string) *CompareResult {
	notequalreason := make(map[string]interface{})
	compareresult := NewCompareResult()
	compareresult.NotEqualReason = notequalreason
	compareresult.Key = key
	compareresult.KeyType = "string"
	compareresult.SourceDB = compare.SourceDB
	compareresult.TargetDB = compare.TargetDB

	sourceexists := KeyExists(compare.Source, key)
	targetexists := KeyExistsInCluster(compare.Target, key)

	if !sourceexists && !targetexists {
		return &compareresult
	}

	sourceval := compare.Source.Get(key).Val()
	targetval := compare.Target.Get(key).Val()

	if sourceval != targetval {
		//zaplogger.Info("Compare_Result", zap.Int("SourceDB", compare.SourceDB), zap.Int("TargetDB", compare.TargetDB), zap.String("key", key), zap.String("keytype", "string"), zap.String("Not_Equal_Reason", "Value not equal"))
		//return errors.Errorf("Not_Equal_Reason: %s,sval: %s,tval: %s", "String value not equal", sourceval, targetval)
		compareresult.IsEqual = false
		compareresult.NotEqualReason["description"] = "String value not equal"
		compareresult.NotEqualReason["sval"] = sourceval
		compareresult.NotEqualReason["tval"] = targetval
		return &compareresult

	}

	sourcettl := compare.Source.PTTL(key).Val().Milliseconds()
	targetttl := compare.Target.PTTL(key).Val().Milliseconds()

	sub := targetttl - sourcettl
	if math.Abs(float64(sub)) > compare.TTLDiff {
		//zaplogger.Info("Compare_Result", zap.Int("SourceDB", compare.SourceDB), zap.Int("TargetDB", compare.TargetDB), zap.String("key", key), zap.String("keytype", "string"), zap.String("Not_Equal_Reason", "Key ttl difference is too large"), zap.Float64("TTLDiff", math.Abs(float64(sub))))
		//return errors.Errorf("Not_Equal_Reason: %s, TTLDiff:%f", "Key ttl difference is too large", math.Abs(float64(sub)))
		compareresult.IsEqual = false
		compareresult.NotEqualReason["description"] = "Key ttl difference is too large"
		compareresult.NotEqualReason["TTLDiff"] = int64(math.Abs(float64(sub)))
		compareresult.NotEqualReason["sourcettl"] = sourcettl
		compareresult.NotEqualReason["targetttl"] = targetttl
		return &compareresult
	}
	return &compareresult
}

func (compare *CompareSingle2Cluster) CompareList(key string) *CompareResult {
	notequalreason := make(map[string]interface{})
	compareresult := NewCompareResult()
	compareresult.NotEqualReason = notequalreason
	compareresult.Key = key
	compareresult.KeyType = "list"
	compareresult.SourceDB = compare.SourceDB
	compareresult.TargetDB = compare.TargetDB

	sourceexists := KeyExists(compare.Source, key)
	targetexists := KeyExistsInCluster(compare.Target, key)

	if !sourceexists && !targetexists {
		return &compareresult
	}

	sourcelen := compare.Source.LLen(key).Val()
	targetlen := compare.Target.LLen(key).Val()

	if sourcelen != targetlen {
		//zaplogger.Info("Compare_Result", zap.Int("SourceDB", compare.SourceDB), zap.Int("TargetDB", compare.TargetDB), zap.String("key", key), zap.String("keytype", "list"), zap.String("Not_Equal_Reason", "Key length not equal"))
		//return errors.Errorf("Not_Equal_Reason: %s", "Key length not equal")
		compareresult.IsEqual = false
		compareresult.NotEqualReason["description"] = "Key length not equal"
		compareresult.NotEqualReason["sourcelen"] = sourcelen
		compareresult.NotEqualReason["targetlen"] = targetlen
		return &compareresult
	}

	sourcettl := compare.Source.PTTL(key).Val().Milliseconds()
	targetttl := compare.Target.PTTL(key).Val().Milliseconds()

	sub := targetttl - sourcettl
	if math.Abs(float64(sub)) > compare.TTLDiff {
		//zaplogger.Info("Compare_Result", zap.Int("SourceDB", compare.SourceDB), zap.Int("TargetDB", compare.TargetDB), zap.String("key", key), zap.String("keytype", "string"), zap.String("Not_Equal_Reason", "Key ttl difference is too large"), zap.Float64("TTLDiff", math.Abs(float64(sub))))
		//return errors.Errorf("Not_Equal_Reason: %s, TTLDiff:%f", "Key ttl difference is too large", math.Abs(float64(sub)))
		compareresult.IsEqual = false
		compareresult.NotEqualReason["description"] = "Key ttl difference is too large"
		compareresult.NotEqualReason["TTLDiff"] = int64(math.Abs(float64(sub)))
		compareresult.NotEqualReason["sourcettl"] = sourcettl
		compareresult.NotEqualReason["targetttl"] = targetttl
		return &compareresult
	}

	quotient := sourcelen / compare.BatchSize // integer division, decimals are truncated
	remainder := sourcelen % compare.BatchSize

	if quotient != 0 {
		var lrangeend int64
		for i := int64(0); i < quotient; i++ {
			if i == quotient-int64(1) {
				lrangeend = quotient * compare.BatchSize
			} else {
				lrangeend = (compare.BatchSize - 1) + i*compare.BatchSize
			}
			sourcevalues := compare.Source.LRange(key, int64(0)+i*compare.BatchSize, lrangeend).Val()
			targetvalues := compare.Target.LRange(key, int64(0)+i*compare.BatchSize, lrangeend).Val()
			for k, v := range sourcevalues {
				if targetvalues[k] != v {
					//zaplogger.Info("Compare_Result", zap.Int("SourceDB", compare.SourceDB), zap.Int("TargetDB", compare.TargetDB), zap.String("key", key), zap.String("keytype", "list"), zap.String("index", strconv.Itoa(k)), zap.String("Not_Equal_Reason", "value not equal"))
					//return errors.Errorf("Not_Equal_Reason: %s,Index:%d, sourceval:%s, targetval:%s", "index value not equal", int64(k)+i*compare.BatchSize, v, targetvalues[k])
					compareresult.IsEqual = false
					compareresult.NotEqualReason["description"] = "index value not equal"
					compareresult.NotEqualReason["Index"] = int64(k) + i*compare.BatchSize
					compareresult.NotEqualReason["sourceval"] = v
					compareresult.NotEqualReason["targetval"] = targetvalues[k]
					return &compareresult
				}
			}
		}
	}

	if remainder != 0 {
		var rangstart int64

		if quotient == int64(0) {
			rangstart = 0
		} else {
			rangstart = quotient*compare.BatchSize + 1
		}

		sourcevalues := compare.Source.LRange(key, rangstart, remainder+quotient*compare.BatchSize).Val()
		targetvalues := compare.Target.LRange(key, rangstart, remainder+quotient*compare.BatchSize).Val()
		for k, v := range sourcevalues {
			if targetvalues[k] != v {
				//zaplogger.Info("Compare_Result", zap.Int("SourceDB", compare.SourceDB), zap.Int("TargetDB", compare.TargetDB), zap.String("key", key), zap.String("keytype", "list"), zap.String("index", strconv.Itoa(k)), zap.String("Not_Equal_Reason", "value not equal"))
				//return errors.Errorf("Not_Equal_Reason: %s,Index:%d, sourceval:%s, targetval:%s", "index value not equal", int64(k)+rangstart, v, targetvalues[k])

				compareresult.IsEqual = false
				compareresult.NotEqualReason["description"] = "index value not equal"
				compareresult.NotEqualReason["Index"] = int64(k) + rangstart
				compareresult.NotEqualReason["sourceval"] = v
				compareresult.NotEqualReason["targetval"] = targetvalues[k]
				return &compareresult
			}
		}
	}

	return &compareresult

}

func (compare *CompareSingle2Cluster) CompareHash(key string) *CompareResult {
	notequalreason := make(map[string]interface{})
	compareresult := NewCompareResult()
	compareresult.NotEqualReason = notequalreason
	compareresult.Key = key
	compareresult.KeyType = "hash"
	compareresult.SourceDB = compare.SourceDB
	compareresult.TargetDB = compare.TargetDB

	sourceexists := KeyExists(compare.Source, key)
	targetexists := KeyExistsInCluster(compare.Target, key)

	if !sourceexists && !targetexists {
		return &compareresult
	}
	sourcelen := compare.Source.HLen(key).Val()
	targetlen := compare.Target.HLen(key).Val()

	if sourcelen != targetlen {
		//zaplogger.Info("Compare_Result", zap.Int("SourceDB", compare.SourceDB), zap.Int("TargetDB", compare.TargetDB), zap.String("key", key), zap.String("keytype", "hash"), zap.String("Not_Equal_Reason", "Key length not equal"))
		//return errors.Errorf("Not_Equal_Reason:%s, sourcelen:%d, targetlen:%d", "Key length not equal", sourcelen, targetlen)
		compareresult.IsEqual = false
		compareresult.NotEqualReason["description"] = "Key length not equal"
		compareresult.NotEqualReason["sourcelen"] = sourcelen
		compareresult.NotEqualReason["targetlen"] = targetlen
		return &compareresult
	}

	sourcettl := compare.Source.PTTL(key).Val().Milliseconds()
	targetttl := compare.Target.PTTL(key).Val().Milliseconds()

	sub := targetttl - sourcettl
	if math.Abs(float64(sub)) > compare.TTLDiff {
		//zaplogger.Info("Compare_Result", zap.Int("SourceDB", compare.SourceDB), zap.Int("TargetDB", compare.TargetDB), zap.String("key", key), zap.String("keytype", "hash"), zap.String("Not_Equal_Reason", "Key ttl difference is too large"), zap.Float64("TTLDiff", math.Abs(float64(sub))))
		//return errors.Errorf("Not_Equal_Reason: %s, TTLDiff:%f", "Key ttl difference is too large", math.Abs(float64(sub)))
		compareresult.IsEqual = false
		compareresult.NotEqualReason["description"] = "Key ttl difference is too large"
		compareresult.NotEqualReason["TTLDiff"] = int64(math.Abs(float64(sub)))
		compareresult.NotEqualReason["sourcettl"] = sourcettl
		compareresult.NotEqualReason["targetttl"] = targetttl
		return &compareresult
	}

	cursor := uint64(0)
	for {
		sourceresult, c, err := compare.Source.HScan(key, cursor, "*", compare.BatchSize).Result()

		if err != nil {
			//zaplogger.Info("Compare_Result", zap.Int("SourceDB", compare.SourceDB), zap.Int("TargetDB", compare.TargetDB), zap.String("key", key), zap.String("keytype", "hash"), zap.String("Not_Equal_Reason", "Source hscan error:"+err.Error()))
			//return errors.Errorf("Not_Equal_Reason:%s", "Source hscan error:"+err.Error())
			compareresult.IsEqual = false
			compareresult.NotEqualReason["description"] = "Source hscan error"
			compareresult.NotEqualReason["hscanerror"] = err.Error()
			return &compareresult
		}

		for i := 0; i < len(sourceresult); i = i + 2 {
			targetfieldval := compare.Target.HGet(key, sourceresult[i]).Val()
			if targetfieldval != sourceresult[i+1] {
				//zaplogger.Info("Compare_Result", zap.Int("SourceDB", compare.SourceDB), zap.Int("TargetDB", compare.TargetDB), zap.String("key", key), zap.String("keytype", "hash"), zap.String("field", sourceresult[i]), zap.String("Not_Equal_Reason", "Field value not equal"))
				//return errors.Errorf("Not_Equal_Reason:%s, field:%s, sourceval:%s, targetval:%s", "Field value not equal", sourceresult[i], sourceresult[i+1], targetfieldval)
				compareresult.IsEqual = false
				compareresult.NotEqualReason["description"] = "Field value not equal"
				compareresult.NotEqualReason["field"] = sourceresult[i]
				compareresult.NotEqualReason["sourceval"] = sourceresult[i+1]
				compareresult.NotEqualReason["targetval"] = targetfieldval
				return &compareresult
			}
		}
		cursor = c
		if c == uint64(0) {
			break
		}
	}
	return &compareresult
}

func (compare *CompareSingle2Cluster) CompareSet(key string) *CompareResult {
	notequalreason := make(map[string]interface{})
	compareresult := NewCompareResult()
	compareresult.NotEqualReason = notequalreason
	compareresult.Key = key
	compareresult.KeyType = "set"
	compareresult.SourceDB = compare.SourceDB
	compareresult.TargetDB = compare.TargetDB

	sourceexists := KeyExists(compare.Source, key)
	targetexists := KeyExistsInCluster(compare.Target, key)

	if !sourceexists && !targetexists {
		return &compareresult
	}
	sourcelen := compare.Source.SCard(key).Val()
	targetlen := compare.Target.SCard(key).Val()
	if sourcelen != targetlen {
		//zaplogger.Info("Compare_Result", zap.Int("SourceDB", compare.SourceDB), zap.Int("TargetDB", compare.TargetDB), zap.String("key", key), zap.String("keytype", "set"), zap.String("Not_Equal_Reason", "Key length not equal"))
		//return errors.Errorf("Not_Equal_Reason:%s, sourcelen:", "Key length not equal")
		compareresult.IsEqual = false
		compareresult.NotEqualReason["description"] = "Key length not equal"
		compareresult.NotEqualReason["sourcelen"] = sourcelen
		compareresult.NotEqualReason["targetlen"] = targetlen
		return &compareresult
	}

	sourcettl := compare.Source.PTTL(key).Val().Milliseconds()
	targetttl := compare.Target.PTTL(key).Val().Milliseconds()

	sub := targetttl - sourcettl
	if math.Abs(float64(sub)) > compare.TTLDiff {
		//zaplogger.Info("Compare_Result", zap.Int("SourceDB", compare.SourceDB), zap.Int("TargetDB", compare.TargetDB), zap.String("key", key), zap.String("keytype", "set"), zap.String("Not_Equal_Reason", "Key ttl difference is too large"), zap.Float64("TTLDiff", math.Abs(float64(sub))))
		compareresult.IsEqual = false
		compareresult.NotEqualReason["description"] = "Key ttl difference is too large"
		compareresult.NotEqualReason["TTLDiff"] = int64(math.Abs(float64(sub)))
		compareresult.NotEqualReason["sourcettl"] = sourcettl
		compareresult.NotEqualReason["targetttl"] = targetttl
		return &compareresult
	}

	cursor := uint64(0)
	for {
		sourceresult, c, err := compare.Source.SScan(key, cursor, "*", compare.BatchSize).Result()
		if err != nil {
			//zaplogger.Info("Compare_Result", zap.Int("SourceDB", compare.SourceDB), zap.Int("TargetDB", compare.TargetDB), zap.String("key", key), zap.String("keytype", "set"), zap.String("Not_Equal_Reason", "Source sscan error:"+err.Error()))
			compareresult.IsEqual = false
			compareresult.NotEqualReason["description"] = "Source sscan error"
			compareresult.NotEqualReason["sscanerror"] = err.Error()
			return &compareresult
		}

		for _, v := range sourceresult {
			if !compare.Target.SIsMember(key, v).Val() {
				zaplogger.Info("Compare_Result", zap.Int("SourceDB", compare.SourceDB), zap.Int("TargetDB", compare.TargetDB), zap.String("key", key), zap.String("keytype", "set"), zap.String("member", v), zap.String("Not_Equal_Reason", "Source member not exists in Target "))
				compareresult.IsEqual = false
				compareresult.NotEqualReason["description"] = "Source member not exists in Target"
				compareresult.NotEqualReason["member"] = v
				return &compareresult
			}
		}

		cursor = c
		if c == 0 {
			break
		}
	}
	return &compareresult
}

func (compare *CompareSingle2Cluster) CompareZset(key string) *CompareResult {
	notequalreason := make(map[string]interface{})
	compareresult := NewCompareResult()
	compareresult.NotEqualReason = notequalreason
	compareresult.Key = key
	compareresult.KeyType = "zset"
	compareresult.SourceDB = compare.SourceDB
	compareresult.TargetDB = compare.TargetDB

	sourceexists := KeyExists(compare.Source, key)
	targetexists := KeyExistsInCluster(compare.Target, key)

	if !sourceexists && !targetexists {
		return &compareresult
	}

	sourcelen := compare.Source.ZCard(key).Val()
	targetlen := compare.Target.ZCard(key).Val()
	if sourcelen != targetlen {
		//zaplogger.Info("Compare_Result", zap.Int("SourceDB", compare.SourceDB), zap.Int("TargetDB", compare.TargetDB), zap.String("key", key), zap.String("keytype", "zset"), zap.String("Not_Equal_Reason", "Key length not equal"))

		compareresult.IsEqual = false
		compareresult.NotEqualReason["description"] = "Key length not equal"
		compareresult.NotEqualReason["sourcelen"] = sourcelen
		compareresult.NotEqualReason["targetlen"] = targetlen
		return &compareresult
	}

	sourcettl := compare.Source.PTTL(key).Val().Milliseconds()
	targetttl := compare.Target.PTTL(key).Val().Milliseconds()

	sub := targetttl - sourcettl
	if math.Abs(float64(sub)) > compare.TTLDiff {
		//zaplogger.Info("Compare_Result", zap.Int("SourceDB", compare.SourceDB), zap.Int("TargetDB", compare.TargetDB), zap.String("key", key), zap.String("keytype", "zset"), zap.String("Not_Equal_Reason", "Key ttl difference is too large"), zap.Float64("TTLDiff", math.Abs(float64(sub))))
		compareresult.IsEqual = false
		compareresult.NotEqualReason["description"] = "Key ttl difference is too large"
		compareresult.NotEqualReason["TTLDiff"] = int64(math.Abs(float64(sub)))
		compareresult.NotEqualReason["sourcettl"] = sourcettl
		compareresult.NotEqualReason["targetttl"] = targetttl

		return &compareresult
	}

	cursor := uint64(0)
	for {
		sourceresult, c, err := compare.Source.ZScan(key, cursor, "*", compare.BatchSize).Result()
		if err != nil {
			//zaplogger.Info("Compare_Result", zap.Int("SourceDB", compare.SourceDB), zap.Int("TargetDB", compare.TargetDB), zap.String("key", key), zap.String("keytype", "zset"), zap.String("Not_Equal_Reason", "Source zscan error:"+err.Error()))
			compareresult.IsEqual = false
			compareresult.NotEqualReason["description"] = "Source zscan error"
			compareresult.NotEqualReason["zscanerror"] = err.Error()
			return &compareresult
		}

		for i := 0; i < len(sourceresult); i = i + 2 {
			sourecemember := sourceresult[i]
			sourcescore, err := strconv.ParseFloat(sourceresult[i+1], 64)
			if err != nil {
				//zaplogger.Sugar().Error(err)
				compareresult.IsEqual = false
				compareresult.NotEqualReason["description"] = "Convert sourcescore to float64 error"
				compareresult.NotEqualReason["error"] = err.Error()
				return &compareresult
			}

			intcmd := compare.Target.ZRank(key, sourecemember)
			targetscore := compare.Target.ZScore(key, sourecemember).Val()

			if intcmd == nil {
				//zaplogger.Info("Compare_Result", zap.Int("SourceDB", compare.SourceDB), zap.Int("TargetDB", compare.TargetDB), zap.String("key", key), zap.String("keytype", "zset"), zap.String("member", sourecemember), zap.String("Not_Equal_Reason", "Source member not exists in Target"))
				compareresult.IsEqual = false
				compareresult.NotEqualReason["description"] = "Source member not exists in Target"
				compareresult.NotEqualReason["member"] = sourecemember
				return &compareresult
			}

			if targetscore != sourcescore {
				zaplogger.Info("Compare_Result", zap.Int("SourceDB", compare.SourceDB), zap.Int("TargetDB", compare.TargetDB), zap.String("key", key), zap.String("keytype", "zset"), zap.String("member", sourecemember), zap.String("Not_Equal_Reason", "member score not equal"))
				fmt.Println(targetscore, sourcescore)
				compareresult.IsEqual = false
				compareresult.NotEqualReason["description"] = "member score not equal"
				compareresult.NotEqualReason["member"] = sourecemember
				compareresult.NotEqualReason["sourcescore"] = sourcescore
				compareresult.NotEqualReason["targetscore"] = targetscore
				return &compareresult
			}

		}

		cursor = c
		if c == 0 {
			break
		}
	}
	return &compareresult
}

func KeyExistsInCluster(client *redis.ClusterClient, key string) bool {
	exists := client.Exists(key).Val()
	if exists == int64(1) {
		return true
	} else {
		return false
	}
}
