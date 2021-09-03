// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// See the License for the specific language governing permissions and
// limitations under the License.

package syncer.transmission.model;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;
import syncer.replica.replication.Replication;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhanenqiang
 * @Description 双向同步
 * @Date 2021/1/5
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MultiTaskModel {


    /**
     * 双向任务Id
     */
    private String taskId;
    /**
     * 同步任务id
     */
    private String parentId;

    /**
     * 任务节点Id
     */
    private String nodeId;

    /**
     * 反向任务节点id
     */
    private String targetNodeId;


    /**
     * 任务名称
     */
    private String taskName;

    /**
     * RedisUri
     */
    private String redisAddress;


    /**
     * redis host
     */
    private String host;


    /**
     * acl
     */
    @Builder.Default
    private boolean acl=false;

    /**
     * 用户名
     */
    @Builder.Default
    private String userName="";

    /**
     * Redis密码
     */
    private String password;

    /**
     * 端口
     */
    @Builder.Default
    private Integer port=6379;

    /**
     * 错误数据总数  30L
     */
    @Builder.Default
    private volatile Long errorCount=1L;

    /**
     * 被抛弃key阈值
     */
    @Builder.Default
    private AtomicLong errorNums = new AtomicLong(0L);



    /**
     * 目标Redis类型
     * 1 单机
     * 2 cluster
     */
    @Builder.Default
    private Integer targetRedisType=1;


    /**
     * md5
     */
    private String md5;

    /**
     * 全量数据分析报告
     */
    private String dataAnalysis;

    /**
     * replId
     */
    @Builder.Default
    private String replId="";

    /**
     * offset地址
     */
    @Builder.Default
    private volatile Long offset=-1L;

    /**
     * 任务状态
     *
     * CREATING,CREATE,RUN,STOP,PAUSE,BROKEN
     *
     *  STOP        0      停止
     *  CREATING    1      创建中
     *  CREATED      2      创建完成（完成任务信息校验进入启动阶段）
     *  RUN         3      任务启动完成，进入运行状态
     *  PAUSE       4      任务暂停
     *  BROKEN      5      任务因异常停止
     *  RDBRUNING   6      全量任务进行中
     *  COMMANDRUNING 7    增量任务进行中
     */
    @Builder.Default
    private Integer status=0;

    /**
     * redis版本
     */
    private  double redisVersion;

    /**
     * rdb版本
     */
    @Builder.Default
    private Integer rdbVersion=6;

    /**
     * 数据同步类型  ---->SyncType
     * 1 sync (默认双活支持)
     * 2 rdb
     * 3 aof
     * 4 mixed
     * 5 onlineRdb
     * 6 onlineAof
     * 7 onlineMixed
     * 8 commandDumpUp
     */
    @Builder.Default
    private Integer syncType=1;


    /**
     * db库映射关系
     */
    private String dbMapper;
    /**
     * 子任务创建时间
     */
    private String createTime;


    /**
     * 更新时间
     */
    private String updateTime;


    /**
     * 全量key的数量
     */
    @Builder.Default
    private volatile Long rdbKeyCount=0L;

    /**
     * 从运行到现在的key总量
     */
    @Builder.Default
    private volatile Long allKeyCount=0L;

    /**
     * 同步到目标的key数量
     */
    @Builder.Default
    private volatile Long realKeyCount=0L;

    /**
     * 当前Key的最后一次更新时间（数据流入）
     */
    private volatile  long lastKeyUpdateTime=0L;

    /**
     * 当前Key的最后一次pipeline提交时间（数据流出）
     */
    private volatile long lastKeyCommitTime=0L;


    /**
     * 扩展字段
     */
    @Builder.Default
    private String expandJson="";


    /**
     * replication对象引用
     */

    private Replication replication;


    public String getExpandJson() {
        if (StringUtils.isEmpty(this.expandJson)){
            this.expandJson= JSON.toJSONString(new ExpandTaskModel());
        }
        return this.expandJson;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }
        if (o == null || getClass() != o.getClass()){
            return false;
        }

        MultiTaskModel that = (MultiTaskModel) o;

        if (acl != that.acl) {
            return false;
        }
        if (Double.compare(that.redisVersion, redisVersion) != 0) {
            return false;
        }
        if (lastKeyUpdateTime != that.lastKeyUpdateTime) {
            return false;
        }
        if (lastKeyCommitTime != that.lastKeyCommitTime) {
            return false;
        }
        if (!taskId.equals(that.taskId)) {
            return false;
        }
        if (!parentId.equals(that.parentId)) {
            return false;
        }
        if (!nodeId.equals(that.nodeId)) {
            return false;
        }
        if (!taskName.equals(that.taskName)) {
            return false;
        }
        if (!redisAddress.equals(that.redisAddress)) {
            return false;
        }
        if (!host.equals(that.host)) {
            return false;
        }
        if (!userName.equals(that.userName)) {
            return false;
        }
        if (!password.equals(that.password)) {
            return false;
        }
        if (!port.equals(that.port)) {
            return false;
        }
        if (!errorCount.equals(that.errorCount)) {
            return false;
        }
        if (!errorNums.equals(that.errorNums)) {
            return false;
        }
        if (!targetRedisType.equals(that.targetRedisType)) {
            return false;
        }
        if (!md5.equals(that.md5)) {
            return false;
        }
        if (!dataAnalysis.equals(that.dataAnalysis)) {
            return false;
        }
        if (!replId.equals(that.replId)) {
            return false;
        }
        if (!offset.equals(that.offset)) {
            return false;
        }
        if (!status.equals(that.status)){
            return false;
        }
        if (!rdbVersion.equals(that.rdbVersion)) {
            return false;
        }
        if (!syncType.equals(that.syncType)){
            return false;
        }
        if (!dbMapper.equals(that.dbMapper)) {
            return false;
        }
        if (!createTime.equals(that.createTime)){
            return false;
        }
        if (!updateTime.equals(that.updateTime)) {
            return false;
        }
        if (!rdbKeyCount.equals(that.rdbKeyCount)) {
            return false;
        }
        if (!allKeyCount.equals(that.allKeyCount)) {
            return false;
        }
        if (!realKeyCount.equals(that.realKeyCount)){
            return false;
        }
        return expandJson.equals(that.expandJson);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = taskId.hashCode();
        result = 31 * result + parentId.hashCode();
        result = 31 * result + nodeId.hashCode();
        result = 31 * result + taskName.hashCode();
        result = 31 * result + redisAddress.hashCode();
        result = 31 * result + host.hashCode();
        result = 31 * result + (acl ? 1 : 0);
        result = 31 * result + userName.hashCode();
        result = 31 * result + password.hashCode();
        result = 31 * result + port.hashCode();
        result = 31 * result + errorCount.hashCode();
        result = 31 * result + errorNums.hashCode();
        result = 31 * result + targetRedisType.hashCode();
        result = 31 * result + md5.hashCode();
        result = 31 * result + dataAnalysis.hashCode();
        result = 31 * result + replId.hashCode();
        result = 31 * result + offset.hashCode();
        result = 31 * result + status.hashCode();
        temp = Double.doubleToLongBits(redisVersion);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + rdbVersion.hashCode();
        result = 31 * result + syncType.hashCode();
        result = 31 * result + dbMapper.hashCode();
        result = 31 * result + createTime.hashCode();
        result = 31 * result + updateTime.hashCode();
        result = 31 * result + rdbKeyCount.hashCode();
        result = 31 * result + allKeyCount.hashCode();
        result = 31 * result + realKeyCount.hashCode();
        result = 31 * result + (int) (lastKeyUpdateTime ^ (lastKeyUpdateTime >>> 32));
        result = 31 * result + (int) (lastKeyCommitTime ^ (lastKeyCommitTime >>> 32));
        result = 31 * result + expandJson.hashCode();
        return result;
    }
}