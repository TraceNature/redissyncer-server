package compare

import (
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

	//比较key的存在状态是否一致
	result := compare.KeyExistsStatusEqual(key)
	if !result.IsEqual {
		return result
	}

	//比较string value是否一致
	result = compare.CompareStringVal(key)
	if !result.IsEqual {
		return result
	}

	//比较ttl差值是否在允许范围内
	result = compare.DiffTTLOver(key)
	if !result.IsEqual {
		return result
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

	result := compare.KeyExistsStatusEqual(key)
	if !result.IsEqual {
		return result
	}

	result = compare.CompareListLen(key)
	if !result.IsEqual {
		return result
	}

	result = compare.DiffTTLOver(key)
	if !result.IsEqual {
		return result
	}

	result = compare.CompareListIndexVal(key)
	if !result.IsEqual {
		return result
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

	result := compare.KeyExistsStatusEqual(key)
	if !result.IsEqual {
		return result
	}

	result = compare.CompareHashLen(key)
	if !result.IsEqual {
		return result
	}

	result = compare.CompareHashFieldVal(key)
	if !result.IsEqual {
		return result
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

	result := compare.KeyExistsStatusEqual(key)
	if !result.IsEqual {
		return result
	}

	result = compare.CompareSetLen(key)
	if !result.IsEqual {
		return result
	}

	result = compare.DiffTTLOver(key)
	if !result.IsEqual {
		return result
	}

	result = compare.CompareSetMember(key)
	if !result.IsEqual {
		return result
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

	result := compare.KeyExistsStatusEqual(key)
	if !result.IsEqual {
		return result
	}

	result = compare.CompareZsetLen(key)
	if !result.IsEqual {
		return result
	}

	result = compare.DiffTTLOver(key)
	if !result.IsEqual {
		return result
	}

	result = compare.CompareZsetMemberScore(key)
	if !result.IsEqual {
		return result
	}
	return &compareresult
}

//判断key在source和target同时不存在
func (compare *CompareSingle2Cluster) KeyExistsStatusEqual(key string) *CompareResult {

	compareresult := NewCompareResult()
	reason := make(map[string]interface{})
	compareresult.Key = key
	//compareresult.KeyType = "Zset"
	compareresult.SourceDB = compare.SourceDB
	compareresult.TargetDB = compare.TargetDB

	sourceexists := KeyExists(compare.Source, key)
	targetexists := KeyExistsInCluster(compare.Target, key)

	if sourceexists == targetexists {
		return &compareresult
	}

	compareresult.IsEqual = false
	reason["description"] = "Source or Target key not exists"
	reason["source"] = sourceexists
	reason["target"] = targetexists
	compareresult.KeyDiffReason = append(compareresult.KeyDiffReason, reason)
	return &compareresult
}

//比较Zset member以及sore值是否一致
func (compare *CompareSingle2Cluster) CompareZsetMemberScore(key string) *CompareResult {
	compareresult := NewCompareResult()
	reason := make(map[string]interface{})
	compareresult.Key = key
	compareresult.KeyType = "Zset"
	compareresult.SourceDB = compare.SourceDB
	compareresult.TargetDB = compare.TargetDB

	cursor := uint64(0)
	for {
		sourceresult, c, err := compare.Source.ZScan(key, cursor, "*", compare.BatchSize).Result()
		if err != nil {
			compareresult.IsEqual = false
			reason["description"] = "Source zscan error"
			reason["zscanerror"] = err.Error()
			compareresult.KeyDiffReason = append(compareresult.KeyDiffReason, reason)
			return &compareresult
		}

		for i := 0; i < len(sourceresult); i = i + 2 {
			sourecemember := sourceresult[i]
			sourcescore, err := strconv.ParseFloat(sourceresult[i+1], 64)
			if err != nil {
				compareresult.IsEqual = false
				reason["description"] = "Convert sourcescore to float64 error"
				reason["floattostringerror"] = err.Error()
				compareresult.KeyDiffReason = append(compareresult.KeyDiffReason, reason)
				return &compareresult
			}

			intcmd := compare.Target.ZRank(key, sourecemember)
			targetscore := compare.Target.ZScore(key, sourecemember).Val()

			if intcmd == nil {
				compareresult.IsEqual = false
				reason["description"] = "Source zset member not exists in Target"
				reason["member"] = sourecemember
				compareresult.KeyDiffReason = append(compareresult.KeyDiffReason, reason)
				return &compareresult
			}

			if targetscore != sourcescore {
				compareresult.IsEqual = false
				reason["description"] = "zset member score not equal"
				reason["member"] = sourecemember
				reason["sourcescore"] = sourcescore
				reason["targetscore"] = targetscore
				compareresult.KeyDiffReason = append(compareresult.KeyDiffReason, reason)
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

//比较zset 长度是否一致
func (compare *CompareSingle2Cluster) CompareZsetLen(key string) *CompareResult {
	compareresult := NewCompareResult()
	reason := make(map[string]interface{})
	compareresult.Key = key
	compareresult.KeyType = "Zset"
	compareresult.SourceDB = compare.SourceDB
	compareresult.TargetDB = compare.TargetDB

	sourcelen := compare.Source.ZCard(key).Val()
	targetlen := compare.Target.ZCard(key).Val()
	if sourcelen != targetlen {
		compareresult.IsEqual = false
		reason["description"] = "Zset length not equal"
		reason["sourcelen"] = sourcelen
		reason["targetlen"] = targetlen
		compareresult.KeyDiffReason = append(compareresult.KeyDiffReason, reason)
		return &compareresult
	}
	return &compareresult
}

//比较set member 是否一致
func (compare *CompareSingle2Cluster) CompareSetMember(key string) *CompareResult {
	compareresult := NewCompareResult()
	reason := make(map[string]interface{})
	compareresult.Key = key
	compareresult.KeyType = "set"
	compareresult.SourceDB = compare.SourceDB
	compareresult.TargetDB = compare.TargetDB

	cursor := uint64(0)
	for {
		sourceresult, c, err := compare.Source.SScan(key, cursor, "*", compare.BatchSize).Result()
		if err != nil {
			compareresult.IsEqual = false
			reason["description"] = "Source sscan error"
			reason["sscanerror"] = err.Error()
			compareresult.KeyDiffReason = append(compareresult.KeyDiffReason, reason)
			return &compareresult
		}

		for _, v := range sourceresult {
			if !compare.Target.SIsMember(key, v).Val() {
				compareresult.IsEqual = false
				reason["description"] = "Source set member not exists in Target"
				reason["member"] = v
				compareresult.KeyDiffReason = append(compareresult.KeyDiffReason, reason)
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

//比较set长度
func (compare *CompareSingle2Cluster) CompareSetLen(key string) *CompareResult {
	compareresult := NewCompareResult()
	reason := make(map[string]interface{})
	compareresult.Key = key
	compareresult.KeyType = "set"
	compareresult.SourceDB = compare.SourceDB
	compareresult.TargetDB = compare.TargetDB

	sourcelen := compare.Source.SCard(key).Val()
	targetlen := compare.Target.SCard(key).Val()
	if sourcelen != targetlen {
		compareresult.IsEqual = false
		reason["description"] = "Set length not equal"
		reason["sourcelen"] = sourcelen
		reason["targetlen"] = targetlen
		compareresult.KeyDiffReason = append(compareresult.KeyDiffReason, reason)
		return &compareresult
	}
	return &compareresult
}

//比较hash field value 返回首个不相等的field
func (compare *CompareSingle2Cluster) CompareHashFieldVal(key string) *CompareResult {
	compareresult := NewCompareResult()
	reason := make(map[string]interface{})
	compareresult.Key = key
	compareresult.KeyType = "hash"
	compareresult.SourceDB = compare.SourceDB
	compareresult.TargetDB = compare.TargetDB

	cursor := uint64(0)
	for {
		sourceresult, c, err := compare.Source.HScan(key, cursor, "*", compare.BatchSize).Result()

		if err != nil {
			compareresult.IsEqual = false
			reason["description"] = "Source hscan error"
			reason["hscanerror"] = err.Error()
			compareresult.KeyDiffReason = append(compareresult.KeyDiffReason, reason)
			return &compareresult
		}

		for i := 0; i < len(sourceresult); i = i + 2 {
			targetfieldval := compare.Target.HGet(key, sourceresult[i]).Val()
			if targetfieldval != sourceresult[i+1] {
				compareresult.IsEqual = false
				reason["description"] = "Field value not equal"
				reason["field"] = sourceresult[i]
				reason["sourceval"] = sourceresult[i+1]
				reason["targetval"] = targetfieldval
				compareresult.KeyDiffReason = append(compareresult.KeyDiffReason, reason)
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

//比较hash长度
func (compare *CompareSingle2Cluster) CompareHashLen(key string) *CompareResult {
	compareresult := NewCompareResult()
	reason := make(map[string]interface{})
	compareresult.Key = key
	compareresult.KeyType = "hash"
	compareresult.SourceDB = compare.SourceDB
	compareresult.TargetDB = compare.TargetDB

	sourcelen := compare.Source.HLen(key).Val()
	targetlen := compare.Target.HLen(key).Val()

	if sourcelen != targetlen {

		compareresult.IsEqual = false
		reason["description"] = "Hash length not equal"
		reason["sourcelen"] = sourcelen
		reason["targetlen"] = targetlen
		compareresult.KeyDiffReason = append(compareresult.KeyDiffReason, reason)
		return &compareresult
	}
	return &compareresult
}

//比较list index对应值是否一致，返回第一条错误的index以及源和目标对应的值
func (compare *CompareSingle2Cluster) CompareListIndexVal(key string) *CompareResult {
	compareresult := NewCompareResult()
	reason := make(map[string]interface{})
	compareresult.Key = key
	compareresult.KeyType = "list"
	compareresult.SourceDB = compare.SourceDB
	compareresult.TargetDB = compare.TargetDB

	sourcelen := compare.Source.LLen(key).Val()
	//targetlen := compare.Target.LLen(key).Val()

	compareresult.Key = key
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
					compareresult.IsEqual = false
					reason["description"] = "List index value not equal"
					reason["Index"] = int64(k) + i*compare.BatchSize
					reason["sourceval"] = v
					reason["targetval"] = targetvalues[k]
					compareresult.KeyDiffReason = append(compareresult.KeyDiffReason, reason)
					return &compareresult
				}
			}
		}
	}

	if remainder != 0 {
		var rangstart int64

		if quotient == int64(0) {
			rangstart = int64(0)
		} else {
			rangstart = quotient*compare.BatchSize + 1
		}

		sourcevalues := compare.Source.LRange(key, rangstart, remainder+quotient*compare.BatchSize).Val()
		targetvalues := compare.Target.LRange(key, rangstart, remainder+quotient*compare.BatchSize).Val()
		for k, v := range sourcevalues {
			if targetvalues[k] != v {
				compareresult.IsEqual = false
				reason["description"] = "List index value not equal"
				reason["Index"] = int64(k) + rangstart
				reason["sourceval"] = v
				reason["targetval"] = targetvalues[k]
				compareresult.KeyDiffReason = append(compareresult.KeyDiffReason, reason)
				return &compareresult
			}
		}
	}

	return &compareresult

}

//比较list长度是否一致
func (compare *CompareSingle2Cluster) CompareListLen(key string) *CompareResult {
	compareresult := NewCompareResult()
	reason := make(map[string]interface{})
	compareresult.Key = key
	compareresult.KeyType = "list"
	compareresult.SourceDB = compare.SourceDB
	compareresult.TargetDB = compare.TargetDB

	sourcelen := compare.Source.LLen(key).Val()
	targetlen := compare.Target.LLen(key).Val()

	compareresult.Key = key
	if sourcelen != targetlen {
		compareresult.IsEqual = false
		reason["description"] = "List length not equal"
		reason["sourcelen"] = sourcelen
		reason["targetlen"] = targetlen
		compareresult.KeyDiffReason = append(compareresult.KeyDiffReason, reason)
		return &compareresult
	}
	return &compareresult
}

//对比string类型value是否一致
func (compare *CompareSingle2Cluster) CompareStringVal(key string) *CompareResult {
	compareresult := NewCompareResult()
	reason := make(map[string]interface{})
	compareresult.Key = key
	compareresult.KeyType = "string"
	compareresult.SourceDB = compare.SourceDB
	compareresult.TargetDB = compare.TargetDB

	sourceval := compare.Source.Get(key).Val()
	targetval := compare.Target.Get(key).Val()
	compareresult.Key = key
	if sourceval != targetval {
		compareresult.IsEqual = false
		reason["description"] = "String value not equal"
		reason["sval"] = sourceval
		reason["tval"] = targetval
		compareresult.KeyDiffReason = append(compareresult.KeyDiffReason, reason)
		return &compareresult
	}
	return &compareresult
}

//对比key TTl差值
func (compare *CompareSingle2Cluster) DiffTTLOver(key string) *CompareResult {
	compareresult := NewCompareResult()
	reason := make(map[string]interface{})
	compareresult.Key = key
	compareresult.KeyType = "string"
	compareresult.SourceDB = compare.SourceDB
	compareresult.TargetDB = compare.TargetDB

	sourcettl := compare.Source.PTTL(key).Val().Milliseconds()
	targetttl := compare.Target.PTTL(key).Val().Milliseconds()

	sub := targetttl - sourcettl
	if math.Abs(float64(sub)) > compare.TTLDiff {
		compareresult.IsEqual = false
		reason["description"] = "Key ttl difference is too large"
		reason["TTLDiff"] = int64(math.Abs(float64(sub)))
		reason["sourcettl"] = sourcettl
		reason["targetttl"] = targetttl

		compareresult.KeyDiffReason = append(compareresult.KeyDiffReason, reason)
		return &compareresult
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
