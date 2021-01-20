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

package syncer.transmission.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/4/23
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StartTaskEntity {
    private String code;
    private String taskId;
    private String groupId;
    private String msg;
    private Object data;
}
