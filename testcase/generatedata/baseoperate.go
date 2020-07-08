//Package generatedata 用于生成测试过程中的数据

package generatedata

import (
	"fmt"
	"github.com/go-redis/redis/v7"
	"go.uber.org/zap"
	"math/rand"
	"strconv"
	"strings"
	"testcase/globalzap"
	"time"
)

var zaplogger = globalzap.GetLogger()

type OptType int32

const (
	BO_APPEND OptType = iota
	BO_BITOP
	BO_DECR_DECRBY
	BO_INCR_INCRBY_INCRBYFLOAT
	BO_MGET_MSETNX
	BO_PSETEX_SETEX
	BO_SET_SETNX
	BO_SETBIT
	BO_SETRANGE
	BO_HINCRBY_HINCRBYFLOAT
	BO_HSET_HMSET_HSETNX
	BO_LPUSH_LPOP_LPUSHX
	BO_LREM_LTRIM_LINSERT
	BO_RPUSH_RPUSHX_RPOP_RPOPLPUSH
	BO_BLPOP_BRPOP_BRPOPLPUSH
	BO_SADD_SMOVE_SPOP_SREM
	BO_SDIFFSTORE_SINTERSTORE_SUNIONSTORE
	BO_ZADD_ZINCRBY_ZPOPMAX_ZPOPMIN_ZREM
	BO_ZPOPMAX_ZPOPMIN
	BO_ZREMRANGEBYLEX_ZREMRANGEBYRANK_ZREMRANGEBYSCORE_ZUNIONSTORE_ZINTERSTORE
)

var BaseOptArray = []OptType{
	BO_DECR_DECRBY,
	BO_INCR_INCRBY_INCRBYFLOAT,
	BO_MGET_MSETNX,
	BO_PSETEX_SETEX,
	BO_SET_SETNX,
	BO_SETBIT,
	BO_SETRANGE,
	BO_HINCRBY_HINCRBYFLOAT,
	BO_HSET_HMSET_HSETNX,
	BO_LPUSH_LPOP_LPUSHX,
	BO_LREM_LTRIM_LINSERT,
	BO_RPUSH_RPUSHX_RPOP_RPOPLPUSH,
	BO_BLPOP_BRPOP_BRPOPLPUSH,
	BO_SADD_SMOVE_SPOP_SREM,
	BO_SDIFFSTORE_SINTERSTORE_SUNIONSTORE,
	BO_ZADD_ZINCRBY_ZPOPMAX_ZPOPMIN_ZREM,
	BO_ZPOPMAX_ZPOPMIN,
	BO_ZREMRANGEBYLEX_ZREMRANGEBYRANK_ZREMRANGEBYSCORE_ZUNIONSTORE_ZINTERSTORE,
}

func (ot OptType) String() string {
	switch ot {
	case BO_APPEND:
		return "BO_APPEND"
	case BO_BITOP:
		return "BO_BITOP"
	case BO_DECR_DECRBY:
		return "BO_DECR_DECRBY"
	case BO_INCR_INCRBY_INCRBYFLOAT:
		return "BO_INCR_INCRBY_INCRBYFLOAT"
	case BO_MGET_MSETNX:
		return "BO_MGET_MSETNX"
	case BO_PSETEX_SETEX:
		return "BO_PSETEX_SETEX"
	case BO_SET_SETNX:
		return "BO_SET_SETNX"
	case BO_SETBIT:
		return "BO_SETBIT"
	case BO_SETRANGE:
		return "BO_SETRANGE"
	case BO_HINCRBY_HINCRBYFLOAT:
		return "BO_HINCRBY_HINCRBYFLOAT"
	case BO_HSET_HMSET_HSETNX:
		return "BO_HSET_HMSET_HSETNX"
	case BO_LPUSH_LPOP_LPUSHX:
		return "BO_LPUSH_LPOP_LPUSHX"
	case BO_LREM_LTRIM_LINSERT:
		return "BO_LREM_LTRIM_LINSERT"
	case BO_RPUSH_RPUSHX_RPOP_RPOPLPUSH:
		return "BO_RPUSH_RPUSHX_RPOP_RPOPLPUSH"
	case BO_BLPOP_BRPOP_BRPOPLPUSH:
		return "BO_BLPOP_BRPOP_BRPOPLPUSH"
	case BO_SADD_SMOVE_SPOP_SREM:
		return "BO_SADD_SMOVE_SPOP_SREM"
	case BO_SDIFFSTORE_SINTERSTORE_SUNIONSTORE:
		return "BO_SDIFFSTORE_SINTERSTORE_SUNIONSTORE"
	case BO_ZADD_ZINCRBY_ZPOPMAX_ZPOPMIN_ZREM:
		return "BO_ZADD_ZINCRBY_ZPOPMAX_ZPOPMIN_ZREM"
	case BO_ZPOPMAX_ZPOPMIN:
		return "BO_ZPOPMAX_ZPOPMIN"
	case BO_ZREMRANGEBYLEX_ZREMRANGEBYRANK_ZREMRANGEBYSCORE_ZUNIONSTORE_ZINTERSTORE:
		return "BO_ZREMRANGEBYLEX_ZREMRANGEBYRANK_ZREMRANGEBYSCORE_ZUNIONSTORE_ZINTERSTORE"
	default:
		return "UNKNOWN"
	}
}

