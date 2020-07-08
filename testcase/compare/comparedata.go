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
	"testcase/global"
	"testcase/globalzap"
	"time"
)

var logger = global.GetInstance()
var zaplogger = globalzap.GetLogger()

type Compare struct {
	Source         *redis.Client //源redis single
	Target         *redis.Client //目标redis single
	LogOut         bool          //是否输出日志
	LogOutPath     string        //日志输出路径
	BatchSize      int64         //比较List、Set、Zset类型时的每批次值的数量
	CompareThreads int           //比较db线程数量
	TTLDiff        float64       //TTL最小差值
	SourceDB       int           //源redis DB number
	TargetDB       int           //目标redis DB number
}

func (compare *Compare) CompareDB() {
	wg := sync.WaitGroup{}
	threads := runtime.NumCPU()
	if compare.CompareThreads > 0 {
		threads = compare.CompareThreads
	}
	cursor := uint64(0)
	zaplogger.Sugar().Info("Compare DB beging")
	ticker := time.NewTicker(time.Second * 10)
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
	logger.Info("Compare data end")
}

func (compare *Compare) CompareKeys(keys []string) {
	for _, v := range keys {
		keytype, err := compare.Source.Type(v).Result()
		if err != nil {
			zaplogger.Sugar().Error(err)
			continue
		}
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
			zaplogger.Info("No type find in compare list", zap.String("key", v), zap.String("type", keytype))
		}
	}
}

func (compare *Compare) CompareString(key string) {
	sourceexists := KeyExists(compare.Source, key)
	targetexists := KeyExists(compare.Target, key)

	if !sourceexists && !targetexists {
		return
	}

	sourceval := compare.Source.Get(key).Val()
	targetval := compare.Target.Get(key).Val()

	if sourceval != targetval {
		zaplogger.Info("Compare_Result", zap.Int("SourceDB", compare.SourceDB), zap.Int("TargetDB", compare.TargetDB), zap.String("key", key), zap.String("keytype", "string"), zap.String("Not_Equal_Reason", "Value not equal"))
		return
	}

	sourcettl := compare.Source.PTTL(key).Val().Milliseconds()
	targetttl := compare.Target.PTTL(key).Val().Milliseconds()

	sub := sourcettl - targetttl
	if math.Abs(float64(sub)) > compare.TTLDiff {
		zaplogger.Info("Compare_Result", zap.Int("SourceDB", compare.SourceDB), zap.Int("TargetDB", compare.TargetDB), zap.String("key", key), zap.String("keytype", "string"), zap.String("Not_Equal_Reason", "Key ttl difference is too large"), zap.Float64("TTLDiff", math.Abs(float64(sub))))
		return
	}

}

func (compare *Compare) CompareList(key string) {
	sourceexists := KeyExists(compare.Source, key)
	targetexists := KeyExists(compare.Target, key)

	if !sourceexists && !targetexists {
		return
	}

	sourcelen := compare.Source.LLen(key).Val()
	targetlen := compare.Target.LLen(key).Val()

	if sourcelen != targetlen {
		zaplogger.Info("Compare_Result", zap.Int("SourceDB", compare.SourceDB), zap.Int("TargetDB", compare.TargetDB), zap.String("key", key), zap.String("keytype", "list"), zap.String("Not_Equal_Reason", "Key length not equal"))
		return
	}

	sourcettl := compare.Source.PTTL(key).Val().Milliseconds()
	targetttl := compare.Target.PTTL(key).Val().Milliseconds()

	sub := sourcettl - targetttl
	if math.Abs(float64(sub)) > compare.TTLDiff {
		zaplogger.Info("Compare_Result", zap.Int("SourceDB", compare.SourceDB), zap.Int("TargetDB", compare.TargetDB), zap.String("key", key), zap.String("keytype", "string"), zap.String("Not_Equal_Reason", "Key ttl difference is too large"), zap.Float64("TTLDiff", math.Abs(float64(sub))))
		return
	}

	quotient := sourcelen / compare.BatchSize // integer division, decimals are truncated
	remainder := sourcelen % compare.BatchSize

	if quotient == 0 && remainder != 0 {
		sourcevaluse := compare.Source.LRange(key, int64(0), remainder).Val()
		targetvaluse := compare.Target.LRange(key, int64(0), remainder).Val()
		for k, v := range sourcevaluse {
			if targetvaluse[k] != v {
				zaplogger.Info("Compare_Result", zap.Int("SourceDB", compare.SourceDB), zap.Int("TargetDB", compare.TargetDB), zap.String("key", key), zap.String("keytype", "list"), zap.String("index", strconv.Itoa(k)), zap.String("Not_Equal_Reason", "value not equal"))
				return
			}
		}
	}

	if quotient != 0 {
		for i := int64(0); i < quotient; i++ {
			sourcevaluse := compare.Source.LRange(key, int64(0)+i*compare.BatchSize, (compare.BatchSize-1)+i*compare.BatchSize).Val()
			targetvaluse := compare.Target.LRange(key, int64(0)+i*compare.BatchSize, (compare.BatchSize-1)+i*compare.BatchSize).Val()
			for k, v := range sourcevaluse {
				if targetvaluse[k] != v {
					zaplogger.Info("Compare_Result", zap.Int("SourceDB", compare.SourceDB), zap.Int("TargetDB", compare.TargetDB), zap.String("key", key), zap.String("keytype", "list"), zap.String("index", strconv.Itoa(k)), zap.String("Not_Equal_Reason", "value not equal"))
					return
				}
			}
		}

		sourcevaluse := compare.Source.LRange(key, quotient*compare.BatchSize, quotient*compare.BatchSize).Val()
		targetvaluse := compare.Target.LRange(key, quotient*compare.BatchSize, quotient*compare.BatchSize).Val()

		for k, v := range sourcevaluse {
			if targetvaluse[k] != v {
				zaplogger.Info("Compare_Result", zap.Int("SourceDB", compare.SourceDB), zap.Int("TargetDB", compare.TargetDB), zap.String("key", key), zap.String("keytype", "list"), zap.String("index", strconv.Itoa(k)), zap.String("Not_Equal_Reason", "value not equal"))
				return
			}
		}
	}

	if remainder != 0 {
		sourcevaluse := compare.Source.LRange(key, quotient*compare.BatchSize, remainder+quotient*compare.BatchSize).Val()
		targetvaluse := compare.Target.LRange(key, quotient*compare.BatchSize, remainder+quotient*compare.BatchSize).Val()
		for k, v := range sourcevaluse {
			if targetvaluse[k] != v {
				zaplogger.Info("Compare_Result", zap.Int("SourceDB", compare.SourceDB), zap.Int("TargetDB", compare.TargetDB), zap.String("key", key), zap.String("keytype", "list"), zap.String("index", strconv.Itoa(k)), zap.String("Not_Equal_Reason", "value not equal"))
				return
			}
		}
	}

}

