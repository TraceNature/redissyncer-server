package cases

import (
	"testing"
	"time"
)

func TestBaseCase(t *testing.T) {
	duration := 5 * time.Second
	genreport := false
	creattaskfile := "../createtask.json"
	loopstep := 1
	syncserver := "http://114.67.67.7:8080"

	BaseCase(duration, genreport, creattaskfile, int64(loopstep), syncserver)

}
