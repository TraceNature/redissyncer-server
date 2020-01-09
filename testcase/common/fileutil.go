package common

import (
	"bufio"
	"bytes"
	"fmt"
	"os"
)

//AppendLineToFile 向文件追加行
func AppendLineToFile(line bytes.Buffer, filename string) {

	f, err := os.OpenFile(filename, os.O_APPEND|os.O_CREATE|os.O_WRONLY, 0644)
	if err != nil {
		panic(err)
	}

	defer f.Close()
	w := bufio.NewWriter(f)
	fmt.Fprintln(w, line.String())
	w.Flush()
}

// Exists 用于判断所给路径文件或文件夹是否存在
func FileExists(path string) bool {
	_, err := os.Stat(path) //os.Stat获取文件信息
	if err != nil {
		if os.IsExist(err) {
			return true
		}
		return false
	}
	return true
}

// IsDir 判断所给路径是否为文件夹
func IsDir(path string) bool {
	s, err := os.Stat(path)
	if err != nil {
		return false
	}
	return s.IsDir()
}

//IsFile 判断所给路径是否为文件
func IsFile(path string) bool {
	return !IsDir(path)
}