func (compare *Compare) CompareHash(key string) {

	sourceexists := KeyExists(compare.Source, key)
	targetexists := KeyExists(compare.Target, key)

	if !sourceexists && !targetexists {
		return
	}
	sourcelen := compare.Source.HLen(key).Val()
	targetlen := compare.Target.HLen(key).Val()

	if sourcelen != targetlen {
		zaplogger.Info("Compare_Result", zap.Int("SourceDB", compare.SourceDB), zap.Int("TargetDB", compare.TargetDB), zap.String("key", key), zap.String("keytype", "hash"), zap.String("Not_Equal_Reason", "Key length not equal"))
		return
	}

	sourcettl := compare.Source.PTTL(key).Val().Milliseconds()
	targetttl := compare.Target.PTTL(key).Val().Milliseconds()

	sub := targetttl - sourcettl
	if math.Abs(float64(sub)) > compare.TTLDiff {
		zaplogger.Info("Compare_Result", zap.Int("SourceDB", compare.SourceDB), zap.Int("TargetDB", compare.TargetDB), zap.String("key", key), zap.String("keytype", "hash"), zap.String("Not_Equal_Reason", "Key ttl difference is too large"), zap.Float64("TTLDiff", math.Abs(float64(sub))))
		return
	}

	cursor := uint64(0)
	for {
		sourceresult, c, err := compare.Source.HScan(key, cursor, "*", compare.BatchSize).Result()

		if err != nil {
			zaplogger.Info("Compare_Result", zap.Int("SourceDB", compare.SourceDB), zap.Int("TargetDB", compare.TargetDB), zap.String("key", key), zap.String("keytype", "hash"), zap.String("Not_Equal_Reason", "Source hscan error:"+err.Error()))
			return
		}

		for i := 0; i < len(sourceresult); i = i + 2 {

			if compare.Target.HGet(key, sourceresult[i]).Val() != sourceresult[i+1] {
				zaplogger.Info("Compare_Result", zap.Int("SourceDB", compare.SourceDB), zap.Int("TargetDB", compare.TargetDB), zap.String("key", key), zap.String("keytype", "hash"), zap.String("field", sourceresult[i]), zap.String("Not_Equal_Reason", "Field value not equal"))
				return
			}

		}
		cursor = c
		if c == uint64(0) {
			break
		}
	}

}

