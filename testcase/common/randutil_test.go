package common

import (
	"fmt"
	"testing"
)

func TestStringWithCharset(t *testing.T) {
	var randing = RandString(10)
	fmt.Println(randing)

}
