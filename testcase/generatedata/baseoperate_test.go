package generatedata

import (
	"context"
	"fmt"
	"github.com/go-redis/redis/v7"
	"sync"
	"testcase/commons"
	"testing"
	"time"
)

func TestBasicOpt_ExecOpt(t *testing.T) {
	saddr := "114.67.100.239:6379"
	opt := &redis.Options{
		Addr: saddr,
		DB:   0, // use default DB
	}
	opt.Password = "redistest0102"
	client := commons.GetGoRedisClient(opt)

	baseopt := BaseOpt{
		RedisConn: client.Conn(),
		//RedisClient:  client,
		RedisVersion: "4.0",
		OptType:      BO_APPEND,
		KeySuffix:    "keysuffix",
		Loopstep:     20,
		EXPIRE:       600 * time.Second,
	}
	baseopt.RedisConn.Select(3)

	for _, v := range BaseOptArray {
		baseopt.OptType = v
		baseopt.ExecOpt()
	}

	baseopt.ExecOpt()

	fmt.Println(baseopt.KeySuffix, baseopt.EXPIRE, baseopt.Loopstep)
	fmt.Println("䷃")

}

func TestBasicOpt_KeepExecBasicOpt(t *testing.T) {
	saddr := "114.67.100.239:6379"
	opt := &redis.Options{
		Addr: saddr,
		DB:   0, // use default DB
	}
	opt.Password = "redistest0102"
	client := commons.GetGoRedisClient(opt)
	defer client.Close()

	baseopt := BaseOpt{
		RedisConn:    client.Conn(),
		RedisVersion: "4.0",
		OptType:      BO_APPEND,
		KeySuffix:    "keysuffix",
		Loopstep:     20,
		EXPIRE:       600 * time.Second,
	}

	wg := sync.WaitGroup{}
	d := time.Now().Add(30000 * time.Second)
	ctx, cancel := context.WithDeadline(context.Background(), d)
	defer cancel()

	wg.Add(1)
	go func() {
		baseopt.KeepExecBasicOpt(ctx, 300*time.Millisecond)
		zaplogger.Sugar().Info("KeepExecBasicOpt Finish!!!")
		wg.Done()
	}()
	wg.Wait()

}
