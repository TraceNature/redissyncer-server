package generatedata

import (
	"context"
	"math/rand"
	"strconv"
	"time"
)

//随机执行一个基础操作
func (bo *BaseOpt) ExecRandOpt() {
	bo.OptType = BaseOptArray[rand.Intn(len(BaseOptArray))]
	bo.ExecOpt()
}

//随机执行一个基础操作半数概率为Set操作
func (bo *BaseOpt) ExecRandOptHalfIsSet() {
	if rand.Int()%2 == 0 {
		bo.OptType = BO_SET_SETNX
		bo.ExecOpt()
	} else {
		bo.ExecRandOpt()
	}
}

//遍历执行所有基本操作
func (bo *BaseOpt) ExecAllBasicOpt() {
	for _, v := range BaseOptArray {
		bo.OptType = v
		bo.ExecOpt()
	}
}

//持续随机执行基础操作
func (bo *BaseOpt) KeepExecBasicOpt(ctx context.Context, sleeptime time.Duration) {
	i := int64(0)
	keysuffix := bo.KeySuffix
	for {
		randi := rand.Intn(2 * len(BaseOptArray))
		bo.KeySuffix = keysuffix + strconv.FormatInt(i, 10)
		if randi < len(BaseOptArray) {
			bo.OptType = BaseOptArray[randi]
			bo.ExecRandOpt()
		} else {
			bo.OptType = BO_SET_SETNX
			bo.ExecRandOpt()
		}
		i++
		time.Sleep(sleeptime)
		select {
		case <-ctx.Done():
			return
		default:
			continue
		}
	}

}
