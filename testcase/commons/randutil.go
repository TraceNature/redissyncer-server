package commons

import (
	"math/rand"
	"time"
	"github.com/satori/go.uuid"
)

const charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

//生成随机字符串
func StringWithCharset(length int, charset string) string {
	var seededRand *rand.Rand = rand.New(
		rand.NewSource(time.Now().UnixNano()))
	b := make([]byte, length)
	for i := range b {
		b[i] = charset[seededRand.Intn(len(charset))]
	}
	return string(b)
}

func RandString(length int) string {
	//var seededRand *rand.Rand = rand.New(
	//	rand.NewSource(time.Now().UnixNano()))
	rand.Seed(time.Now().UnixNano())

	b := make([]byte, length)
	for i := range b {
		//b[i] = charset[seededRand.Intn(len(charset))]
		b[i] = charset[rand.Intn(len(charset))]

	}
	return string(b)
}

func GetUUID() string {
	return uuid.NewV4().String()
}
