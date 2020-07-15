package main

import (
	"context"
	"fmt"
	"strconv"
	"testing"
	"time"
)

var key string = "name"

func TestExists(t *testing.T) {

	ctx, cancel := context.WithCancel(context.Background())
	for i := 0; i < 10; i++ {
		name := "监控" + strconv.Itoa(i)
		valueCtx := context.WithValue(ctx, key, name)
		go watch(valueCtx, name)
	}

	time.Sleep(10 * time.Second)
	fmt.Println("可以了，通知监控停止")

	cancel()
	//为了检测监控过是否停止，如果没有监控输出，就表示停止了
	time.Sleep(5 * time.Second)

	FileExist := true
	if !FileExist {
		t.Errorf("Exists(%s) == true", "/tmp/abc")
	}

}

func watch(ctx context.Context, name string) {
	for {
		select {
		case <-ctx.Done():
			fmt.Println(name, "监控退出，停止了...")
			return
		default:
			fmt.Println(ctx.Value(key), "goroutine监控中...")
			time.Sleep(2 * time.Second)
		}
	}
}