func (compare *Compare) CompareSet(key string) {

	sourceexists := KeyExists(compare.Source, key)
	targetexists := KeyExists(compare.Target, key)

	if !sourceexists && !targetexists {
		return
	}
	sourcelen := compare.Source.SCard(key).Val()
	targetlen := compare.Target.SCard(key).Val()
	if sourcelen != targetlen {
		zaplogger.Info("Compare_Result", zap.Int("SourceDB", compare.SourceDB), zap.Int("TargetDB", compare.TargetDB), zap.String("key", key), zap.String("keytype", "set"), zap.String("Not_Equal_Reason", "Key length not equal"))
		return
	}

	sourcettl := compare.Source.PTTL(key).Val().Milliseconds()
	targetttl := compare.Target.PTTL(key).Val().Milliseconds()

	sub := sourcettl - targetttl
	if math.Abs(float64(sub)) > compare.TTLDiff {

		zaplogger.Info("Compare_Result", zap.Int("SourceDB", compare.SourceDB), zap.Int("TargetDB", compare.TargetDB), zap.String("key", key), zap.String("keytype", "set"), zap.String("Not_Equal_Reason", "Key ttl difference is too large"), zap.Float64("TTLDiff", math.Abs(float64(sub))))
		return
	}

	cursor := uint64(0)
	for {
		sourceresult, c, err := compare.Source.SScan(key, cursor, "*", compare.BatchSize).Result()
		if err != nil {
			zaplogger.Info("Compare_Result", zap.Int("SourceDB", compare.SourceDB), zap.Int("TargetDB", compare.TargetDB), zap.String("key", key), zap.String("keytype", "set"), zap.String("Not_Equal_Reason", "Source sscan error:"+err.Error()))
			return
		}

		for _, v := range sourceresult {
			if !compare.Target.SIsMember(key, v).Val() {
				zaplogger.Info("Compare_Result", zap.Int("SourceDB", compare.SourceDB), zap.Int("TargetDB", compare.TargetDB), zap.String("key", key), zap.String("keytype", "set"), zap.String("member", v), zap.String("Not_Equal_Reason", "Source member not exists in Target "))
				return
			}
		}

		cursor = c
		if c == 0 {
			break
		}
	}
}

func (compare *Compare) CompareZset(key string) {
	sourceexists := KeyExists(compare.Source, key)
	targetexists := KeyExists(compare.Target, key)

	if !sourceexists && !targetexists {
		return
	}

	sourcelen := compare.Source.ZCard(key).Val()
	targetlen := compare.Target.ZCard(key).Val()
	if sourcelen != targetlen {
		zaplogger.Info("Compare_Result", zap.Int("SourceDB", compare.SourceDB), zap.Int("TargetDB", compare.TargetDB), zap.String("key", key), zap.String("keytype", "zset"), zap.String("Not_Equal_Reason", "Key length not equal"))
		return
	}

	sourcettl := compare.Source.PTTL(key).Val().Milliseconds()
	targetttl := compare.Target.PTTL(key).Val().Milliseconds()

	sub := sourcettl - targetttl
	if math.Abs(float64(sub)) > compare.TTLDiff {
		zaplogger.Info("Compare_Result", zap.Int("SourceDB", compare.SourceDB), zap.Int("TargetDB", compare.TargetDB), zap.String("key", key), zap.String("keytype", "zset"), zap.String("Not_Equal_Reason", "Key ttl difference is too large"), zap.Float64("TTLDiff", math.Abs(float64(sub))))
		return
	}

	cursor := uint64(0)
	for {
		sourceresult, c, err := compare.Source.ZScan(key, cursor, "*", compare.BatchSize).Result()
		if err != nil {
			zaplogger.Info("Compare_Result", zap.Int("SourceDB", compare.SourceDB), zap.Int("TargetDB", compare.TargetDB), zap.String("key", key), zap.String("keytype", "zset"), zap.String("Not_Equal_Reason", "Source zscan error:"+err.Error()))
			return
		}

		for i := 0; i < len(sourceresult); i = i + 2 {
			sourecemember := sourceresult[i]
			sourcescore, err := strconv.ParseFloat(sourceresult[i+1], 64)

			intcmd := compare.Target.ZRank(key, sourecemember)
			targetscore := compare.Target.ZScore(key, sourecemember).Val()

			if intcmd == nil {
				zaplogger.Info("Compare_Result", zap.Int("SourceDB", compare.SourceDB), zap.Int("TargetDB", compare.TargetDB), zap.String("key", key), zap.String("keytype", "zset"), zap.String("member", sourecemember), zap.String("Not_Equal_Reason", "Source member not exists in Target"))
				return
			}

			if err != nil {
				zaplogger.Sugar().Error(err)
				return
			}

			if targetscore != sourcescore {
				zaplogger.Info("Compare_Result", zap.Int("SourceDB", compare.SourceDB), zap.Int("TargetDB", compare.TargetDB), zap.String("key", key), zap.String("keytype", "zset"), zap.String("member", sourecemember), zap.String("Not_Equal_Reason", "member score not equal"))
				fmt.Println(targetscore, sourcescore)
				return
			}

		}

		cursor = c
		if c == 0 {
			break
		}
	}

}

func KeyExists(client *redis.Client, key string) bool {
	exists := client.Exists(key).Val()
	if exists == int64(1) {
		return true
	} else {
		return false
	}
}
