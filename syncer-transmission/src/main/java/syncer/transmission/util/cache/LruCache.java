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

package syncer.transmission.util.cache;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 最近最少使用淘汰算法
 * @author zhanenqiang
 * @Description 描述
 * @Date 2019/12/30
 */
public class LruCache <k, v> extends LinkedHashMap<k, v> {
    private final int MAX_SIZE;
    public LruCache(int capcity) {
        super(8, 0.75f,true);
        this.MAX_SIZE = capcity;
    }

    @Override
    public boolean removeEldestEntry(Map.Entry<k, v> eldest) {
        if (size() > MAX_SIZE) {
            v res=eldest.getValue();
        }
        return size() > MAX_SIZE;
    }

}