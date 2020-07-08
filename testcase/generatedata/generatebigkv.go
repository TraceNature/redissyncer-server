package generatedata

import (
	"github.com/go-redis/redis/v7"
	"go.uber.org/zap"
	"math/rand"
	"strconv"
	"sync"
	"time"
)

type GenBigKV struct {
	RedisConn   *redis.Conn
	KeySuffix   string
	Loopstep    int //生成数据的循环次数
	EXPIRE      time.Duration
	DB          int
	ValuePrefix string
}

func (gbkv *GenBigKV) GenBigHash() string {
	t1 := time.Now()
	key := "BigHash_" + gbkv.KeySuffix
	for i := 0; i < gbkv.Loopstep; i++ {
		gbkv.RedisConn.HSet(key, key+strconv.Itoa(i), gbkv.ValuePrefix+strconv.Itoa(i))
	}
	gbkv.RedisConn.Expire(key, gbkv.EXPIRE)
	t2 := time.Now()

	zaplogger.Info("GenBigKV", zap.Int("db", gbkv.DB), zap.String("keytype", "hash"), zap.String("key", key), zap.String("duration", t2.Sub(t1).String()))
	return key
}

func (gbkv *GenBigKV) GenBigList() string {
	t1 := time.Now()
	key := "BigList_" + gbkv.KeySuffix
	for i := 0; i < gbkv.Loopstep; i++ {
		gbkv.RedisConn.LPush(key, gbkv.ValuePrefix+strconv.Itoa(i))
	}
	gbkv.RedisConn.Expire(key, gbkv.EXPIRE)
	t2 := time.Now()
	zaplogger.Info("GenBigKV", zap.Int("db", gbkv.DB), zap.String("keytype", "list"), zap.String("key", key), zap.String("duration", t2.Sub(t1).String()))

	return key
}

func (gbkv *GenBigKV) GenBigSet() string {
	t1 := time.Now()
	key := "BigSet_" + gbkv.KeySuffix
	for i := 0; i < gbkv.Loopstep; i++ {
		gbkv.RedisConn.SAdd(key, gbkv.ValuePrefix+strconv.Itoa(i))
	}
	gbkv.RedisConn.Expire(key, gbkv.EXPIRE)
	t2 := time.Now()
	zaplogger.Info("GenBigKV", zap.Int("db", gbkv.DB), zap.String("keytype", "set"), zap.String("key", key), zap.String("duration", t2.Sub(t1).String()))
	return key
}

func (gbkv *GenBigKV) GenBigZset() string {
	t1 := time.Now()
	key := "BigZset_" + gbkv.KeySuffix
	for i := 0; i < gbkv.Loopstep; i++ {
		member := &redis.Z{
			Score:  rand.Float64(),
			Member: gbkv.ValuePrefix + strconv.Itoa(i),
		}
		gbkv.RedisConn.ZAdd(key, member)
	}
	gbkv.RedisConn.Expire(key, gbkv.EXPIRE)
	t2 := time.Now()
	zaplogger.Info("GenBigKV", zap.Int("db", gbkv.DB), zap.String("keytype", "zset"), zap.String("key", key), zap.String("duration", t2.Sub(t1).String()))
	return key
}

func (gbkv *GenBigKV) GenBigString() {
	t1 := time.Now()
	key := "BigString_" + gbkv.KeySuffix
	for i := 0; i < gbkv.Loopstep; i++ {
		gbkv.RedisConn.Set(key+strconv.Itoa(i), gbkv.ValuePrefix+strconv.Itoa(i), gbkv.EXPIRE)
	}
	t2 := time.Now()
	zaplogger.Info("GenBigKV", zap.Int("db", gbkv.DB), zap.String("keytype", "string"), zap.String("keyprefix", key), zap.String("duration", t2.Sub(t1).String()))
}

func (gbkv *GenBigKV) GenerateBaseDataParallel(client *redis.Client) map[string]string {

	zaplogger.Sugar().Info("Generate Base data Beging...")
	bigkvmap := make(map[string]string)
	wg := sync.WaitGroup{}

	wg.Add(1)
	go func() {
		newgbkv := new(GenBigKV)
		newgbkv.RedisConn = client.Conn()
		newgbkv.KeySuffix = gbkv.KeySuffix
		newgbkv.Loopstep = gbkv.Loopstep
		newgbkv.EXPIRE = gbkv.EXPIRE
		newgbkv.DB = gbkv.DB
		newgbkv.ValuePrefix = gbkv.ValuePrefix
		bigkvmap[newgbkv.GenBigHash()] = "Hash"
		wg.Done()
	}()

	wg.Add(1)
	go func() {
		newgbkv := new(GenBigKV)
		newgbkv.RedisConn = client.Conn()
		newgbkv.KeySuffix = gbkv.KeySuffix
		newgbkv.Loopstep = gbkv.Loopstep
		newgbkv.EXPIRE = gbkv.EXPIRE
		newgbkv.ValuePrefix = gbkv.ValuePrefix
		newgbkv.DB = gbkv.DB
		bigkvmap[newgbkv.GenBigList()] = "List"
		wg.Done()
	}()

	wg.Add(1)
	go func() {
		newgbkv := new(GenBigKV)
		newgbkv.RedisConn = client.Conn()
		newgbkv.KeySuffix = gbkv.KeySuffix
		newgbkv.Loopstep = gbkv.Loopstep
		newgbkv.EXPIRE = gbkv.EXPIRE
		newgbkv.ValuePrefix = gbkv.ValuePrefix
		newgbkv.DB = gbkv.DB
		bigkvmap[newgbkv.GenBigSet()] = "Set"
		wg.Done()
	}()

	wg.Add(1)
	go func() {
		newgbkv := new(GenBigKV)
		newgbkv.RedisConn = client.Conn()
		newgbkv.KeySuffix = gbkv.KeySuffix
		newgbkv.Loopstep = gbkv.Loopstep
		newgbkv.EXPIRE = gbkv.EXPIRE
		newgbkv.ValuePrefix = gbkv.ValuePrefix
		newgbkv.DB = gbkv.DB
		bigkvmap[newgbkv.GenBigZset()] = "Zset"
		wg.Done()
	}()

	wg.Add(1)
	go func() {
		newgbkv := new(GenBigKV)
		newgbkv.RedisConn = client.Conn()
		newgbkv.KeySuffix = gbkv.KeySuffix
		newgbkv.Loopstep = gbkv.Loopstep
		newgbkv.EXPIRE = gbkv.EXPIRE
		newgbkv.ValuePrefix = gbkv.ValuePrefix
		newgbkv.DB = gbkv.DB
		newgbkv.GenBigString()
		wg.Done()
	}()
	wg.Wait()
	return bigkvmap
}
