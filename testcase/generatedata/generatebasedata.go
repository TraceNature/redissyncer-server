package generatedata

import (
	"github.com/go-redis/redis/v7"
	"github.com/sirupsen/logrus"
	"math/rand"
	"strconv"
	"sync"
	"time"
	"github.com/panjf2000/ants/v2"
)
import "testcase/common"

var logger = logrus.New()

func GenerateBase(client *redis.Client) {
	//生成biglist
	listname := "list_" + common.RandString(10)
	listbasevalue := common.RandString(128 * 1024)
	loopstep := 100
	for i := 0; i < loopstep; i++ {
		client.LPush(listname, listbasevalue+strconv.Itoa(i))
		//fmt.Println(listbasevalue)
	}
	//生成string kv
	baseset := common.RandString(128)
	for i := 0; i < loopstep; i++ {
		client.Set(baseset+strconv.Itoa(i), "xxx", time.Duration(3600*time.Second))
	}

	//生成set
	setname := "set_" + common.RandString(16)
	setbasevalue := common.RandString(128)
	for i := 0; i < loopstep; i++ {
		client.SAdd(setname, setbasevalue+strconv.Itoa(i))
	}

	//生成hset
	hsetname := "set_" + common.RandString(16)
	hsetbasevalue := common.RandString(128)
	for i := 0; i < loopstep; i++ {
		client.HSet(hsetname, hsetbasevalue+strconv.Itoa(i), i)
	}

	//生成sortedSet
	sortedsetname := "sortedset_" + common.RandString(16)
	for i := 0; i < loopstep; i++ {
		member := &redis.Z{
			Score:  float64(i),
			Member: common.RandString(10),
		}
		client.ZAdd(sortedsetname, member)

	}
}

