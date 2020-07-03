package globalzap

import (
	"context"
	"fmt"
	"go.uber.org/zap"
	"go.uber.org/zap/zapcore"
	"gopkg.in/natefinch/lumberjack.v2"
	"testing"
	"time"
)

func TestGetLogger(t *testing.T) {

	log := GetLogger()
	defer log.Sync()
	slog := log.Sugar()
	log.Info("log 初始化成功")
	log.Debug("debugmesg")
	log.Info("无法获取网址",
		zap.String("url", "http://www.baidu.com"),
		zap.Int("attempt", 3),
		zap.Duration("backoff", time.Second))
	slog.Infow("test sugar", "url", "http://example.com",
		"attempt", 3,
		"backoff", time.Second)

	log.Sync()
	ctx, cancel := context.WithCancel(context.Background())

	go generatelog(ctx, "json")

	time.Sleep(3 * time.Second)

	ld := LoggerDefine{
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
		Level:       zap.InfoLevel,
		EncoderType: EcoderConsol,
	}

	fmt.Println(ld.EncoderType)
	ld.SetLogger()
	ld.InitLogger()
	log = GetLogger()
	log.Info("log 初始化成功")
	log.Debug("debugmesg")
	log.Info("无法获取网址",
		zap.String("url", "http://www.baidu.com"),
		zap.Int("attempt", 3),
		zap.Duration("backoff", time.Second))
	go generatelog(ctx, "console")
	time.Sleep(3 * time.Second)
	log.Sync()

	cancel()

}

func generatelog(ctx context.Context, name string) {
	log := GetLogger()
	for {
		select {
		case <-ctx.Done():
			return
		default:
			log.Info(name + ":" + time.Now().String())
		}
		time.Sleep(1 * time.Second)

	}

}
