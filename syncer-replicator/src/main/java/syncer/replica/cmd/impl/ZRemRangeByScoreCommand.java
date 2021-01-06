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

package syncer.replica.cmd.impl;

/**
 * @author Leon Chen
 * @since 2.1.1
 */
public class ZRemRangeByScoreCommand extends GenericKeyCommand {

    private static final long serialVersionUID = 1L;

    private byte[] min;
    private byte[] max;

    public ZRemRangeByScoreCommand() {
    }

    public ZRemRangeByScoreCommand(byte[] key, byte[] min, byte[] max) {
        super(key);
        this.min = min;
        this.max = max;
    }

    public byte[] getMin() {
        return min;
    }

    public void setMin(byte[] min) {
        this.min = min;
    }

    public byte[] getMax() {
        return max;
    }

    public void setMax(byte[] max) {
        this.max = max;
    }
}
