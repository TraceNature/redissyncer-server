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
import syncer.replica.constant.ThreadStatusEnum;

import java.io.Serializable;

/**
 * @author zhanenqiang
 * @Description 任务运行状态
 * @Date 2020/3/10
 */


@AllArgsConstructor

public enum TaskStatusType implements Serializable {

    /**
     * 停止
     */
    STOP(ThreadStatusEnum.STOP,0,"stop"),
    /**
     * 创建中
     */
    CREATING(ThreadStatusEnum.CREATING,1,"creating"),

    /**
     * 创建完成（完成数据校验阶段）
     */
    CREATED(ThreadStatusEnum.CREATED,2,"created"),

    /**
     * 运行状态
     * 准备拆分 TASK_BEGAIN PSYNC
     */
    RUN(ThreadStatusEnum.RUN,3,"run"),

    /**
     * 任务暂停
     */

    PAUSE(ThreadStatusEnum.PAUSE,4,"pause"),

    /**
     * 任务因异常而停止
     */
    BROKEN(ThreadStatusEnum.BROKEN,5,"broken"),

    /**
     * 全量任务进行中
     */
    RDBRUNING(ThreadStatusEnum.RDBRUNING,6,"rdbrunning"),

    /**
     * 增量任务进行中
     */
    COMMANDRUNING(ThreadStatusEnum.COMMANDRUNING,7,"commandrunning");


    public static TaskStatusType getTaskStatusTypeByName(String name){
        if(name.equalsIgnoreCase(TaskStatusType.CREATING.getMsg())){
            return TaskStatusType.CREATING;
        }else if(name.equalsIgnoreCase(TaskStatusType.CREATED.getMsg())){
            return TaskStatusType.CREATED;
        }else if(name.equalsIgnoreCase(TaskStatusType.RUN.getMsg())){
            return TaskStatusType.RUN;
        }else if(name.equalsIgnoreCase(TaskStatusType.RDBRUNING.getMsg())){
            return TaskStatusType.RDBRUNING;
        }else if(name.equalsIgnoreCase(TaskStatusType.COMMANDRUNING.getMsg())){
            return TaskStatusType.COMMANDRUNING;
        }else if(name.equalsIgnoreCase(TaskStatusType.STOP.getMsg())){
            return TaskStatusType.STOP;
        }else if(name.equalsIgnoreCase(TaskStatusType.BROKEN.getMsg())){
            return TaskStatusType.BROKEN;
        }else if(name.equalsIgnoreCase(TaskStatusType.PAUSE.getMsg())){
            return TaskStatusType.PAUSE;
        }

        return null;
    }

    @Getter
    private ThreadStatusEnum status;
    @Getter
    private int code;
    @Getter
    private String msg;
}
