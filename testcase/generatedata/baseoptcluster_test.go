package generatedata

import (
	"github.com/go-redis/redis/v7"
	"testing"
	"time"
)

func TestOptCluster_ExecOpt(t *testing.T) {
	rdb := redis.NewClusterClient(&redis.ClusterOptions{
		Addrs: []string{"114.67.67.7:16379",
			" 114.67.67.7:16380",
			" 114.67.83.163:16379 ",
			" 114.67.83.163:16380 ",
			" 114.67.112.67:16379 ",
			" 114.67.112.67:16380"},
		Password: "testredis0102",
	})
	oc := &OptCluster{
		ClusterClient: rdb,
		OptType:       BO_APPEND,
		KeySuffix:     "clustertest",
		Loopstep:      20,
		EXPIRE:        600 * time.Second,
	}

	for _, v := range BaseOptArray {
		oc.OptType = v
		oc.ExecOpt()

	}
}
