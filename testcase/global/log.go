package global

// UseLog 演示了使用全局 log
func UseLog() error {
	logger := GetInstance()

	// 如果在其他包 应该调用global.WithField 和 global.Debug
	logger.WithField("key", "value").Debug("hello")
	logger.Debug("test")

	return nil
}