type BaseOpt struct {
	RedisConn    *redis.Conn
	RedisVersion string
	OptType      OptType
	KeySuffix    string
	Loopstep     int
	EXPIRE       time.Duration
	DB           int
}

func (bo *BaseOpt) ExecOpt() {

	switch bo.OptType.String() {
	case "BO_APPEND":
		bo.BO_APPEND()
	case "BO_BITOP":
		bo.BO_BITOP()
	case "BO_DECR_DECRBY":
		bo.BO_DECR_DECRBY()
	case "BO_INCR_INCRBY_INCRBYFLOAT":
		bo.BO_INCR_INCRBY_INCRBYFLOAT()
	case "BO_MGET_MSETNX":
		bo.BO_MGET_MSETNX()
	//case "BO_PSETEX_SETEX":
	//	bo.BO_PSETEX_SETEX()
	case "BO_SET_SETNX":
		bo.BO_SET_SETNX()
	case "BO_SETBIT":
		bo.BO_SETBIT()
	case "BO_SETRANGE":
		bo.BO_SETRANGE()
	case "BO_HINCRBY_HINCRBYFLOAT":
		bo.BO_HINCRBY_HINCRBYFLOAT()
	case "BO_HSET_HMSET_HSETNX":
		bo.BO_HSET_HMSET_HSETNX()
	case "BO_LPUSH_LPOP_LPUSHX":
		bo.BO_LPUSH_LPOP_LPUSHX()
	case "BO_LREM_LTRIM_LINSERT":
		bo.BO_LREM_LTRIM_LINSERT()
	case "BO_RPUSH_RPUSHX_RPOP_RPOPLPUSH":
		bo.BO_RPUSH_RPUSHX_RPOP_RPOPLPUSH()
	case "BO_BLPOP_BRPOP_BRPOPLPUSH":
		bo.BO_BLPOP_BRPOP_BRPOPLPUSH()
	case "BO_SADD_SMOVE_SPOP_SREM":
		bo.BO_SADD_SMOVE_SPOP_SREM()
	case "BO_SDIFFSTORE_SINTERSTORE_SUNIONSTORE":
		bo.BO_SDIFFSTORE_SINTERSTORE_SUNIONSTORE()
	case "BO_ZADD_ZINCRBY_ZPOPMAX_ZPOPMIN_ZREM":
		bo.BO_ZADD_ZINCRBY_ZPOPMAX_ZPOPMIN_ZREM()
	case "BO_ZPOPMAX_ZPOPMIN":
		bo.BO_ZPOPMAX_ZPOPMIN()
	case "BO_ZREMRANGEBYLEX_ZREMRANGEBYRANK_ZREMRANGEBYSCORE_ZUNIONSTORE_ZINTERSTORE":
		bo.BO_ZREMRANGEBYLEX_ZREMRANGEBYRANK_ZREMRANGEBYSCORE_ZUNIONSTORE_ZINTERSTORE()

	default:
		return
	}
}

// 比较目标库版本是否小于要求版本
func (bo BaseOpt) VersionLessThan(version string) bool {

	boverarray := strings.Split(bo.RedisVersion, ".")
	versionarry := strings.Split(version, ".")

	bover := ""
	ver := ""
	for i := 0; i < 3; i++ {
		if i < len(boverarray) {
			bover = bover + boverarray[i]
		} else {
			bover = bover + "0"
		}

		if i < len(ver) {
			ver = ver + versionarry[i]
		} else {
			ver = ver + "0"
		}
	}

	intbover, _ := strconv.Atoi(bover)
	intver, _ := strconv.Atoi(ver)

	if intbover < intver {
		return true
	}

	return false

}

