//Package generatedata 用于生成测试过程中的数据

package generatedata

import (
	"context"
	"github.com/go-redis/redis/v7"
	"go.uber.org/zap"
	"math/rand"
	"strconv"
	"strings"
	"testcase/globalzap"
	"time"
)

var zaplogger = globalzap.GetLogger()

type OptSingle struct {
	RedisConn    *redis.Conn
	RedisVersion string
	OptType      OptType
	KeySuffix    string
	Loopstep     int
	EXPIRE       time.Duration
	DB           int
}

func (optsingle *OptSingle) ExecOpt() {

	switch optsingle.OptType.String() {
	case "BO_APPEND":
		optsingle.BO_APPEND()
	case "BO_BITOP":
		optsingle.BO_BITOP()
	case "BO_DECR_DECRBY":
		optsingle.BO_DECR_DECRBY()
	case "BO_INCR_INCRBY_INCRBYFLOAT":
		optsingle.BO_INCR_INCRBY_INCRBYFLOAT()
	case "BO_MSET_MSETNX":
		optsingle.BO_MSET_MSETNX()
	//case "BO_PSETEX_SETEX":
	//	optsingle.BO_PSETEX_SETEX()
	case "BO_PFADD":
		optsingle.BO_PFADD()
	case "BO_PFMERGE":
		optsingle.BO_PFMERGE()
	case "BO_SET_SETNX":
		optsingle.BO_SET_SETNX()
	case "BO_SETBIT":
		optsingle.BO_SETBIT()
	case "BO_SETRANGE":
		optsingle.BO_SETRANGE()
	case "BO_HINCRBY_HINCRBYFLOAT":
		optsingle.BO_HINCRBY_HINCRBYFLOAT()
	case "BO_HSET_HMSET_HSETNX":
		optsingle.BO_HSET_HMSET_HSETNX()
	case "BO_LPUSH_LPOP_LPUSHX":
		optsingle.BO_LPUSH_LPOP_LPUSHX()
	case "BO_LREM_LTRIM_LINSERT":
		optsingle.BO_LREM_LTRIM_LINSERT()
	case "BO_RPUSH_RPUSHX_RPOP_RPOPLPUSH":
		optsingle.BO_RPUSH_RPUSHX_RPOP_RPOPLPUSH()
	case "BO_BLPOP_BRPOP_BRPOPLPUSH":
		optsingle.BO_BLPOP_BRPOP_BRPOPLPUSH()
	case "BO_SADD_SMOVE_SPOP_SREM":
		optsingle.BO_SADD_SMOVE_SPOP_SREM()
	case "BO_SDIFFSTORE_SINTERSTORE_SUNIONSTORE":
		optsingle.BO_SDIFFSTORE_SINTERSTORE_SUNIONSTORE()
	case "BO_ZADD_ZINCRBY_ZPOPMAX_ZPOPMIN_ZREM":
		optsingle.BO_ZADD_ZINCRBY_ZPOPMAX_ZPOPMIN_ZREM()
	case "BO_ZPOPMAX_ZPOPMIN":
		optsingle.BO_ZPOPMAX_ZPOPMIN()
	case "BO_ZREMRANGEBYLEX_ZREMRANGEBYRANK_ZREMRANGEBYSCORE":
		optsingle.BO_ZREMRANGEBYLEX_ZREMRANGEBYRANK_ZREMRANGEBYSCORE()
	case "BO_UNIONSTORE_ZINTERSTORE":
		optsingle.BO_ZUNIONSTORE_ZINTERSTORE()
	default:
		return
	}

}

