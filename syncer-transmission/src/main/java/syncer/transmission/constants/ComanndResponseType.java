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

package syncer.transmission.constants;

import lombok.*;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/7/10
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ComanndResponseType {
    /**
     *  1 OK
     *  2 LONG >=0
     *  3 LONG >=0 & <0
     *  4 String
     *  5 Double
     *  6 ArrayList
     *  7 HashSet
     *  8 PONG
     *  9 Long Double
     */
    int type;
    String command;
    String commandResponse;
}
