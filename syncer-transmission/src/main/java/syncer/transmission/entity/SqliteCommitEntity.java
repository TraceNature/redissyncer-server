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

import lombok.*;

/**
 * @author zhanenqiang
 * @Description sqlite串行化
 * @Date 2020/7/23
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SqliteCommitEntity {
    private Integer type;
    private Object object;
    private String msg;
}
