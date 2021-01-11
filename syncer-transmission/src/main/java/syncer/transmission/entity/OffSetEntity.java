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

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/15
 */
@Getter
@Setter
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OffSetEntity implements Serializable {
    private static final long serialVersionUID = -5809782578272943997L;
    private String  replId;
    private final AtomicLong replOffset = new AtomicLong(-1);
}
