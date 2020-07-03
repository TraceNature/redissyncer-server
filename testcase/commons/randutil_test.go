package commons

import (
	"fmt"
	"testing"
)

func TestStringWithCharset(t *testing.T) {
	var randing = RandString(10)
	fmt.Println(randing)
}

func TestGetUUID(t *testing.T) {
	fmt.Println(GetUUID())
}