func GenerateIncrement(client *redis.Client) {

	p, _ := ants.NewPool(8, ants.WithMaxBlockingTasks(8))
	defer p.Release()

	loopsize := 100
	var wg sync.WaitGroup

	//APPEND
	wg.Add(1)
	appendfunc := func() {
		t1 := time.Now()
		appended := "append_" + common.RandString(16)
		for i := 0; i < loopsize; i++ {
			client.Append(appended, strconv.Itoa(i))
		}
		t2 := time.Now()
		logger.WithFields(logrus.Fields{
			"command": "append",
		}).Info(t2.Sub(t1))
		wg.Done()
	}
	p.Submit(appendfunc)

	//BITOP
	wg.Add(1)
	bitopfunc := func() {
		t1 := time.Now()
		strarry := []string{}
		opandkey := "opand_" + common.RandString(8)
		oporkey := "opor_" + common.RandString(8)
		opxorkey := "opxor_" + common.RandString(8)
		opnotkey := "opnot_" + common.RandString(8)
		for i := 0; i < 20; i++ {
			bitopkey := "bitop_" + common.RandString(16)
			client.Do("set", bitopkey, common.RandString(16))
			strarry = append(strarry, bitopkey)
		}

		client.BitOpAnd(opandkey, strarry...)
		client.BitOpOr(oporkey, strarry...)
		client.BitOpXor(opxorkey, strarry...)
		client.BitOpNot(opnotkey, strarry[0])
		t2 := time.Now()
		logger.WithFields(logrus.Fields{
			"command": "bitop",
		}).Info(t2.Sub(t1))
		wg.Done()
	}
	p.Submit(bitopfunc)

	//DECR and DECRBY
	wg.Add(1)
	decrfunc := func() {
		t1 := time.Now()
		strarry := []string{}
		for i := 0; i < 100; i++ {
			desckey := "desc_" + common.RandString(16)
			client.Do("set", desckey, rand.Intn(200))
			strarry = append(strarry, desckey)
		}

		for _, str := range strarry {
			client.Decr(str)
			client.DecrBy(str, rand.Int63n(300))
		}
		t2 := time.Now()
		logger.WithFields(logrus.Fields{
			"command": "decr",
		}).Info(t2.Sub(t1))
		wg.Done()
	}
	p.Submit(decrfunc)

	//INCR and INCRBY and INCRBYFLOAT
	wg.Add(1)
	incrfunc := func() {
		t1 := time.Now()
		strarry := []string{}
		for i := 0; i < 100; i++ {
			desckey := "incr_" + common.RandString(16)
			client.Do("set", desckey, rand.Intn(200))
			strarry = append(strarry, desckey)
		}
		for _, str := range strarry {
			client.Incr(str)
			client.IncrBy(str, rand.Int63n(300))
			client.IncrByFloat(str, rand.Float64())
		}
		t2 := time.Now()
		logger.WithFields(logrus.Fields{
			"command": "incr",
		}).Info(t2.Sub(t1))
		wg.Done()
	}
	p.Submit(incrfunc)

	//MSET and MSETNX
	wg.Add(1)
	msetfunc := func() {
		t1 := time.Now()
		msetarry := []string{}
		msetnxarry := []string{}

		for i := 0; i < 100; i++ {
			msetkv := "mset_" + common.RandString(16)
			msetarry = append(msetarry, msetkv)
		}

		for i := 0; i < 100; i++ {
			msetnxkv := "msetnx_" + common.RandString(16)
			msetnxarry = append(msetnxarry, msetnxkv)
		}

		client.MSetNX(msetnxarry)
		client.MSet(msetarry)
		client.MSetNX(msetarry)
		t2 := time.Now()
		logger.WithFields(logrus.Fields{
			"command": "mset",
		}).Info(t2.Sub(t1))
		wg.Done()
	}
	p.Submit(msetfunc)

	//PSETEX
	wg.Add(1)
	psetnexfuc := func() {
		t1 := time.Now()
		psetexbaseval := common.RandString(64)
		for i := 0; i < 100; i++ {
			psetexkey := "psetex_" + common.RandString(16)
			client.Do("psetex", psetexkey, 3600000, psetexbaseval+strconv.Itoa(i))
		}
		t2 := time.Now()
		logger.WithFields(logrus.Fields{
			"command": "psetex",
		}).Info(t2.Sub(t1))
		wg.Done()
	}
	p.Submit(psetnexfuc)

	//SET
	wg.Add(1)
	setfunc := func() {
		t1 := time.Now()
		setbaseval := common.RandString(64)
		for i := 0; i < 100; i++ {
			setkey := "set_" + common.RandString(16)
			client.Set(setkey, setbaseval+strconv.Itoa(i), time.Duration(3600*time.Second))
		}
		t2 := time.Now()
		logger.WithFields(logrus.Fields{
			"command": "set",
		}).Info(t2.Sub(t1))
		wg.Done()
	}
	p.Submit(setfunc)

	//SETBIT
	wg.Add(1)
	setbitfunc := func() {
		t1 := time.Now()
		for i := 0; i < 100; i++ {
			setbitkey := "setbit_" + common.RandString(16)
			client.SetBit(setbitkey, rand.Int63n(100), rand.Intn(1))
		}
		t2 := time.Now()
		logger.WithFields(logrus.Fields{
			"command": "setbit",
		}).Info(t2.Sub(t1))
		wg.Done()
	}
	p.Submit(setbitfunc)

	//SETEX
	wg.Add(1)
	setexfunc := func() {
		t1 := time.Now()
		for i := 0; i < 100; i++ {
			setexkey := "setex_" + common.RandString(16)
			client.Do("setex", setexkey, 3600, setexkey)
		}
		t2 := time.Now()
		logger.WithFields(logrus.Fields{
			"command": "setex",
		}).Info(t2.Sub(t1))

		wg.Done()
	}
	p.Submit(setexfunc)

	//SETNX
	wg.Add(1)
	setnxfunc := func() {
		t1 := time.Now()
		for i := 0; i < 100; i++ {
			setnxkey := "setnx_" + common.RandString(16)
			client.SetNX(setnxkey, setnxkey, time.Duration(3600*time.Second))
		}
		t2 := time.Now()
		logger.WithFields(logrus.Fields{
			"command": "setnx",
		}).Info(t2.Sub(t1))
		wg.Done()
	}
	p.Submit(setnxfunc)

	//SETRANGE
	wg.Add(1)
	setrangefunc := func() {
		t1 := time.Now()
		strarry := []string{}
		setrangebaseval := common.RandString(4)
		for i := 0; i < 100; i++ {
			setrangekey := "setrange_" + common.RandString(16)
			client.SetRange(setrangekey, rand.Int63n(16), setrangebaseval+strconv.Itoa(i))
			strarry = append(strarry, setrangekey)
		}
		t2 := time.Now()
		logger.WithFields(logrus.Fields{
			"command": "setrange",
		}).Info(t2.Sub(t1))
		wg.Done()
	}
	p.Submit(setrangefunc)

	//HINCRBY
	wg.Add(1)
	hicrbyfunc := func() {
		t1 := time.Now()
		for i := 0; i < 100; i++ {
			client.HIncrBy("HINCRBY_"+strconv.Itoa(i), "page_view", rand.Int63n(100))
		}
		t2 := time.Now()
		logger.WithFields(logrus.Fields{
			"command": "hincrby",
		}).Info(t2.Sub(t1))
		wg.Done()
	}
	p.Submit(hicrbyfunc)

	//HINCRBYFLOAT
	wg.Add(1)
	hincrbyfloatfunc := func() {
		t1 := time.Now()
		for i := 0; i < 100; i++ {
			client.HIncrByFloat("HINCRBY_"+common.RandString(4), "page_view", rand.Float64())
		}
		t2 := time.Now()
		logger.WithFields(logrus.Fields{
			"command": "hincrbyfloat",
		}).Info(t2.Sub(t1))
		wg.Done()
	}
	p.Submit(hincrbyfloatfunc)

	//HMSET
	wg.Add(1)
	hmsetfunc := func() {
		t1 := time.Now()
		fieldmap := make(map[string]interface{})
		for i := 0; i < 20; i++ {
			key := common.RandString(8)
			fieldmap[key] = "wwww." + key + ".com"
		}
		client.HMSet("hmset_"+common.RandString(4), fieldmap)
		t2 := time.Now()
		logger.WithFields(logrus.Fields{
			"command": "hmset",
		}).Info(t2.Sub(t1))
		wg.Done()
	}
	p.Submit(hmsetfunc)

	//HSETNX
	wg.Add(1)
	hsetnxfunc := func() {
		defer wg.Done()
		t1 := time.Now()
		basekey := "hsetnx_" + common.RandString(8)
		basefield := common.RandString(8)
		for i := 0; i < 20; i++ {
			client.HSetNX(basekey, basefield+strconv.Itoa(i), basefield)
		}
		t2 := time.Now()
		logger.WithFields(logrus.Fields{
			"command": "hsetnx",
		}).Info(t2.Sub(t1))

	}
	p.Submit(hsetnxfunc)

	//LPUSH
	wg.Add(1)
	lpushfunc := func() {
		defer wg.Done()
		t1 := time.Now()
		values := make([]interface{}, 40)
		for i := 0; i < len(values); i++ {
			values[i] = i
		}

		client.LPush("lpush_"+common.RandString(4), values...)
		t2 := time.Now()
		logger.WithFields(logrus.Fields{
			"command": "lpush",
		}).Info(t2.Sub(t1))

	}
	p.Submit(lpushfunc)

	//BLPOP
	wg.Add(1)
	blpopfunc := func() {
		defer wg.Done()
		t1 := time.Now()
		basekey := "blpop_" + common.RandString(8)
		keys := []string{}
		values := make([]interface{}, 40)
		for i := 0; i < len(values); i++ {
			values[i] = i
		}
		for i := 0; i < 50; i++ {
			key := basekey + strconv.Itoa(i)
			client.LPush(key, values...)
			keys = append(keys, key)
		}
		client.BLPop(5*time.Second, keys...)

		t2 := time.Now()
		logger.WithFields(logrus.Fields{
			"command": "blpop",
		}).Info(t2.Sub(t1))
	}
	p.Submit(blpopfunc)

	//BRPOP
	wg.Add(1)
	brpopfunc := func() {
		defer wg.Done()
		t1 := time.Now()
		basekey := "brpop_" + common.RandString(8)
		keys := []string{}
		values := make([]interface{}, 40)
		for i := 0; i < len(values); i++ {
			values[i] = i
		}
		for i := 0; i < 50; i++ {
			key := basekey + strconv.Itoa(i)
			client.LPush(key, values...)
			keys = append(keys, key)
		}
		client.BRPop(5*time.Second, keys...)

		t2 := time.Now()
		logger.WithFields(logrus.Fields{
			"command": "brpop",
		}).Info(t2.Sub(t1))
	}
	p.Submit(brpopfunc)

	//BRPOPLPUSH
	wg.Add(1)
	brpoplpushfunc := func() {
		defer wg.Done()
		t1 := time.Now()
		basekey := "brpoplpush_" + common.RandString(8)
		keys := []string{}
		values := make([]interface{}, 40)
		for i := 0; i < len(values); i++ {
			values[i] = i
		}

		for i := 0; i < 50; i++ {
			key := basekey + strconv.Itoa(i)
			client.LPush(key, values...)
			keys = append(keys, key)
		}

		for k, v := range keys {
			client.BRPopLPush(v, v+strconv.Itoa(k), 30*time.Second)
		}

		t2 := time.Now()
		logger.WithFields(logrus.Fields{
			"command": "brpoplpush",
		}).Info(t2.Sub(t1))
	}
	p.Submit(brpoplpushfunc)

	//LINSERT
	wg.Add(1)
	linsertfunc := func() {
		defer wg.Done()
		t1 := time.Now()
		basekey := "linsert_" + common.RandString(8)
		keys := []string{}
		values := make([]interface{}, 40)
		for i := 0; i < len(values); i++ {
			values[i] = i
		}

		for i := 0; i < 50; i++ {
			key := basekey + strconv.Itoa(i)
			client.LPush(key, values...)
			keys = append(keys, key)
		}

		for _, v := range keys {
			client.LInsert(v, "AFTER", rand.Intn(40), v)
		}

		t2 := time.Now()
		logger.WithFields(logrus.Fields{
			"command": "linsert",
		}).Info(t2.Sub(t1))
	}
	p.Submit(linsertfunc)

	//LPOP
	wg.Add(1)
	lpopfunc := func() {
		defer wg.Done()
		t1 := time.Now()
		basekey := "lpop_" + common.RandString(8)
		keys := []string{}
		values := make([]interface{}, 40)
		for i := 0; i < len(values); i++ {
			values[i] = i
		}

		for i := 0; i < 50; i++ {
			key := basekey + strconv.Itoa(i)
			client.LPush(key, values...)
			keys = append(keys, key)
		}

		for _, v := range keys {
			client.LPop(v)
		}

		t2 := time.Now()
		logger.WithFields(logrus.Fields{
			"command": "lpop",
		}).Info(t2.Sub(t1))
	}
	p.Submit(lpopfunc)

	//LPUSHX
	wg.Add(1)
	lpushxfunc := func() {
		defer wg.Done()
		t1 := time.Now()
		basekey := "lpushx_" + common.RandString(8)
		values := make([]interface{}, 40)
		for i := 0; i < len(values); i++ {
			values[i] = i
		}

		client.LPushX(basekey, values)
		t2 := time.Now()
		logger.WithFields(logrus.Fields{
			"command": "lpushx",
		}).Info(t2.Sub(t1))
	}
	p.Submit(lpushxfunc)

	//LREM
	wg.Add(1)
	lremfunc := func() {
		t1 := time.Now()
		defer wg.Done()
		basekey := "lrem_" + common.RandString(4)
		for i := 0; i < 50; i++ {
			client.LPush(basekey, i)
		}

		for i := 0; i < 10; i++ {
			client.LRem(basekey, 0, rand.Intn(20))
		}
		t2 := time.Now()
		logger.WithFields(logrus.Fields{
			"command": "lrem",
		}).Info(t2.Sub(t1))
	}
	p.Submit(lremfunc)

	//LSET
	wg.Add(1)
	lsetfunc := func() {
		t1 := time.Now()
		defer wg.Done()
		basekey := "lrem_" + common.RandString(4)
		for i := 0; i < 50; i++ {
			client.LPush(basekey, i)
		}

		for i := 0; i < 10; i++ {
			client.LSet(basekey, rand.Int63n(49), common.RandString(4))
		}
		t2 := time.Now()
		logger.WithFields(logrus.Fields{
			"command": "lset",
		}).Info(t2.Sub(t1))
	}
	p.Submit(lsetfunc)

	//LTRIM
	wg.Add(1)
	ltrimfunc := func() {
		t1 := time.Now()
		defer wg.Done()
		basekey := "ltrim_" + common.RandString(4)
		values := make([]interface{}, 50)
		keys := []string{}
		for i := 0; i < len(values); i++ {
			values[i] = i
		}
		for i := 0; i < 50; i++ {
			key := basekey + strconv.Itoa(i)
			client.LPush(key, values...)
			keys = append(keys, key)
		}

		for _, v := range keys {
			client.LTrim(v, rand.Int63n(49), rand.Int63n(49))
		}
		t2 := time.Now()
		logger.WithFields(logrus.Fields{
			"command": "ltrim",
		}).Info(t2.Sub(t1))
	}
	p.Submit(ltrimfunc)

	//RPOP
	wg.Add(1)
	lrpopfunc := func() {
		t1 := time.Now()
		defer wg.Done()
		basekey := "rpop_" + common.RandString(4)
		values := make([]interface{}, 50)
		keys := []string{}
		for i := 0; i < len(values); i++ {
			values[i] = i
		}
		for i := 0; i < 50; i++ {
			key := basekey + strconv.Itoa(i)
			client.RPush(key, values...)
			keys = append(keys, key)
		}

		for _, v := range keys {
			client.RPop(v)
		}
		t2 := time.Now()
		logger.WithFields(logrus.Fields{
			"command": "rpop",
		}).Info(t2.Sub(t1))
	}
	p.Submit(lrpopfunc)

	//RPOPLPUSH
	wg.Add(1)
	rpoplpushfunc := func() {
		defer wg.Done()
		t1 := time.Now()
		basekey := "rpoplpush_" + common.RandString(8)
		keys := []string{}
		values := make([]interface{}, 40)
		for i := 0; i < len(values); i++ {
			values[i] = i
		}

		for i := 0; i < 50; i++ {
			key := basekey + strconv.Itoa(i)
			client.LPush(key, values...)
			keys = append(keys, key)
		}

		for k, v := range keys {
			client.RPopLPush(v, v+strconv.Itoa(k))
		}

		t2 := time.Now()
		logger.WithFields(logrus.Fields{
			"command": "rpoplpush",
		}).Info(t2.Sub(t1))
	}
	p.Submit(rpoplpushfunc)

	//RPUSHX
	wg.Add(1)
	rpushxfunc := func() {
		defer wg.Done()
		t1 := time.Now()
		basekey := "rpushx_" + common.RandString(8)
		values := make([]interface{}, 40)
		for i := 0; i < len(values); i++ {
			values[i] = i
		}

		client.RPushX(basekey, values)
		t2 := time.Now()
		logger.WithFields(logrus.Fields{
			"command": "rpushx",
		}).Info(t2.Sub(t1))
	}
	p.Submit(rpushxfunc)

	//SADD
	wg.Add(1)
	saddfunc := func() {
		defer wg.Done()
		t1 := time.Now()
		basekey := "sadd_" + common.RandString(4)
		for i := 0; i < 50; i++ {
			client.SAdd(basekey, basekey+strconv.Itoa(i))
		}
		t2 := time.Now()
		logger.WithFields(logrus.Fields{
			"command": "sadd",
		}).Info(t2.Sub(t1))
	}
	p.Submit(saddfunc)

	//SDIFFSTORE
	wg.Add(1)
	sdiffstorefunc := func() {
		defer wg.Done()
		t1 := time.Now()
		basekey := "sdiffstore_" + common.RandString(4)
		keys := []string{}
		for i := 0; i < 50; i++ {
			key := basekey + strconv.Itoa(i)
			client.SAdd(key, strconv.Itoa(rand.Intn(10)))
			keys = append(keys, key)
		}
		client.SDiffStore(basekey+common.RandString(2), keys...)
		t2 := time.Now()
		logger.WithFields(logrus.Fields{
			"command": "sdiffstore",
		}).Info(t2.Sub(t1))
	}
	p.Submit(sdiffstorefunc)

	//SINTERSTORE
	wg.Add(1)
	sinsertstorefunc := func() {
		defer wg.Done()
		t1 := time.Now()
		basekey := "sinsertstore_" + common.RandString(4)
		keys := []string{}
		for i := 0; i < 50; i++ {
			key := basekey + strconv.Itoa(i)
			client.SAdd(key, strconv.Itoa(rand.Intn(10)))
			keys = append(keys, key)
		}
		client.SDiffStore(basekey+common.RandString(2), keys...)
		t2 := time.Now()
		logger.WithFields(logrus.Fields{
			"command": "sinsertstore",
		}).Info(t2.Sub(t1))
	}
	p.Submit(sinsertstorefunc)

	//SMOVE
	wg.Add(1)
	smovefunc := func() {
		defer wg.Done()
		t1 := time.Now()
		basekey := "smove_" + common.RandString(4)
		values := []string{}
		for i := 0; i < 50; i++ {
			value := basekey + strconv.Itoa(i)
			client.SAdd(basekey, value)
			values = append(values, value)
		}
		for i := 0; i < 10; i++ {
			client.SMove(basekey, basekey+strconv.Itoa(i), values[i+1])
		}

		t2 := time.Now()
		logger.WithFields(logrus.Fields{
			"command": "smove",
		}).Info(t2.Sub(t1))
	}
	p.Submit(smovefunc)

	//SPOP
	wg.Add(1)
	spopfunc := func() {
		defer wg.Done()
		t1 := time.Now()
		basekey := "spop_" + common.RandString(4)

		for i := 0; i < 50; i++ {
			value := basekey + strconv.Itoa(i)
			client.SAdd(basekey, value)

		}
		for i := 0; i < 10; i++ {
			client.SPop(basekey)
		}

		t2 := time.Now()
		logger.WithFields(logrus.Fields{
			"command": "spop",
		}).Info(t2.Sub(t1))
	}
	p.Submit(spopfunc)

	//SREM

	wg.Wait()

}

func PushList(client *redis.Client, listname string, str string) {
	client.LPush(listname, str)
}

func Hset(client *redis.Client, key string, field string, value interface{}) {
	client.HSet(key, field, value)
}