//SELECT命令
func (bo *BaseOpt) BO_SELECT(db int) {
	_, err := bo.RedisConn.Select(db).Result()

	if err != nil {
		zaplogger.Sugar().Error(err)
		return
	}
	bo.DB = db
}

//APPEND 命令基本操作
//start version:2.0.0
func (bo *BaseOpt) BO_APPEND() {
	t1 := time.Now()
	appended := "append_" + bo.KeySuffix
	for i := 0; i < bo.Loopstep; i++ {
		bo.RedisConn.Append(appended, strconv.Itoa(i))
	}
	bo.RedisConn.Expire(appended, bo.EXPIRE)
	t2 := time.Now()
	zaplogger.Info("ExecCMD", zap.Int("db", bo.DB), zap.String("command", "APPEND"), zap.String("key", appended), zap.Int64("time", t2.Sub(t1).Milliseconds()))

}

//BITOP
//start version:2.6.0
func (bo *BaseOpt) BO_BITOP() {
	t1 := time.Now()
	strarry := []string{}
	opandkey := "opand_" + bo.KeySuffix
	oporkey := "opor_" + bo.KeySuffix
	opxorkey := "opxor_" + bo.KeySuffix
	opnotkey := "opnot_" + bo.KeySuffix
	for i := 0; i < bo.Loopstep; i++ {
		bitopkey := "bitop_" + bo.KeySuffix + strconv.Itoa(i)
		bo.RedisConn.Set(bitopkey, bitopkey, bo.EXPIRE)
		strarry = append(strarry, bitopkey)
	}

	bo.RedisConn.BitOpAnd(opandkey, strarry...)
	bo.RedisConn.BitOpOr(oporkey, strarry...)
	bo.RedisConn.BitOpXor(opxorkey, strarry...)
	bo.RedisConn.BitOpNot(opnotkey, strarry[0])
	bo.RedisConn.Expire(opandkey, bo.EXPIRE)
	bo.RedisConn.Expire(oporkey, bo.EXPIRE)
	bo.RedisConn.Expire(opxorkey, bo.EXPIRE)
	bo.RedisConn.Expire(opnotkey, bo.EXPIRE)
	t2 := time.Now()
	zaplogger.Info("ExecCMD", zap.Int("db", bo.DB), zap.String("command", "BITOP"), zap.Any("keys", []string{opandkey, oporkey, opxorkey, opnotkey}), zap.Duration("time", t2.Sub(t1)))
}

//DECR and DECRBY
func (bo *BaseOpt) BO_DECR_DECRBY() {
	t1 := time.Now()
	desckey := "desc_" + bo.KeySuffix
	bo.RedisConn.Set(desckey, bo.Loopstep, bo.EXPIRE)
	bo.RedisConn.Decr(desckey)
	bo.RedisConn.DecrBy(desckey, rand.Int63n(int64(bo.Loopstep)))
	t2 := time.Now()

	zaplogger.Info("ExecCMD", zap.Int("db", bo.DB), zap.String("command", "DECR_DECRBY"), zap.String("key", desckey), zap.Duration("time", t2.Sub(t1)))
}

//INCR and INCRBY and INCRBYFLOAT
func (bo *BaseOpt) BO_INCR_INCRBY_INCRBYFLOAT() {
	t1 := time.Now()
	incrkey := "incr_" + bo.KeySuffix
	bo.RedisConn.Set(incrkey, rand.Intn(bo.Loopstep), bo.EXPIRE)
	bo.RedisConn.Incr(incrkey)
	bo.RedisConn.IncrBy(incrkey, rand.Int63n(int64(bo.Loopstep)))
	bo.RedisConn.IncrByFloat(incrkey, rand.Float64()*float64(rand.Intn(bo.Loopstep)))
	t2 := time.Now()
	zaplogger.Info("ExecCMD", zap.Int("db", bo.DB), zap.String("command", "INCR_INCRBY_INCRBYFLOAT"), zap.String("key", incrkey), zap.Duration("time", t2.Sub(t1)))
}

