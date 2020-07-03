package globalzap

import (
	"testcase/commons"
	"fmt"
	"go.uber.org/zap"
	"go.uber.org/zap/zapcore"
	"gopkg.in/natefinch/lumberjack.v2"
	"os"
	"sync"
)

var (
	logger *zap.Logger
	l      = &sync.RWMutex{}
)

type EncoderType int

const (
	EcoderJson EncoderType = iota - 1
	EcoderConsol
)

func (e EncoderType) String() string {
	switch e {
	case EcoderJson:
		return "json"
	case EcoderConsol:
		return "console"
	default:
		return fmt.Sprintf("Econder(%d)", e)
	}
}

type LoggerDefine struct {
	Hook          lumberjack.Logger
	EncoderConfig zapcore.EncoderConfig
	Level         zapcore.Level
	EncoderType   EncoderType
	Caller        bool //是否开启开发模式，堆栈跟踪
}

var defaultloggerdefine = &LoggerDefine{
	Hook: lumberjack.Logger{
		Filename:   "./logs/spikeProxy1.log", // 日志文件路径
		MaxSize:    128,                      // 每个日志文件保存的最大尺寸 单位：M
		MaxBackups: 30,                       // 日志文件最多保存多少个备份
		MaxAge:     7,                        // 文件最多保存多少天
		Compress:   true,                     // 是否压缩
	},
	EncoderConfig: zapcore.EncoderConfig{
		TimeKey:        "time",
		LevelKey:       "level",
		NameKey:        "logger",
		CallerKey:      "linenum",
		MessageKey:     "msg",
		StacktraceKey:  "stacktrace",
		LineEnding:     zapcore.DefaultLineEnding,
		EncodeLevel:    zapcore.LowercaseLevelEncoder,  // 小写编码器
		EncodeTime:     zapcore.ISO8601TimeEncoder,     // ISO8601 UTC 时间格式
		EncodeDuration: zapcore.SecondsDurationEncoder, //
		EncodeCaller:   zapcore.FullCallerEncoder,      // 全路径编码器
		EncodeName:     zapcore.FullNameEncoder,
	},
	Level:       zap.DebugLevel,
	EncoderType: EcoderConsol,
	Caller:      false,
}

func GetLogger() *zap.Logger {
	if commons.IsNil(logger) {
		l.Lock()
		defer l.Unlock()
		if commons.IsNil(logger) {
			defaultloggerdefine.InitLogger()
		}
	}

	return logger
}

func (d *LoggerDefine) SetLogger() {
	defaultloggerdefine = d
}

func (d *LoggerDefine) InitLogger() {

	// 设置日志级别
	atomicLevel := zap.NewAtomicLevel()
	//atomicLevel.SetLevel(zap.InfoLevel)
	atomicLevel.SetLevel(d.Level)

	//定义encoder

	var encoder zapcore.Encoder
	switch d.EncoderType {
	case EcoderJson:
		encoder = zapcore.NewJSONEncoder(d.EncoderConfig)
	case EcoderConsol:
		encoder = zapcore.NewConsoleEncoder(d.EncoderConfig)
	default:
		encoder = zapcore.NewJSONEncoder(d.EncoderConfig)
	}

	if d.EncoderType == EcoderConsol {
		encoder = zapcore.NewConsoleEncoder(d.EncoderConfig)
	}
	core := zapcore.NewCore(

		encoder,
		zapcore.NewMultiWriteSyncer(zapcore.AddSync(os.Stdout), zapcore.AddSync(&(d.Hook))), // 打印到控制台和文件
		atomicLevel,                                                                         // 日志级别
	)

	options := []zap.Option{}
	// 开启开发模式，堆栈跟踪
	caller := zap.AddCaller()

	if d.Caller {
		options = append(options, caller)
	}
	// 开启文件及行号
	//development := zap.Development()
	// 设置初始化字段
	//filed := zap.Fields(zap.String("serviceName", "serviceName"))
	// 构造日志
	logger = zap.New(core, options...)

}
