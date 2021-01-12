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
package syncer.replica.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/3/17
 */
@AllArgsConstructor


public enum SyncType implements Serializable {
    /**
     * replication
     */
    SYNC(1,FileType.SYNC,"replication"),

    RDB(2,FileType.RDB,"RDB文件解析"),

    AOF(3,FileType.AOF,"AOF文件解析"),

    MIXED(4,FileType.MIXED,"混合文件解析"),

    ONLINERDB(5,FileType.ONLINERDB,"在线RDB解析"),

    ONLINEAOF(6,FileType.ONLINEAOF,"在线AOF"),

    ONLINEMIXED(7,FileType.ONLINEMIXED,"在线混合文件解析"),

    COMMANDDUMPUP(8,FileType.COMMANDDUMPUP,"增量命令实时备份");


    /**
     * 数据同步类型
     * 1 sync
     * 2 rdb
     * 3 aof
     * 4 mixed
     * 5 onlineRdb
     * 6 onlineAof
     * 7 onlineMixed
     * 8 commandDumpUp
     */

    @Getter
    private Integer code;
    @Getter
    private FileType fileType;
    @Getter
    private String msg;
}