//MSET and MSETNX
func (bo *BaseOpt) BO_MGET_MSETNX() {
	t1 := time.Now()
	msetarry := []string{}
	msetnxarry := []string{}

	msetkey := "mset_" + bo.KeySuffix
	msetnxkey := "msetnx_" + bo.KeySuffix

	for i := 0; i < bo.Loopstep; i++ {
		msetarry = append(msetarry, msetkey+strconv.Itoa(i))
		msetarry = append(msetarry, msetkey+strconv.Itoa(i))
		msetnxarry = append(msetnxarry, msetnxkey+strconv.Itoa(i))
		msetnxarry = append(msetnxarry, msetnxkey+strconv.Itoa(i))
	}

	bo.RedisConn.MSetNX(msetnxarry)
	bo.RedisConn.MSet(msetarry)
	bo.RedisConn.MSetNX(msetnxarry)

	for i := 0; i < bo.Loopstep; i++ {
		bo.RedisConn.Expire(msetkey+strconv.Itoa(i), bo.EXPIRE)
		bo.RedisConn.Expire(msetnxkey+strconv.Itoa(i), bo.EXPIRE)
	}

	t2 := time.Now()
	zaplogger.Info("ExecCMD", zap.Int("db", bo.DB), zap.String("command", "MGET_MSETNX"), zap.String("KeySuffix", bo.KeySuffix), zap.Duration("time", t2.Sub(t1)))
}

////PSETEX and SETEX
//func (bo *BaseOpt) BO_PSETEX_SETEX() {
//	t1 := time.Now()
//	psetexkey := "psetex_" + bo.KeySuffix
//	setexkey := "setex_" + bo.KeySuffix
//	bo.RedisConn.SetNX(setexkey, setexkey, bo.EXPIRE)
//	bo.RedisConn.
//	bo.RedisConn.Do("SETEX", setexkey, bo.EXPIRE.Seconds(), setexkey)
//	bo.RedisConn.Do("PSETEX", psetexkey, bo.EXPIRE.Milliseconds(), psetexkey)
//	t2 := time.Now()
//	zaplogger.Info("ExecCMD", zap.String("command", "MGET_MSETNX"), zap.String("KeySuffix", bo.KeySuffix), zap.Duration("time", t2.Sub(t1)))
//
//}

//SET and SETNX
func (bo *BaseOpt) BO_SET_SETNX() {
	t1 := time.Now()
	setkey := "set_" + bo.KeySuffix
	setnxkey := "setnx_" + bo.KeySuffix
	bo.RedisConn.Set(setkey, setkey, bo.EXPIRE)
	fmt.Println(bo.RedisConn.SetNX(setnxkey, setnxkey, bo.EXPIRE).Result())
	bo.RedisConn.SetNX(setnxkey, setkey, bo.EXPIRE)
	t2 := time.Now()
	zaplogger.Info("ExecCMD", zap.Int("db", bo.DB), zap.String("command", "SET_SETNX"), zap.String("KeySuffix", bo.KeySuffix), zap.Duration("time", t2.Sub(t1)))
}

//SETBIT
func (bo *BaseOpt) BO_SETBIT() {
	t1 := time.Now()
	setbitkey := "setbit_" + bo.KeySuffix
	bo.RedisConn.SetBit(setbitkey, rand.Int63n(int64(bo.Loopstep)), rand.Intn(bo.Loopstep))
	bo.RedisConn.Expire(setbitkey, bo.EXPIRE)

	t2 := time.Now()
	zaplogger.Info("ExecCMD", zap.Int("db", bo.DB), zap.String("command", "SETBIT"), zap.String("KeySuffix", bo.KeySuffix), zap.Duration("time", t2.Sub(t1)))
}

//SETRANGE
func (bo *BaseOpt) BO_SETRANGE() {
	t1 := time.Now()
	setrangekey := "setrange_" + bo.KeySuffix
	bo.RedisConn.Set(setrangekey, setrangekey, bo.EXPIRE)
	bo.RedisConn.SetRange(setrangekey, rand.Int63n(int64(bo.Loopstep)), strconv.Itoa(rand.Intn(bo.Loopstep)))
	t2 := time.Now()
	zaplogger.Info("ExecCMD", zap.Int("db", bo.DB), zap.String("command", "SETRANGE"), zap.String("KeySuffix", bo.KeySuffix), zap.Duration("time", t2.Sub(t1)))
}

//HINCRBY and HINCRBYFLOAT
func (bo *BaseOpt) BO_HINCRBY_HINCRBYFLOAT() {
	t1 := time.Now()
	hincrbykey := "hincrby_" + bo.KeySuffix
	hincrbyfloatkey := "hincrbyfloat_" + bo.KeySuffix
	for i := 0; i < bo.Loopstep; i++ {
		bo.RedisConn.HIncrBy(hincrbykey, hincrbykey+strconv.Itoa(rand.Intn(bo.Loopstep)), int64(rand.Intn(bo.Loopstep)))
		bo.RedisConn.HIncrByFloat(hincrbyfloatkey, hincrbyfloatkey+strconv.Itoa(rand.Intn(bo.Loopstep)), rand.Float64()*10)
	}
	t2 := time.Now()
	zaplogger.Info("ExecCMD", zap.Int("db", bo.DB), zap.String("command", "HINCRBY_HINCRBYFLOAT"), zap.String("KeySuffix", bo.KeySuffix), zap.Duration("time", t2.Sub(t1)))
}

