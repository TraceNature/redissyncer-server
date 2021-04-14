package syncer.common.constant;

/**
 * 断点续传类型
 */
public enum BreakpointContinuationType {
    /**
     * v1,基于syncer自身机制记录的 offset
     */
    v1,
    /**
     * v2 尽最大可能保证offset正确性
     * 为每个提交批次封装事务，并加入checkpoint点
     * 断点续传时寻找目标redis所有库中最大的checkpoint 中offset值
     * 若不存在时使用v1策略兜底
     */
    v2
}
