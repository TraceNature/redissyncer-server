package compare

import (
	"github.com/go-redis/redis/v7"
	"testcase/commons"
	"testing"
)

func TestCompareSingle2Single_CompareDB(t *testing.T) {
	saddr := "114.67.100.239:6379"
	opt := &redis.Options{
		Addr: saddr,
		DB:   0, // use default DB
	}
	opt.Password = "redistest0102"
	sclient := commons.GetGoRedisClient(opt)

	//compare := CompareSingle2Single{
	//	Source: client, Target: client, BatchSize: 10,
	//}

	tclusterclient := redis.NewClusterClient(&redis.ClusterOptions{
		Addrs: []string{"114.67.67.7:16379",
			" 114.67.67.7:16380",
			" 114.67.83.163:16379 ",
			" 114.67.83.163:16380 ",
			" 114.67.112.67:16379 ",
			" 114.67.112.67:16380"},
		Password: "testredis0102",
	})
	
	csc := &CompareSingle2Cluster{
		Source:         sclient,         //源redis single
		Target:         tclusterclient,  //目标redis single
		BatchSize:      int64(30),       //比较List、Set、Zset类型时的每批次值的数量
		CompareThreads: 4,               //比较db线程数量
		TTLDiff:        float64(100000), //TTL最小差值
		SourceDB:       0,               //源redis DB number
		TargetDB:       0,               //目标redis DB number
	}
	csc.CompareDB()
}
