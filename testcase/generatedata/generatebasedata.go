package generatedata

import (

	//"fmt"
	"github.com/go-redis/redis/v7"
	"math/rand"
	"strconv"
	"sync"
	"time"
)
import "testcase/common"

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
	for i := 0; i < loopstep; i++ {
		client.Set(common.RandString(128), "xxx", time.Duration(3600*time.Second))
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
	loopsize := 100
	var wg sync.WaitGroup
	//APPEND
	wg.Add(1)
	go func() {
		appended := "append_" + common.RandString(16)
		for i := 0; i < loopsize; i++ {
			client.Append(appended, strconv.Itoa(i))
		}
		wg.Done()
	}()

	//BITOP
	wg.Add(1)
	go func() {
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

		wg.Done()
	}()

	//DECR and DECRBY
	wg.Add(1)
	go func() {
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

		wg.Done()
	}()

	//INCR and INCRBY and INCRBYFLOAT
	wg.Add(1)
	go func() {
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
		wg.Done()
	}()

	//MSET and MSETNX
	wg.Add(1)
	go func() {
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

		wg.Done()
	}()

	//PSETEX
	wg.Add(1)
	go func() {

		psetexbaseval := common.RandString(64)
		for i := 0; i < 100; i++ {
			psetexkey := "psetex_" + common.RandString(16)
			client.Do("psetex", psetexkey, 3600000, psetexbaseval+strconv.Itoa(i))
		}

		wg.Done()
	}()

	//SET
	wg.Add(1)
	go func() {
		setbaseval := common.RandString(64)
		for i := 0; i < 100; i++ {
			setkey := "set_" + common.RandString(16)
			client.Set(setkey, setbaseval+strconv.Itoa(i), time.Duration(3600*time.Second))
		}

		wg.Done()
	}()

	//SETBIT
	go func() {

		for i := 0; i < 100; i++ {
			setbitkey := "setbit_" + common.RandString(16)
			client.SetBit(setbitkey, rand.Int63n(100), rand.Intn(1))
		}

		wg.Done()
	}()

	wg.Wait()

}

func PushList(client *redis.Client, listname string, str string) {
	client.LPush(listname, str)
}

func Hset(client *redis.Client, key string, field string, value string) {
	client.HSet(key, field, value)
}