// 比较目标库版本是否小于要求版本
func (optsingle OptSingle) VersionLessThan(version string) bool {

	boverarray := strings.Split(optsingle.RedisVersion, ".")
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
func (optsingle *OptSingle) BO_SELECT(db int) {

	_, err := optsingle.RedisConn.Select(db).Result()
	if err != nil {
		zaplogger.Sugar().Error(err)
		return
	}
	optsingle.DB = db
}

//APPEND 命令基本操作
//start version:2.0.0
func (optsingle *OptSingle) BO_APPEND() {
	t1 := time.Now()
	appended := "append_" + optsingle.KeySuffix
	for i := 0; i < optsingle.Loopstep; i++ {
		optsingle.RedisConn.Append(appended, strconv.Itoa(i))
	}
	optsingle.RedisConn.Expire(appended, optsingle.EXPIRE)
	t2 := time.Now()
	zaplogger.Info("ExecCMD", zap.Int("db", optsingle.DB), zap.String("command", "APPEND"), zap.String("key", appended), zap.Int64("time", t2.Sub(t1).Milliseconds()))

}

//BITOP
//start version:2.6.0
func (optsingle *OptSingle) BO_BITOP() {
	t1 := time.Now()
	strarry := []string{}
	opandkey := "opand_" + optsingle.KeySuffix
	oporkey := "opor_" + optsingle.KeySuffix
	opxorkey := "opxor_" + optsingle.KeySuffix
	opnotkey := "opnot_" + optsingle.KeySuffix
	for i := 0; i < optsingle.Loopstep; i++ {
		bitopkey := "bitop_" + optsingle.KeySuffix + strconv.Itoa(i)
		optsingle.RedisConn.Set(bitopkey, bitopkey, optsingle.EXPIRE)
		strarry = append(strarry, bitopkey)
	}

	optsingle.RedisConn.BitOpAnd(opandkey, strarry...)
	optsingle.RedisConn.BitOpOr(oporkey, strarry...)
	optsingle.RedisConn.BitOpXor(opxorkey, strarry...)
	optsingle.RedisConn.BitOpNot(opnotkey, strarry[0])
	optsingle.RedisConn.Expire(opandkey, optsingle.EXPIRE)
	optsingle.RedisConn.Expire(oporkey, optsingle.EXPIRE)
	optsingle.RedisConn.Expire(opxorkey, optsingle.EXPIRE)
	optsingle.RedisConn.Expire(opnotkey, optsingle.EXPIRE)
	t2 := time.Now()
	zaplogger.Info("ExecCMD", zap.Int("db", optsingle.DB), zap.String("command", "BITOP"), zap.Any("keys", []string{opandkey, oporkey, opxorkey, opnotkey}), zap.Duration("time", t2.Sub(t1)))
}

//DECR and DECRBY
func (optsingle *OptSingle) BO_DECR_DECRBY() {
	t1 := time.Now()
	desckey := "desc_" + optsingle.KeySuffix
	optsingle.RedisConn.Set(desckey, optsingle.Loopstep, optsingle.EXPIRE)
	optsingle.RedisConn.Decr(desckey)
	optsingle.RedisConn.DecrBy(desckey, rand.Int63n(int64(optsingle.Loopstep)))
	t2 := time.Now()

	zaplogger.Info("ExecCMD", zap.Int("db", optsingle.DB), zap.String("command", "DECR_DECRBY"), zap.String("key", desckey), zap.Duration("time", t2.Sub(t1)))
}

//INCR and INCRBY and INCRBYFLOAT
func (optsingle *OptSingle) BO_INCR_INCRBY_INCRBYFLOAT() {
	t1 := time.Now()
	incrkey := "incr_" + optsingle.KeySuffix
	optsingle.RedisConn.Set(incrkey, rand.Intn(optsingle.Loopstep), optsingle.EXPIRE)
	optsingle.RedisConn.Incr(incrkey)
	optsingle.RedisConn.IncrBy(incrkey, rand.Int63n(int64(optsingle.Loopstep)))
	optsingle.RedisConn.IncrByFloat(incrkey, rand.Float64()*float64(rand.Intn(optsingle.Loopstep)))
	t2 := time.Now()
	zaplogger.Info("ExecCMD", zap.Int("db", optsingle.DB), zap.String("command", "INCR_INCRBY_INCRBYFLOAT"), zap.String("key", incrkey), zap.Duration("time", t2.Sub(t1)))
}

//MSET and MSETNX
func (optsingle *OptSingle) BO_MSET_MSETNX() {
	t1 := time.Now()
	msetarry := []string{}
	msetnxarry := []string{}

	msetkey := "mset_" + optsingle.KeySuffix
	msetnxkey := "msetnx_" + optsingle.KeySuffix

	for i := 0; i < optsingle.Loopstep; i++ {
		msetarry = append(msetarry, msetkey+strconv.Itoa(i))
		msetarry = append(msetarry, msetkey+strconv.Itoa(i))
		msetnxarry = append(msetnxarry, msetnxkey+strconv.Itoa(i))
		msetnxarry = append(msetnxarry, msetnxkey+strconv.Itoa(i))
	}

	optsingle.RedisConn.MSetNX(msetnxarry)
	optsingle.RedisConn.MSet(msetarry)
	optsingle.RedisConn.MSetNX(msetnxarry)

	for i := 0; i < optsingle.Loopstep; i++ {
		optsingle.RedisConn.Expire(msetkey+strconv.Itoa(i), optsingle.EXPIRE)
		optsingle.RedisConn.Expire(msetnxkey+strconv.Itoa(i), optsingle.EXPIRE)
	}

	t2 := time.Now()
	zaplogger.Info("ExecCMD", zap.Int("db", optsingle.DB), zap.String("command", "MGET_MSETNX"), zap.String("KeySuffix", optsingle.KeySuffix), zap.Duration("time", t2.Sub(t1)))
}

////PSETEX and SETEX
//func (bo *OptSingle) BO_PSETEX_SETEX() {
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

//PFADD
func (optsingle *OptSingle) BO_PFADD() {
	t1 := time.Now()
	pfaddkey := "pfadd_" + optsingle.KeySuffix
	rand.Seed(time.Now().UnixNano())
	for i := 0; i < optsingle.Loopstep; i++ {
		optsingle.RedisConn.PFAdd(pfaddkey, rand.Float64()*float64(rand.Int()))
	}
	t2 := time.Now()
	zaplogger.Info("ExecCMD", zap.Int("db", optsingle.DB), zap.String("command", "BO_PFADD"), zap.String("KeySuffix", optsingle.KeySuffix), zap.Duration("time", t2.Sub(t1)))
}

//PFMERGE
func (optsingle *OptSingle) BO_PFMERGE() {
	t1 := time.Now()
	pfaddkey := "pfadd_" + optsingle.KeySuffix
	pfmergekey := "pfmerge_" + optsingle.KeySuffix
	pfaddkeyarray := []string{}
	rand.Seed(time.Now().UnixNano())
	for i := 0; i < optsingle.Loopstep; i++ {
		key := pfaddkey + strconv.Itoa(i)
		optsingle.RedisConn.PFAdd(key, rand.Float64()*float64(rand.Int()))
		pfaddkeyarray = append(pfaddkeyarray, key)
	}
	optsingle.RedisConn.PFMerge(pfmergekey, pfaddkeyarray...)

	for _, v := range pfaddkeyarray {
		optsingle.RedisConn.Del(v)
	}
	t2 := time.Now()
	zaplogger.Info("ExecCMD", zap.Int("db", optsingle.DB), zap.String("command", "BO_PFMERGE"), zap.String("KeySuffix", optsingle.KeySuffix), zap.Duration("time", t2.Sub(t1)))
}

//SET and SETNX
func (optsingle *OptSingle) BO_SET_SETNX() {
	t1 := time.Now()
	setkey := "set_" + optsingle.KeySuffix
	setnxkey := "setnx_" + optsingle.KeySuffix
	optsingle.RedisConn.Set(setkey, setkey, optsingle.EXPIRE)
	optsingle.RedisConn.SetNX(setnxkey, setnxkey, optsingle.EXPIRE)
	optsingle.RedisConn.SetNX(setnxkey, setkey, optsingle.EXPIRE)
	t2 := time.Now()
	zaplogger.Info("ExecCMD", zap.Int("db", optsingle.DB), zap.String("command", "SET_SETNX"), zap.String("KeySuffix", optsingle.KeySuffix), zap.Duration("time", t2.Sub(t1)))
}

//SETBIT
func (optsingle *OptSingle) BO_SETBIT() {
	t1 := time.Now()
	setbitkey := "setbit_" + optsingle.KeySuffix
	optsingle.RedisConn.SetBit(setbitkey, rand.Int63n(int64(optsingle.Loopstep)), rand.Intn(optsingle.Loopstep))
	optsingle.RedisConn.Expire(setbitkey, optsingle.EXPIRE)

	t2 := time.Now()
	zaplogger.Info("ExecCMD", zap.Int("db", optsingle.DB), zap.String("command", "SETBIT"), zap.String("KeySuffix", optsingle.KeySuffix), zap.Duration("time", t2.Sub(t1)))
}

//SETRANGE
func (optsingle *OptSingle) BO_SETRANGE() {
	t1 := time.Now()
	setrangekey := "setrange_" + optsingle.KeySuffix
	optsingle.RedisConn.Set(setrangekey, setrangekey, optsingle.EXPIRE)
	optsingle.RedisConn.SetRange(setrangekey, rand.Int63n(int64(optsingle.Loopstep)), strconv.Itoa(rand.Intn(optsingle.Loopstep)))
	t2 := time.Now()
	zaplogger.Info("ExecCMD", zap.Int("db", optsingle.DB), zap.String("command", "SETRANGE"), zap.String("KeySuffix", optsingle.KeySuffix), zap.Duration("time", t2.Sub(t1)))
}

//HINCRBY and HINCRBYFLOAT
func (optsingle *OptSingle) BO_HINCRBY_HINCRBYFLOAT() {
	t1 := time.Now()
	hincrbykey := "hincrby_" + optsingle.KeySuffix
	hincrbyfloatkey := "hincrbyfloat_" + optsingle.KeySuffix
	for i := 0; i < optsingle.Loopstep; i++ {
		optsingle.RedisConn.HIncrBy(hincrbykey, hincrbykey+strconv.Itoa(rand.Intn(optsingle.Loopstep)), int64(rand.Intn(optsingle.Loopstep)))
		optsingle.RedisConn.HIncrByFloat(hincrbyfloatkey, hincrbyfloatkey+strconv.Itoa(rand.Intn(optsingle.Loopstep)), rand.Float64()*10)
	}
	t2 := time.Now()
	zaplogger.Info("ExecCMD", zap.Int("db", optsingle.DB), zap.String("command", "HINCRBY_HINCRBYFLOAT"), zap.String("KeySuffix", optsingle.KeySuffix), zap.Duration("time", t2.Sub(t1)))
}

//HSET HMSET HSETNX HDEL
func (optsingle *OptSingle) BO_HSET_HMSET_HSETNX() {
	t1 := time.Now()
	hsetkey := "hset_" + optsingle.KeySuffix
	hmsetkey := "hmset_" + optsingle.KeySuffix
	fieldmap := make(map[string]interface{})

	for i := 0; i < optsingle.Loopstep; i++ {
		field := hmsetkey + strconv.Itoa(i)
		fieldmap[field] = field
	}

	for i := 0; i < optsingle.Loopstep; i++ {
		optsingle.RedisConn.HSet(hsetkey, hsetkey+strconv.Itoa(i), hsetkey+strconv.Itoa(i))
	}

	optsingle.RedisConn.HMSet(hmsetkey, fieldmap)

	//HSETNX
	for i := 0; i < optsingle.Loopstep; i++ {
		optsingle.RedisConn.HSetNX(hmsetkey, hmsetkey+strconv.Itoa(rand.Intn(optsingle.Loopstep*2)), hmsetkey+strconv.Itoa(i))
	}

	//HDEL
	for i := 0; i < optsingle.Loopstep; i++ {
		if rand.Intn(optsingle.Loopstep)%2 == 0 {
			optsingle.RedisConn.HDel(hmsetkey, hmsetkey+strconv.Itoa(rand.Intn(optsingle.Loopstep)))
		}
	}

	optsingle.RedisConn.Expire(hsetkey, optsingle.EXPIRE)
	optsingle.RedisConn.Expire(hmsetkey, optsingle.EXPIRE)
	t2 := time.Now()
	zaplogger.Info("ExecCMD", zap.Int("db", optsingle.DB), zap.String("command", "HSET_HMSET_HSETNX_HDEL"), zap.String("KeySuffix", optsingle.KeySuffix), zap.Duration("time", t2.Sub(t1)))
}

//LPUSH and LPOP and LPUSHX and LSET
func (optsingle *OptSingle) BO_LPUSH_LPOP_LPUSHX() {
	t1 := time.Now()
	lpushkey := "lpush_" + optsingle.KeySuffix
	lpushxkey := "lpushx_" + optsingle.KeySuffix
	values := make([]interface{}, optsingle.Loopstep)
	for i := 0; i < len(values); i++ {
		values[i] = lpushkey + strconv.Itoa(i)
	}

	optsingle.RedisConn.LPush(lpushkey, values...)
	for i := 0; i < optsingle.Loopstep; i++ {
		if rand.Intn(optsingle.Loopstep)%2 != 0 {
			optsingle.RedisConn.LSet(lpushkey, int64(rand.Intn(2*optsingle.Loopstep)-optsingle.Loopstep), lpushkey+strconv.Itoa(i))
		}
	}

	for i := 0; i < optsingle.Loopstep; i++ {
		if rand.Intn(optsingle.Loopstep)%2 != 0 {
			optsingle.RedisConn.LPop(lpushkey)
		}
	}
	optsingle.RedisConn.LPushX(lpushxkey, values)
	optsingle.RedisConn.LPushX(lpushkey, values)

	optsingle.RedisConn.Expire(lpushkey, optsingle.EXPIRE)
	t2 := time.Now()
	zaplogger.Info("ExecCMD", zap.Int("db", optsingle.DB), zap.String("command", "LPUSH_LPOP_LPUSHX_LSET"), zap.String("KeySuffix", optsingle.KeySuffix), zap.Duration("time", t2.Sub(t1)))

}

//LREM and LTRIM and LINSERT
func (optsingle *OptSingle) BO_LREM_LTRIM_LINSERT() {
	t1 := time.Now()
	lremkey := "lrem_" + optsingle.KeySuffix
	ltrimkey := "ltrim_" + optsingle.KeySuffix
	values := make([]interface{}, optsingle.Loopstep)
	for i := 0; i < len(values); i++ {
		values[i] = lremkey + strconv.Itoa(i)
	}
	optsingle.RedisConn.LPush(lremkey, values...)
	optsingle.RedisConn.LPush(ltrimkey, values...)

	for i := 0; i < optsingle.Loopstep; i++ {
		op := "BEFORE"
		if i%2 == 0 {
			op = "AFTER"
		} else {
			op = "BEFORE"
		}
		optsingle.RedisConn.LInsert(lremkey, op, lremkey+strconv.Itoa(rand.Intn(optsingle.Loopstep)), lremkey+strconv.Itoa(rand.Intn(optsingle.Loopstep)))
	}

	optsingle.RedisConn.LRem(lremkey, int64(rand.Intn(2*optsingle.Loopstep)-optsingle.Loopstep), lremkey+strconv.Itoa(rand.Intn(optsingle.Loopstep)))
	optsingle.RedisConn.LTrim(ltrimkey, int64(rand.Intn(2*optsingle.Loopstep)-optsingle.Loopstep), int64(rand.Intn(2*optsingle.Loopstep)-optsingle.Loopstep))
	optsingle.RedisConn.Expire(lremkey, optsingle.EXPIRE)
	optsingle.RedisConn.Expire(ltrimkey, optsingle.EXPIRE)
	t2 := time.Now()
	zaplogger.Info("ExecCMD", zap.Int("db", optsingle.DB), zap.String("command", "LREM_TRIM_LINSERT"), zap.String("KeySuffix", optsingle.KeySuffix), zap.Duration("time", t2.Sub(t1)))
}

//RPUSH RPUSHX RPOP RPOPLPUSH
func (optsingle *OptSingle) BO_RPUSH_RPUSHX_RPOP_RPOPLPUSH() {
	t1 := time.Now()
	rpushkey := "rpush_" + optsingle.KeySuffix
	rpushxkey := "rpushx_" + optsingle.KeySuffix
	values := make([]interface{}, optsingle.Loopstep)
	for i := 0; i < len(values); i++ {
		values[i] = rpushkey + strconv.Itoa(i)
	}
	optsingle.RedisConn.RPush(rpushkey, values...)
	optsingle.RedisConn.RPushX(rpushxkey, values...)
	optsingle.RedisConn.RPushX(rpushkey, values...)

	//rpoplpush 操作同一个key相当于将列表逆转
	for i := 0; i < optsingle.Loopstep; i++ {
		if rand.Intn(optsingle.Loopstep)%2 == 0 {
			optsingle.RedisConn.RPopLPush(rpushkey, rpushkey)
		}
	}

	for i := 0; i < optsingle.Loopstep; i++ {
		if rand.Intn(optsingle.Loopstep)%2 == 0 {
			optsingle.RedisConn.RPop(rpushkey)
		}
	}

	optsingle.RedisConn.Expire(rpushkey, optsingle.EXPIRE)
	optsingle.RedisConn.Expire(rpushxkey, optsingle.EXPIRE)

	t2 := time.Now()
	zaplogger.Info("ExecCMD", zap.Int("db", optsingle.DB), zap.String("command", "BO_RPUSH_RPUSHX_RPOP_RPOPLPUSH"), zap.String("KeySuffix", optsingle.KeySuffix), zap.Duration("time", t2.Sub(t1)))
}

//BLPOP BRPOP BRPOPLPUSH
//BRPOPLPUSH 集群模式下key分布在不同节点会报错(error) CROSSSLOT Keys in request don't hash to the same slot
func (optsingle *OptSingle) BO_BLPOP_BRPOP_BRPOPLPUSH() {
	t1 := time.Now()
	blpopkey := "blpop_" + optsingle.KeySuffix
	brpopkey := "brpop_" + optsingle.KeySuffix

	values := make([]interface{}, optsingle.Loopstep)
	for i := 0; i < len(values); i++ {
		values[i] = blpopkey + strconv.Itoa(i)
	}

	optsingle.RedisConn.RPush(blpopkey, values...)
	optsingle.RedisConn.RPush(brpopkey, values...)

	for i := 0; i < optsingle.Loopstep; i++ {
		if rand.Intn(optsingle.Loopstep)%2 == 0 {
			optsingle.RedisConn.BRPopLPush(blpopkey, blpopkey, optsingle.EXPIRE)
		}
	}

	for i := 0; i < optsingle.Loopstep; i++ {
		if rand.Intn(optsingle.Loopstep)%2 == 0 {
			optsingle.RedisConn.RPop(blpopkey)
			optsingle.RedisConn.RPop(brpopkey)
		}
	}

	optsingle.RedisConn.Expire(blpopkey, optsingle.EXPIRE)
	optsingle.RedisConn.Expire(brpopkey, optsingle.EXPIRE)
	t2 := time.Now()
	zaplogger.Info("ExecCMD", zap.Int("db", optsingle.DB), zap.String("command", "BO_BLPOP_BRPOP"), zap.String("KeySuffix", optsingle.KeySuffix), zap.Duration("time", t2.Sub(t1)))
}

//SADD SMOVE SPOP SREM
func (optsingle *OptSingle) BO_SADD_SMOVE_SPOP_SREM() {
	t1 := time.Now()
	saddkey := "sadd_" + optsingle.KeySuffix
	smovekey := "smove_" + optsingle.KeySuffix
	spopkey := "spop_" + optsingle.KeySuffix
	sremkey := "srem_" + optsingle.KeySuffix

	for i := 0; i < optsingle.Loopstep; i++ {
		optsingle.RedisConn.SAdd(saddkey, saddkey+strconv.Itoa(i))
		optsingle.RedisConn.SAdd(smovekey, smovekey+strconv.Itoa(i))
		optsingle.RedisConn.SAdd(spopkey, spopkey+strconv.Itoa(i))
		optsingle.RedisConn.SAdd(sremkey, sremkey+strconv.Itoa(i))
	}

	for i := 0; i < optsingle.Loopstep; i++ {
		if rand.Intn(optsingle.Loopstep)%2 == 0 {
			optsingle.RedisConn.SPop(spopkey)
		}
	}

	for i := 0; i < optsingle.Loopstep; i++ {
		if rand.Intn(optsingle.Loopstep)%2 == 0 {
			optsingle.RedisConn.SRem(sremkey, sremkey+strconv.Itoa(rand.Intn(optsingle.Loopstep)))
		}
	}

	for i := 0; i < optsingle.Loopstep; i++ {
		if rand.Intn(optsingle.Loopstep)%2 == 0 {
			optsingle.RedisConn.SMove(smovekey, smovekey, saddkey+strconv.Itoa(i))
		}
	}

	optsingle.RedisConn.Del(saddkey)
	optsingle.RedisConn.Expire(smovekey, optsingle.EXPIRE)
	optsingle.RedisConn.Expire(spopkey, optsingle.EXPIRE)
	optsingle.RedisConn.Expire(sremkey, optsingle.EXPIRE)

	t2 := time.Now()
	zaplogger.Info("ExecCMD", zap.Int("db", optsingle.DB), zap.String("command", "BO_RPUSH_RPUSHX_RPOP_RPOPLPUSH"), zap.String("KeySuffix", optsingle.KeySuffix), zap.Duration("time", t2.Sub(t1)))

}

//SDIFFSTORE SINTERSTORE SUNIONSTORE 集群模式下key分布在不同节点会报错(error) CROSSSLOT Keys in request don't hash to the same slot
func (optsingle *OptSingle) BO_SDIFFSTORE_SINTERSTORE_SUNIONSTORE() {
	t1 := time.Now()
	sdiff1 := "sdiff1_" + optsingle.KeySuffix
	sdiff2 := "sdiff2_" + optsingle.KeySuffix
	sdiffstore := "sdiffsotre_" + optsingle.KeySuffix
	sinterstore := "sintersotre_" + optsingle.KeySuffix
	sunionstore := "sunionstore_" + optsingle.KeySuffix

	for i := 0; i < optsingle.Loopstep; i++ {
		optsingle.RedisConn.SAdd(sdiff1, optsingle.KeySuffix+strconv.Itoa(rand.Intn(2*optsingle.Loopstep)))
		optsingle.RedisConn.SAdd(sdiff2, optsingle.KeySuffix+strconv.Itoa(rand.Intn(2*optsingle.Loopstep)))
	}

	optsingle.RedisConn.SDiffStore(sdiffstore, sdiff1, sdiff2)
	optsingle.RedisConn.SInterStore(sinterstore, sdiff1, sdiff2)
	optsingle.RedisConn.SUnionStore(sunionstore, sdiff1, sdiff2)

	optsingle.RedisConn.Expire(sdiffstore, optsingle.EXPIRE)
	optsingle.RedisConn.Expire(sinterstore, optsingle.EXPIRE)
	optsingle.RedisConn.Expire(sunionstore, optsingle.EXPIRE)

	optsingle.RedisConn.Del(sdiff1, sdiff2)

	t2 := time.Now()
	zaplogger.Info("ExecCMD", zap.Int("db", optsingle.DB), zap.String("command", "BO_SDIFFSTORE_SINTERSTORE_SUNIONSTORE"), zap.String("KeySuffix", optsingle.KeySuffix), zap.Duration("time", t2.Sub(t1)))
}

//ZADD ZINCRBY ZREM
//start version:1.2.0
func (optsingle OptSingle) BO_ZADD_ZINCRBY_ZPOPMAX_ZPOPMIN_ZREM() {
	t1 := time.Now()

	zaddkey := "zadd_" + optsingle.KeySuffix
	zincrby := "zincrby_" + optsingle.KeySuffix
	zrem := "zrem_" + optsingle.KeySuffix

	for i := 0; i < optsingle.Loopstep; i++ {
		z := redis.Z{
			Score:  float64(i),
			Member: zaddkey + strconv.Itoa(i),
		}
		optsingle.RedisConn.ZAdd(zaddkey, &z)
		optsingle.RedisConn.ZAdd(zincrby, &z)
		optsingle.RedisConn.ZAdd(zrem, &z)

	}

	for i := 0; i < optsingle.Loopstep; i++ {
		if rand.Intn(optsingle.Loopstep)%2 == 0 {
			optsingle.RedisConn.ZIncrBy(zincrby, float64(rand.Intn(2*optsingle.Loopstep)-optsingle.Loopstep), zaddkey+strconv.Itoa(rand.Intn(optsingle.Loopstep)))
			optsingle.RedisConn.ZRem(zrem, zaddkey+strconv.Itoa(rand.Intn(optsingle.Loopstep)))
		}
	}

	optsingle.RedisConn.Expire(zincrby, optsingle.EXPIRE)
	optsingle.RedisConn.Expire(zrem, optsingle.EXPIRE)
	t2 := time.Now()

	zaplogger.Info("ExecCMD", zap.Int("db", optsingle.DB), zap.String("command", "BO_ZADD_ZINCRBY_ZPOPMAX_ZPOPMIN_ZREM"), zap.String("KeySuffix", optsingle.KeySuffix), zap.Duration("time", t2.Sub(t1)))
}

//ZPOPMAX ZPOPMIN
//start version:5.0
func (optsingle OptSingle) BO_ZPOPMAX_ZPOPMIN() {
	t1 := time.Now()

	zpopmax := "zpopmax_" + optsingle.KeySuffix
	zpopmin := "zpopmin_" + optsingle.KeySuffix

	for i := 0; i < optsingle.Loopstep; i++ {
		z := redis.Z{
			Score:  float64(i),
			Member: zpopmax + strconv.Itoa(i),
		}
		optsingle.RedisConn.ZAdd(zpopmax, &z)
		optsingle.RedisConn.ZAdd(zpopmin, &z)
	}

	for i := 0; i < optsingle.Loopstep; i++ {
		if rand.Intn(optsingle.Loopstep)%2 == 0 {
			optsingle.RedisConn.ZPopMax(zpopmax, 1)
			optsingle.RedisConn.ZPopMin(zpopmin, 1)
		}
	}

	optsingle.RedisConn.Expire(zpopmax, optsingle.EXPIRE)
	optsingle.RedisConn.Expire(zpopmin, optsingle.EXPIRE)
	t2 := time.Now()

	zaplogger.Info("ExecCMD", zap.Int("db", optsingle.DB), zap.String("command", "BO_ZPOPMAX_ZPOPMIN"), zap.String("KeySuffix", optsingle.KeySuffix), zap.Duration("time", t2.Sub(t1)))
}

//ZREMRANGEBYLEX ZREMRANGEBYRANK ZREMRANGEBYSCORE
//start version:2.8.9
func (optsingle *OptSingle) BO_ZREMRANGEBYLEX_ZREMRANGEBYRANK_ZREMRANGEBYSCORE() {
	t1 := time.Now()

	zremrangebylex := "zremrangebylex_" + optsingle.KeySuffix
	zremrangebyrank := "zremrangebyrank_" + optsingle.KeySuffix
	zremrangebyscore := "zremrangebyscore_" + optsingle.KeySuffix

	for i := 0; i < optsingle.Loopstep; i++ {
		z := redis.Z{
			Score:  float64(i),
			Member: zremrangebylex + strconv.Itoa(i),
		}
		optsingle.RedisConn.ZAdd(zremrangebylex, &z)
		optsingle.RedisConn.ZAdd(zremrangebyrank, &z)
		optsingle.RedisConn.ZAdd(zremrangebyscore, &z)
	}

	optsingle.RedisConn.ZRemRangeByLex(zremrangebylex, zremrangebylex+strconv.Itoa(0), zremrangebylex+strconv.Itoa(rand.Intn(optsingle.Loopstep-1)))
	optsingle.RedisConn.ZRemRangeByRank(zremrangebyrank, int64(rand.Intn(2*optsingle.Loopstep)-optsingle.Loopstep), int64(rand.Intn(2*optsingle.Loopstep)-optsingle.Loopstep))
	optsingle.RedisConn.ZRemRangeByScore(zremrangebyscore, strconv.Itoa(rand.Intn(optsingle.Loopstep)), strconv.Itoa(rand.Intn(optsingle.Loopstep)))

	optsingle.RedisConn.Expire(zremrangebylex, optsingle.EXPIRE)
	optsingle.RedisConn.Expire(zremrangebyrank, optsingle.EXPIRE)
	optsingle.RedisConn.Expire(zremrangebyscore, optsingle.EXPIRE)

	t2 := time.Now()
	zaplogger.Info("ExecCMD", zap.Int("db", optsingle.DB), zap.String("command", "BO_ZREMRANGEBYLEX_ZREMRANGEBYRANK_ZREMRANGEBYSCORE"), zap.String("KeySuffix", optsingle.KeySuffix), zap.Duration("time", t2.Sub(t1)))

}

// BO_ZUNIONSTORE_ZINTERSTORE,集群模式下key分布在不同节点会报错(error) CROSSSLOT Keys in request don't hash to the same slot
func (optsingle *OptSingle) BO_ZUNIONSTORE_ZINTERSTORE() {
	t1 := time.Now()
	zset1 := "zset1_" + optsingle.KeySuffix
	zset2 := "zset2_" + optsingle.KeySuffix
	zset3 := "zset3_" + optsingle.KeySuffix
	zinterstore := "zinterstore_" + optsingle.KeySuffix
	zunionstore := "zunionstore_" + optsingle.KeySuffix

	for i := 0; i < optsingle.Loopstep; i++ {
		z := redis.Z{
			Score:  float64(i),
			Member: zset1 + strconv.Itoa(i),
		}
		optsingle.RedisConn.ZAdd(zset1, &z)
		optsingle.RedisConn.ZAdd(zset2, &z)
		optsingle.RedisConn.ZAdd(zset3, &z)
	}

	zstore := redis.ZStore{
		Keys:    []string{zset1, zset2, zset3},
		Weights: []float64{float64(rand.Intn(optsingle.Loopstep))},
	}

	optsingle.RedisConn.ZInterStore(zinterstore, &zstore)
	optsingle.RedisConn.ZUnionStore(zunionstore, &zstore)

	optsingle.RedisConn.Del(zset1, zset2, zset3)
	optsingle.RedisConn.Expire(zinterstore, optsingle.EXPIRE)
	optsingle.RedisConn.Expire(zunionstore, optsingle.EXPIRE)
	t2 := time.Now()
	zaplogger.Info("ExecCMD", zap.Int("db", optsingle.DB), zap.String("command", "BO_ZUNIONSTORE_ZINTERSTORE"), zap.String("KeySuffix", optsingle.KeySuffix), zap.Duration("time", t2.Sub(t1)))

}

//随机执行一个基础操作
func (optsingle *OptSingle) ExecRandOpt() {
	optsingle.OptType = BaseOptArray[rand.Intn(len(BaseOptArray))]
	optsingle.ExecOpt()
}

//随机执行一个基础操作半数概率为Set操作
func (optsingle *OptSingle) ExecRandOptHalfIsSet() {
	if rand.Int()%2 == 0 {
		optsingle.OptType = BO_SET_SETNX
		optsingle.ExecOpt()
	} else {
		optsingle.ExecRandOpt()
	}
}

//遍历执行所有基本操作
func (optsingle *OptSingle) ExecAllBasicOpt() {
	for _, v := range BaseOptArray {
		optsingle.OptType = v
		optsingle.ExecOpt()
	}
}

//持续随机执行基础操作
func (optsingle *OptSingle) KeepExecBasicOpt(ctx context.Context, sleeptime time.Duration, tocluster bool) {
	i := int64(0)
	keysuffix := optsingle.KeySuffix
	//会引起CROSSSLOT Keys in request don't hash to the same slot错误的命令列表
	tocluster_skip_array := map[OptType]string{
		BO_MSET_MSETNX:                        "BO_MSET_MSETNX",
		BO_PFMERGE:                            "BO_PFMERGE",
		BO_SDIFFSTORE_SINTERSTORE_SUNIONSTORE: "BO_SDIFFSTORE_SINTERSTORE_SUNIONSTORE",
		BO_ZUNIONSTORE_ZINTERSTORE:            "BO_ZUNIONSTORE_ZINTERSTORE",
	}

	for {
		rand.Seed(time.Now().UnixNano())
		randi := rand.Intn(2 * len(BaseOptArray))
		optsingle.KeySuffix = keysuffix + strconv.FormatInt(i, 10)
		if randi < len(BaseOptArray) {
			optsingle.OptType = BaseOptArray[randi]
		} else {
			optsingle.OptType = BO_SET_SETNX
		}

		if tocluster {
			if _, ok := tocluster_skip_array[optsingle.OptType]; ok {
				continue
			}
		}

		optsingle.ExecOpt()

		i++
		time.Sleep(sleeptime)
		select {
		case <-ctx.Done():
			return
		default:
			continue
		}
	}

}
