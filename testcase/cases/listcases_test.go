package cases

import (
	"fmt"
	"testing"
	"time"
)

func TestDisplayCasesList(t *testing.T) {
	DisplayCasesList()

	ticker := time.NewTicker(time.Second * 5)
	defer ticker.Stop()
	for {
		time.Sleep(3 * time.Second)
		fmt.Println("ticker test")
		select {
		case <-ticker.C:
			fmt.Println("ticker be touched")
		default:

		}
	}
}
