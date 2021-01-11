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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author zhanenqiang
 * @Description 数据补偿类
 * @Date 2020/4/26
 */
@Data
@AllArgsConstructor
@Builder
public class DataCompensationModel {
    private int id;
    private String taskId;
    private String groupId;
    private String command;
    private String value;
    private String key;
    private int times;
    private String createTime;
}