//HSET HMSET HSETNX HDEL
func (bo *BaseOpt) BO_HSET_HMSET_HSETNX() {
	t1 := time.Now()
	hsetkey := "hset_" + bo.KeySuffix
	hmsetkey := "hmset_" + bo.KeySuffix
	fieldmap := make(map[string]interface{})

	for i := 0; i < bo.Loopstep; i++ {
		field := hmsetkey + strconv.Itoa(i)
		fieldmap[field] = field
	}

	for i := 0; i < bo.Loopstep; i++ {
		bo.RedisConn.HSet(hsetkey, hsetkey+strconv.Itoa(i), hsetkey+strconv.Itoa(i))
	}

	bo.RedisConn.HMSet(hmsetkey, fieldmap)

	//HSETNX
	for i := 0; i < bo.Loopstep; i++ {
		bo.RedisConn.HSetNX(hmsetkey, hmsetkey+strconv.Itoa(rand.Intn(bo.Loopstep*2)), hmsetkey+strconv.Itoa(i))
	}

	//HDEL
	for i := 0; i < bo.Loopstep; i++ {
		if rand.Intn(bo.Loopstep)%2 == 0 {
			bo.RedisConn.HDel(hmsetkey, hmsetkey+strconv.Itoa(rand.Intn(bo.Loopstep)))
		}
	}

	bo.RedisConn.Expire(hsetkey, bo.EXPIRE)
	bo.RedisConn.Expire(hmsetkey, bo.EXPIRE)
	t2 := time.Now()
	zaplogger.Info("ExecCMD", zap.Int("db", bo.DB), zap.String("command", "HSET_HMSET_HSETNX_HDEL"), zap.String("KeySuffix", bo.KeySuffix), zap.Duration("time", t2.Sub(t1)))
}

//LPUSH and LPOP and LPUSHX and LSET
func (bo *BaseOpt) BO_LPUSH_LPOP_LPUSHX() {
	t1 := time.Now()
	lpushkey := "lpush_" + bo.KeySuffix
	lpushxkey := "lpushx_" + bo.KeySuffix
	values := make([]interface{}, bo.Loopstep)
	for i := 0; i < len(values); i++ {
		values[i] = lpushkey + strconv.Itoa(i)
	}

	bo.RedisConn.LPush(lpushkey, values...)
	for i := 0; i < bo.Loopstep; i++ {
		if rand.Intn(bo.Loopstep)%2 != 0 {
			bo.RedisConn.LSet(lpushkey, int64(rand.Intn(2*bo.Loopstep)-bo.Loopstep), lpushkey+strconv.Itoa(i))
		}
	}

	for i := 0; i < bo.Loopstep; i++ {
		if rand.Intn(bo.Loopstep)%2 != 0 {
			bo.RedisConn.LPop(lpushkey)
		}
	}
	bo.RedisConn.LPushX(lpushxkey, values)
	bo.RedisConn.LPushX(lpushkey, values)

	bo.RedisConn.Expire(lpushkey, bo.EXPIRE)
	t2 := time.Now()
	zaplogger.Info("ExecCMD", zap.Int("db", bo.DB), zap.String("command", "LPUSH_LPOP_LPUSHX_LSET"), zap.String("KeySuffix", bo.KeySuffix), zap.Duration("time", t2.Sub(t1)))

}

//LREM and LTRIM and LINSERT
func (bo *BaseOpt) BO_LREM_LTRIM_LINSERT() {
	t1 := time.Now()
	lremkey := "lrem_" + bo.KeySuffix
	ltrimkey := "ltrim_" + bo.KeySuffix
	values := make([]interface{}, bo.Loopstep)
	for i := 0; i < len(values); i++ {
		values[i] = lremkey + strconv.Itoa(i)
	}
	bo.RedisConn.LPush(lremkey, values...)
	bo.RedisConn.LPush(ltrimkey, values...)

	for i := 0; i < bo.Loopstep; i++ {
		op := "BEFORE"
		if i%2 == 0 {
			op = "AFTER"
		} else {
			op = "BEFORE"
		}
		bo.RedisConn.LInsert(lremkey, op, lremkey+strconv.Itoa(rand.Intn(bo.Loopstep)), lremkey+strconv.Itoa(rand.Intn(bo.Loopstep)))
	}

	bo.RedisConn.LRem(lremkey, int64(rand.Intn(2*bo.Loopstep)-bo.Loopstep), lremkey+strconv.Itoa(rand.Intn(bo.Loopstep)))
	bo.RedisConn.LTrim(ltrimkey, int64(rand.Intn(2*bo.Loopstep)-bo.Loopstep), int64(rand.Intn(2*bo.Loopstep)-bo.Loopstep))
	bo.RedisConn.Expire(lremkey, bo.EXPIRE)
	bo.RedisConn.Expire(ltrimkey, bo.EXPIRE)
	t2 := time.Now()
	zaplogger.Info("ExecCMD", zap.Int("db", bo.DB), zap.String("command", "LREM_TRIM_LINSERT"), zap.String("KeySuffix", bo.KeySuffix), zap.Duration("time", t2.Sub(t1)))
}

