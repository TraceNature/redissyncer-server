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

public enum TaskType implements Serializable {

    /**
     *任务类型
     * default total   1  全量＋增量
     *  stockonly      2  全量
     *  incrementonly  3  增量
     */
    TOTAL(1,TaskRunTypeEnum.TOTAL,"全量+增量"),
    STOCKONLY(2,TaskRunTypeEnum.STOCKONLY,"只全量"),
    INCREMENTONLY(3,TaskRunTypeEnum.INCREMENTONLY,"只增量");

    @Getter
    private int code;
    @Getter
    private TaskRunTypeEnum type;
    @Getter
    private String msg;
}
