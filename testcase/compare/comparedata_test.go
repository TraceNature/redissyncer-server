package compare

import (
	"github.com/go-redis/redis/v7"
	"testcase/commons"
	"testing"
)

func TestCompare_CompareDB(t *testing.T) {
	saddr := "114.67.100.239:6379"
	opt := &redis.Options{
		Addr: saddr,
		DB:   0, // use default DB
	}
	opt.Password = "redistest0102"
	client := commons.GetGoRedisClient(opt)

	compare := Compare{
		Source: client, Target: client, BatchSize: 10,
	}

	compare.CompareDB()
}
