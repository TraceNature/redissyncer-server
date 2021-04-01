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

package syncer.transmission.po.entity;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import syncer.replica.event.Event;
import syncer.replica.type.FileType;
import syncer.replica.util.TaskRunTypeEnum;
import syncer.transmission.compensator.ISyncerCompensator;
import syncer.transmission.entity.OffSetEntity;

import java.io.Serializable;
import java.util.Map;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/22
 */
@Builder
@Getter
@Setter
@EqualsAndHashCode
public class KeyValueEventEntity implements Serializable {
    private Event event;
    /**
     * 映射之后的dbNum
     */
    private Long dbNum;

    /**
     * 剩余过期时间
     */
    private Long ms;

    /**
     * db映射关系
     */
    private Map<Integer,Integer> dbMapper;

    /**
     * redis版本
     */
    private double redisVersion;

    /**
     * 类型
     */
    private TaskRunTypeEnum taskRunTypeEnum;


    /**
     * 增量offset
     */
    private OffSetEntity baseOffSet;

    //configuration
//    private Configuration configuration;

    /**
     * 服务器id
     */
    private String replId = "?";

    /**
     * offset
     */
    private  Long replOffset = -1L;

    /**
     * fileType
     */
    private FileType fileType= FileType.SYNC;

    /**
     * 补偿机制
     */
    private ISyncerCompensator iSyncerCompensator;

    /**
     * 批次总大小
     */
    private KeyValueSizeEntity keyValueSizeEntity;



}