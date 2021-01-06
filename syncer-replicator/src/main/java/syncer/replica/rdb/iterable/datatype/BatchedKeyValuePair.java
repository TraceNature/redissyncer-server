/*
 * Copyright 2016-2018 Leon Chen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package syncer.replica.rdb.iterable.datatype;

import syncer.replica.rdb.datatype.KeyValuePair;

/**
 * @author Leon Chen
 * @since 3.0.0
 */
public class BatchedKeyValuePair<K, V> extends KeyValuePair<K, V> {
    
    private static final long serialVersionUID = 1L;
    
    private int batch;
    private boolean last;
    
    public int getBatch() {
        return batch;
    }
    
    public void setBatch(int batch) {
        this.batch = batch;
    }
    
    public boolean isLast() {
        return last;
    }
    
    public void setLast(boolean last) {
        this.last = last;
    }
}
