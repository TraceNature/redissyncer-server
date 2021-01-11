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

package syncer.transmission.task.circle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2021/1/5
 */
@Data
@Builder
@AllArgsConstructor
public class FlushCommandStatus {
    /**
     1 flushall
     2 flushdb
     -1 none
     */
    @Builder.Default
    private int type=-1;
    private AtomicBoolean status;
    @Builder.Default
    private int db=-1;
    /**
     * 出现次数
     */
    @Builder.Default
    private AtomicInteger num;
}
