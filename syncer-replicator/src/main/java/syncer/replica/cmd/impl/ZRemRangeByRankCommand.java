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
public class ZRemRangeByRankCommand extends GenericKeyCommand {

    private static final long serialVersionUID = 1L;

    private long start;
    private long stop;

    public ZRemRangeByRankCommand() {
    }

    public ZRemRangeByRankCommand(byte[] key, long start, long stop) {
        super(key);
        this.start = start;
        this.stop = stop;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getStop() {
        return stop;
    }

    public void setStop(long stop) {
        this.stop = stop;
    }
}
