package compare

import (
	"fmt"
	"reflect"
	"testing"
)

type TT struct {
	A   string
	B   int
	IsA bool
}

func (t *TT) Testtt() {
	var temp interface{}

	if t.IsA {
		temp = t.A
	} else {
		temp = t.B
	}

	fmt.Println(reflect.TypeOf(temp))

}
func TestCompare_CompareDB(t *testing.T) {

	//saddr := "114.67.100.239:6379"
	//opt := &redis.Options{
	//	Addr: saddr,
	//	DB:   0, // use default DB
	//}
	//opt.Password = "redistest0102"
	//client := commons.GetGoRedisClient(opt)
	//
	//compare := Compare{
	//	Source: client, Target: client, BatchSize: 10,
	//}
	//
	//compare.CompareDB()
	tt := TT{
		A:   "abc",
		B:   1234,
		IsA: false,
	}

	tt.Testtt()

}
