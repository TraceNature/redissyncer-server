package generatedata

import (
	"github.com/go-redis/redis/v7"
	"go.uber.org/zap"
	"math/rand"
	"strconv"
	"sync"
	"time"
)

type GenBigKVCluster struct {
	RedisClusterClient *redis.ClusterClient
	KeySuffix          string
	Loopstep           int //生成数据的循环次数
	EXPIRE             time.Duration
	DB                 int
	ValuePrefix        string
}

func (gbkv *GenBigKVCluster) GenBigHash() string {
	t1 := time.Now()
	key := "BigHash_" + gbkv.KeySuffix
	for i := 0; i < gbkv.Loopstep; i++ {
		gbkv.RedisClusterClient.HSet(key, key+strconv.Itoa(i), gbkv.ValuePrefix+strconv.Itoa(i))
	}
	gbkv.RedisClusterClient.Expire(key, gbkv.EXPIRE)
	t2 := time.Now()

	zaplogger.Info("GenBigKV", zap.Int("db", gbkv.DB), zap.String("keytype", "hash"), zap.String("key", key), zap.String("duration", t2.Sub(t1).String()))
	return key
}

func (gbkv *GenBigKVCluster) GenBigList() string {
	t1 := time.Now()
	key := "BigList_" + gbkv.KeySuffix
	for i := 0; i < gbkv.Loopstep; i++ {
		gbkv.RedisClusterClient.LPush(key, gbkv.ValuePrefix+strconv.Itoa(i))
	}
	gbkv.RedisClusterClient.Expire(key, gbkv.EXPIRE)
	t2 := time.Now()
	zaplogger.Info("GenBigKV", zap.Int("db", gbkv.DB), zap.String("keytype", "list"), zap.String("key", key), zap.String("duration", t2.Sub(t1).String()))

	return key
}

func (gbkv *GenBigKVCluster) GenBigSet() string {
	t1 := time.Now()
	key := "BigSet_" + gbkv.KeySuffix
	for i := 0; i < gbkv.Loopstep; i++ {
		gbkv.RedisClusterClient.SAdd(key, gbkv.ValuePrefix+strconv.Itoa(i))
	}
	gbkv.RedisClusterClient.Expire(key, gbkv.EXPIRE)
	t2 := time.Now()
	zaplogger.Info("GenBigKV", zap.Int("db", gbkv.DB), zap.String("keytype", "set"), zap.String("key", key), zap.String("duration", t2.Sub(t1).String()))
	return key
}

func (gbkv *GenBigKVCluster) GenBigZset() string {
	t1 := time.Now()
	key := "BigZset_" + gbkv.KeySuffix
	for i := 0; i < gbkv.Loopstep; i++ {
		member := &redis.Z{
			Score:  rand.Float64(),
			Member: gbkv.ValuePrefix + strconv.Itoa(i),
		}
		gbkv.RedisClusterClient.ZAdd(key, member)
	}
	gbkv.RedisClusterClient.Expire(key, gbkv.EXPIRE)
	t2 := time.Now()
	zaplogger.Info("GenBigKV", zap.Int("db", gbkv.DB), zap.String("keytype", "zset"), zap.String("key", key), zap.String("duration", t2.Sub(t1).String()))
	return key
}

func (gbkv *GenBigKVCluster) GenBigString() {
	t1 := time.Now()
	key := "BigString_" + gbkv.KeySuffix
	for i := 0; i < gbkv.Loopstep; i++ {
		gbkv.RedisClusterClient.Set(key+strconv.Itoa(i), gbkv.ValuePrefix+strconv.Itoa(i), gbkv.EXPIRE)
	}
	t2 := time.Now()
	zaplogger.Info("GenBigKV", zap.Int("db", gbkv.DB), zap.String("keytype", "string"), zap.String("keyprefix", key), zap.String("duration", t2.Sub(t1).String()))
}

func (gbkv *GenBigKVCluster) GenerateBaseDataParallelCluster() map[string]string {

	zaplogger.Sugar().Info("Generate Base data Beging...")
	bigkvmap := make(map[string]string)
	wg := sync.WaitGroup{}

	wg.Add(1)
	go func() {
		bigkvmap[gbkv.GenBigHash()] = "Hash"
		wg.Done()
	}()

	wg.Add(1)
	go func() {
		bigkvmap[gbkv.GenBigList()] = "List"
		wg.Done()
	}()

	wg.Add(1)
	go func() {
		bigkvmap[gbkv.GenBigSet()] = "Set"
		wg.Done()
	}()

	wg.Add(1)
	go func() {
		bigkvmap[gbkv.GenBigZset()] = "Zset"
		wg.Done()
	}()

	wg.Add(1)
	go func() {
		gbkv.GenBigString()
		wg.Done()
	}()
	wg.Wait()
	return bigkvmap
}