//RPUSH RPUSHX RPOP RPOPLPUSH
func (bo *BaseOpt) BO_RPUSH_RPUSHX_RPOP_RPOPLPUSH() {
	t1 := time.Now()
	rpushkey := "rpush_" + bo.KeySuffix
	rpushxkey := "rpushx_" + bo.KeySuffix
	values := make([]interface{}, bo.Loopstep)
	for i := 0; i < len(values); i++ {
		values[i] = rpushkey + strconv.Itoa(i)
	}
	bo.RedisConn.RPush(rpushkey, values...)
	bo.RedisConn.RPushX(rpushxkey, values...)
	bo.RedisConn.RPushX(rpushkey, values...)

	for i := 0; i < bo.Loopstep; i++ {
		if rand.Intn(bo.Loopstep)%2 == 0 {
			bo.RedisConn.RPop(rpushkey)
		}
	}
	bo.RedisConn.RPopLPush(rpushxkey, rpushkey)
	bo.RedisConn.Expire(rpushkey, bo.EXPIRE)
	bo.RedisConn.Expire(rpushxkey, bo.EXPIRE)

	t2 := time.Now()
	zaplogger.Info("ExecCMD", zap.Int("db", bo.DB), zap.String("command", "BO_RPUSH_RPUSHX_RPOP_RPOPLPUSH"), zap.String("KeySuffix", bo.KeySuffix), zap.Duration("time", t2.Sub(t1)))
}

//BLPOP BRPOP BRPOPLPUSH
func (bo *BaseOpt) BO_BLPOP_BRPOP_BRPOPLPUSH() {
	t1 := time.Now()
	blpopkey := "blpop_" + bo.KeySuffix
	brpopkey := "brpop_" + bo.KeySuffix

	values := make([]interface{}, bo.Loopstep)
	for i := 0; i < len(values); i++ {
		values[i] = blpopkey + strconv.Itoa(i)
	}

	bo.RedisConn.RPush(blpopkey, values...)
	bo.RedisConn.RPush(brpopkey, values...)

	bo.RedisConn.BLPop(bo.EXPIRE, blpopkey)
	bo.RedisConn.BRPop(bo.EXPIRE, brpopkey)

	bo.RedisConn.BRPopLPush(blpopkey, brpopkey, bo.EXPIRE)
	bo.RedisConn.Expire(blpopkey, bo.EXPIRE)
	bo.RedisConn.Expire(brpopkey, bo.EXPIRE)
	t2 := time.Now()
	zaplogger.Info("ExecCMD", zap.Int("db", bo.DB), zap.String("command", "BO_BLPOP_BRPOP_BRPOPLPUSH"), zap.String("KeySuffix", bo.KeySuffix), zap.Duration("time", t2.Sub(t1)))
}

