package common

import(
	redis "github.com/go-redis/redis/v7"
)

//GetGoRedisClient 获取redis client
func GetGoRedisClient(opt *redis.Options) *redis.Client {

	client := redis.NewClient(opt)
	return client
}
