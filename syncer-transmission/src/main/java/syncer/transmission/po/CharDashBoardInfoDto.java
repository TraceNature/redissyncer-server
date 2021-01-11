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

package syncer.transmission.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/10
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CharDashBoardInfoDto {
    private Integer syncCount;
    private Integer rdbCount;
    private Integer onlineRdbCount;
    private Integer aofCount;
    private Integer onlineAofCount;
    private Integer mixedCount;
    private Integer onlineMixedCount;
}