//SADD SMOVE SPOP SREM
func (bo *BaseOpt) BO_SADD_SMOVE_SPOP_SREM() {
	t1 := time.Now()
	saddkey := "sadd_" + bo.KeySuffix
	smovekey := "smove_" + bo.KeySuffix
	spopkey := "spop_" + bo.KeySuffix
	sremkey := "srem_" + bo.KeySuffix

	for i := 0; i < bo.Loopstep; i++ {
		bo.RedisConn.SAdd(saddkey, saddkey+strconv.Itoa(i))
		bo.RedisConn.SAdd(smovekey, smovekey+strconv.Itoa(i))
		bo.RedisConn.SAdd(spopkey, spopkey+strconv.Itoa(i))
		bo.RedisConn.SAdd(sremkey, sremkey+strconv.Itoa(i))
	}

	for i := 0; i < bo.Loopstep; i++ {
		if rand.Intn(bo.Loopstep)%2 == 0 {
			bo.RedisConn.SPop(spopkey)
		}
	}

	for i := 0; i < bo.Loopstep; i++ {
		if rand.Intn(bo.Loopstep)%2 == 0 {
			bo.RedisConn.SRem(sremkey, sremkey+strconv.Itoa(rand.Intn(bo.Loopstep)))
		}
	}

	for i := 0; i < bo.Loopstep; i++ {
		if rand.Intn(bo.Loopstep)%2 == 0 {
			bo.RedisConn.SMove(saddkey, smovekey, saddkey+strconv.Itoa(i))
		}
	}

	bo.RedisConn.Del(saddkey)
	bo.RedisConn.Expire(smovekey, bo.EXPIRE)
	bo.RedisConn.Expire(spopkey, bo.EXPIRE)
	bo.RedisConn.Expire(sremkey, bo.EXPIRE)

	t2 := time.Now()
	zaplogger.Info("ExecCMD", zap.Int("db", bo.DB), zap.String("command", "BO_RPUSH_RPUSHX_RPOP_RPOPLPUSH"), zap.String("KeySuffix", bo.KeySuffix), zap.Duration("time", t2.Sub(t1)))

}

//SDIFFSTORE SINTERSTORE SUNIONSTORE
func (bo *BaseOpt) BO_SDIFFSTORE_SINTERSTORE_SUNIONSTORE() {
	t1 := time.Now()
	sdiff1 := "sdiff1_" + bo.KeySuffix
	sdiff2 := "sdiff2_" + bo.KeySuffix
	sdiffstore := "sdiffsotre_" + bo.KeySuffix
	sinterstore := "sintersotre_" + bo.KeySuffix
	sunionstore := "sunionstore_" + bo.KeySuffix

	for i := 0; i < bo.Loopstep; i++ {
		bo.RedisConn.SAdd(sdiff1, bo.KeySuffix+strconv.Itoa(rand.Intn(2*bo.Loopstep)))
		bo.RedisConn.SAdd(sdiff2, bo.KeySuffix+strconv.Itoa(rand.Intn(2*bo.Loopstep)))
	}

	bo.RedisConn.SDiffStore(sdiffstore, sdiff1, sdiff2)
	bo.RedisConn.SInterStore(sinterstore, sdiff1, sdiff2)
	bo.RedisConn.SUnionStore(sunionstore, sdiff1, sdiff2)

	bo.RedisConn.Expire(sdiffstore, bo.EXPIRE)
	bo.RedisConn.Expire(sinterstore, bo.EXPIRE)
	bo.RedisConn.Expire(sunionstore, bo.EXPIRE)

	bo.RedisConn.Del(sdiff1, sdiff2)

	t2 := time.Now()
	zaplogger.Info("ExecCMD", zap.Int("db", bo.DB), zap.String("command", "BO_SDIFFSTORE_SINTERSTORE_SUNIONSTORE"), zap.String("KeySuffix", bo.KeySuffix), zap.Duration("time", t2.Sub(t1)))
}

//ZADD ZINCRBY ZREM
//start version:1.2.0
func (bo BaseOpt) BO_ZADD_ZINCRBY_ZPOPMAX_ZPOPMIN_ZREM() {
	t1 := time.Now()

	zaddkey := "zadd_" + bo.KeySuffix
	zincrby := "zincrby_" + bo.KeySuffix
	zrem := "zrem_" + bo.KeySuffix

	for i := 0; i < bo.Loopstep; i++ {
		z := redis.Z{
			Score:  float64(i),
			Member: zaddkey + strconv.Itoa(i),
		}
		bo.RedisConn.ZAdd(zaddkey, &z)
		bo.RedisConn.ZAdd(zincrby, &z)
		bo.RedisConn.ZAdd(zrem, &z)

	}

	for i := 0; i < bo.Loopstep; i++ {
		if rand.Intn(bo.Loopstep)%2 == 0 {
			bo.RedisConn.ZIncrBy(zincrby, float64(rand.Intn(2*bo.Loopstep)-bo.Loopstep), zaddkey+strconv.Itoa(rand.Intn(bo.Loopstep)))
			bo.RedisConn.ZRem(zrem, zaddkey+strconv.Itoa(rand.Intn(bo.Loopstep)))
		}
	}

	bo.RedisConn.Expire(zincrby, bo.EXPIRE)
	bo.RedisConn.Expire(zrem, bo.EXPIRE)
	t2 := time.Now()

	zaplogger.Info("ExecCMD", zap.Int("db", bo.DB), zap.String("command", "BO_ZADD_ZINCRBY_ZPOPMAX_ZPOPMIN_ZREM"), zap.String("KeySuffix", bo.KeySuffix), zap.Duration("time", t2.Sub(t1)))
}

