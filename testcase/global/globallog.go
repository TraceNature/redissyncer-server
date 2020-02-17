package global

import (
	"github.com/sirupsen/logrus"
	"os"
	"sync"
)

var (
	log     *logrus.Logger
	initLog sync.Once
)

func GetInstance() *logrus.Logger {
	initLog.Do(func() {
		log = logrus.New()
		log.Formatter = &logrus.TextFormatter{}
		log.Out = os.Stdout
		log.Level = logrus.DebugLevel
	})
	return log
}

// SetLog 设置log
func SetLog(l *logrus.Logger) {
	log = l
}

// WithField 使用全局log返回logrus.Entry指针
func WithField(key string, value interface{}) *logrus.Entry {
	return log.WithField(key, value)
}

func WithFields(fields logrus.Fields) *logrus.Entry {
	return log.WithFields(fields)
}

// Debug 使用全局log记录信息
func Debug(args ...interface{}) {
	log.Debug(args...)
}
