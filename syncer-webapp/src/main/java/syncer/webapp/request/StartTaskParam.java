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

package syncer.webapp.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/17
 */
@Getter
@Setter
@ApiModel(value = "启动任务", description = "启动任务")
public class StartTaskParam {
    @ApiModelProperty(value = "任务id", required = true, example = "DE034278589D47FAB92D3B3DCBC668D1")
    private String taskid;

    @ApiModelProperty(value = "任务组id",  example = "DE034278589D47FAB92D3B3DCBC668D1")
    private String groupId;

    @ApiModelProperty(value = "是否从头开始同步任务")
    @Builder.Default
    private boolean afresh = true;


}
