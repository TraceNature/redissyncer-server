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

import lombok.*;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/25
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class ListTaskParamDto {
    @NotBlank(message = "regulation不能为空")
    private String regulation;
    @Builder.Default
    private List<String> tasknames = new ArrayList<>();

    private String taskstatus;
    @Builder.Default
    private List<String> taskids = new ArrayList<>();
    @Builder.Default
    private List<String> groupIds = new ArrayList<>();
    @Builder.Default
    int currentPage = 1;
    @Builder.Default
    int pageSize = 10;
}
