package generatedata

import (
	"fmt"
	"github.com/go-redis/redis/v7"
	"math/rand"
	"strconv"

	//"go.uber.org/zap"
	"testcase/commons"
	"testing"
	"time"
)

func TestGenerateBaseDataParallel(t *testing.T) {
	saddr := "114.67.100.239:6379"
	opt := &redis.Options{
		Addr: saddr,
		DB:   0, // use default DB
	}
	opt.Password = "redistest0102"
	client := commons.GetGoRedisClient(opt)

	defer client.Close()

	client.Set("aaa", "sa", 1000*time.Second)
	client.LPush("lista", "tttt", "aa", "aadd")

	client.FlushAll()

	for i := 0; i < 100; i++ {
		client.HSet("hash_1", "feild"+strconv.Itoa(i), "val"+strconv.Itoa(i))
	}
	for i := 0; i < 100; i++ {
		client.SAdd("set_1", "set"+strconv.Itoa(i))
	}
	for i := 0; i < 100; i++ {
		client.ZAdd("zset_1", &redis.Z{Score: rand.Float64(), Member: "member" + strconv.Itoa(i)})
	}

	fmt.Println(client.Type("hash_1"))
	cursor := uint64(0)
	for {
		sourceresult, c, err := client.HScan("hash_1", cursor, "*", int64(1)).Result()
		if err != nil {
			fmt.Println(err)
		}

		fmt.Println("output hash_1")
		fmt.Println(c)
		fmt.Println(sourceresult)

		cursor = c
		if c == 0 {
			break
		}
	}

	cursor = uint64(0)
	for {
		sourceresult, c, err := client.SScan("set_1", cursor, "*", int64(10)).Result()
		if err != nil {
			fmt.Println(err)
		}

		fmt.Println("output hash_1")
		fmt.Println(c)
		fmt.Println(sourceresult)

		cursor = c
		if c == 0 {
			break
		}
	}

}
