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
import lombok.NoArgsConstructor;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhanenqiang
 * @Description Task扩展类对应 expandJson 字段
 * @Date 2020/8/24
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExpandTaskModel {
    @Builder.Default
    private String brokenReason="";
    @Builder.Default
    public AtomicLong fileSize=new AtomicLong(0L);
    @Builder.Default
    public AtomicLong readFileSize=new AtomicLong(0L);


}
