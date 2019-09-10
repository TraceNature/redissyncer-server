package com.i1314i.syncerplusservice.entity.thread;

import com.i1314i.syncerplusservice.constant.ThreadStatusEnum;
import com.i1314i.syncerplusservice.entity.dto.RedisClusterDto;
import com.moilioncircle.redis.replicator.Replicator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor

@EqualsAndHashCode

public class ThreadReturnMsgEntity {
    private String id;
    private String threadName;
    private ThreadStatusEnum status;
}
