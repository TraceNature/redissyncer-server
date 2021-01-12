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
package syncer.replica.constant;

/**
 * @author zhanenqiang
 * @Description 同步类型【全量 增量】
 * @Date 2020/12/18
 */
public enum  SyncStatusType {
    /**
     * 存量同步状态
     */
    RdbSync(1,"存量同步状态"),
    /**
     * 增量同步状态
     */
    CommandSync(2,"增量同步状态");

    SyncStatusType(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    private Integer code;
    private String msg;
}