//ZPOPMAX ZPOPMIN
//start version:5.0
func (bo BaseOpt) BO_ZPOPMAX_ZPOPMIN() {
	t1 := time.Now()

	zpopmax := "zpopmax_" + bo.KeySuffix
	zpopmin := "zpopmin_" + bo.KeySuffix

	for i := 0; i < bo.Loopstep; i++ {
		z := redis.Z{
			Score:  float64(i),
			Member: zpopmax + strconv.Itoa(i),
		}
		bo.RedisConn.ZAdd(zpopmax, &z)
		bo.RedisConn.ZAdd(zpopmin, &z)
	}

	for i := 0; i < bo.Loopstep; i++ {
		if rand.Intn(bo.Loopstep)%2 == 0 {
			bo.RedisConn.ZPopMax(zpopmax, 1)
			bo.RedisConn.ZPopMin(zpopmin, 1)
		}
	}

	bo.RedisConn.Expire(zpopmax, bo.EXPIRE)
	bo.RedisConn.Expire(zpopmin, bo.EXPIRE)
	t2 := time.Now()

	zaplogger.Info("ExecCMD", zap.Int("db", bo.DB), zap.String("command", "BO_ZPOPMAX_ZPOPMIN"), zap.String("KeySuffix", bo.KeySuffix), zap.Duration("time", t2.Sub(t1)))
}

//ZREMRANGEBYLEX ZREMRANGEBYRANK ZREMRANGEBYSCORE  ZUNIONSTORE ZINTERSTORE
//start version:2.8.9
func (bo *BaseOpt) BO_ZREMRANGEBYLEX_ZREMRANGEBYRANK_ZREMRANGEBYSCORE_ZUNIONSTORE_ZINTERSTORE() {
	t1 := time.Now()

	zremrangebylex := "zremrangebylex_" + bo.KeySuffix
	zremrangebyrank := "zremrangebyrank_" + bo.KeySuffix
	zremrangebyscore := "zremrangebyscore_" + bo.KeySuffix
	zinterstore := "zinterstore_" + bo.KeySuffix
	zunionstore := "zunionstore_" + bo.KeySuffix

	for i := 0; i < bo.Loopstep; i++ {
		z := redis.Z{
			Score:  float64(i),
			Member: zremrangebylex + strconv.Itoa(i),
		}
		bo.RedisConn.ZAdd(zremrangebylex, &z)
		bo.RedisConn.ZAdd(zremrangebyrank, &z)
	}

	bo.RedisConn.ZRemRangeByLex(zremrangebylex, zremrangebylex+strconv.Itoa(0), zremrangebylex+strconv.Itoa(rand.Intn(bo.Loopstep-1)))
	bo.RedisConn.ZRemRangeByRank(zremrangebyrank, int64(rand.Intn(2*bo.Loopstep)-bo.Loopstep), int64(rand.Intn(2*bo.Loopstep)-bo.Loopstep))
	bo.RedisConn.ZRemRangeByScore(zremrangebyscore, strconv.Itoa(rand.Intn(bo.Loopstep)), strconv.Itoa(rand.Intn(bo.Loopstep)))

	zstore := redis.ZStore{
		Keys:    []string{zremrangebylex, zremrangebyrank, zremrangebyscore},
		Weights: []float64{float64(rand.Intn(bo.Loopstep))},
	}

	bo.RedisConn.ZInterStore(zinterstore, &zstore)
	bo.RedisConn.ZUnionStore(zunionstore, &zstore)

	bo.RedisConn.Expire(zremrangebylex, bo.EXPIRE)
	bo.RedisConn.Expire(zremrangebyrank, bo.EXPIRE)
	bo.RedisConn.Expire(zremrangebyscore, bo.EXPIRE)
	bo.RedisConn.Expire(zinterstore, bo.EXPIRE)
	bo.RedisConn.Expire(zunionstore, bo.EXPIRE)

	t2 := time.Now()
	zaplogger.Info("ExecCMD", zap.Int("db", bo.DB), zap.String("command", "BO_ZREMRANGEBYLEX_ZREMRANGEBYRANK_ZREMRANGEBYSCORE_ZUNIONSTORE_ZINTERSTORE"), zap.String("KeySuffix", bo.KeySuffix), zap.Duration("time", t2.Sub(t1)))

}
