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

package syncer.replica.cmd.parser;


import syncer.replica.cmd.CommandParser;
import syncer.replica.cmd.CommandParsers;
import syncer.replica.cmd.impl.AggregateType;
import syncer.replica.cmd.impl.ZInterStoreCommand;
import syncer.replica.util.objectutil.Strings;

/**
 * @author Leon Chen
 * @since 2.1.0
 */
public class ZInterStoreParser implements CommandParser<ZInterStoreCommand> {
    @Override
    public ZInterStoreCommand parse(Object[] command) {
        int idx = 1;
        AggregateType aggregateType = null;
        byte[] destination = CommandParsers.toBytes(command[idx]);
        idx++;
        int numkeys = CommandParsers.toInt(command[idx++]);
        byte[][] keys = new byte[numkeys][];
        for (int i = 0; i < numkeys; i++) {
            keys[i] = CommandParsers.toBytes(command[idx]);
            idx++;
        }
        double[] weights = null;
        while (idx < command.length) {
            String param = CommandParsers.toRune(command[idx]);
            if (Strings.isEquals(param, "WEIGHTS")) {
                idx++;
                weights = new double[numkeys];
                for (int i = 0; i < numkeys; i++) {
                    weights[i] = CommandParsers.toDouble(command[idx++]);
                }
            }
            if (Strings.isEquals(param, "AGGREGATE")) {
                idx++;
                String next = CommandParsers.toRune(command[idx++]);
                if (Strings.isEquals(next, "SUM")) {
                    aggregateType = AggregateType.SUM;
                } else if (Strings.isEquals(next, "MIN")) {
                    aggregateType = AggregateType.MIN;
                } else if (Strings.isEquals(next, "MAX")) {
                    aggregateType = AggregateType.MAX;
                }
            }
        }
        return new ZInterStoreCommand(destination, numkeys, keys, weights, aggregateType);
    }

}
