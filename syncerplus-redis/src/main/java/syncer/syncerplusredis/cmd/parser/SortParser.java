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

package syncer.syncerplusredis.cmd.parser;

import syncer.syncerplusredis.cmd.CommandParser;
import syncer.syncerplusredis.cmd.impl.Limit;
import syncer.syncerplusredis.cmd.impl.SortCommand;
import syncer.syncerplusredis.cmd.CommandParsers;
import syncer.syncerplusredis.cmd.impl.OrderType;

import java.util.ArrayList;
import java.util.List;

import static syncer.syncerplusredis.util.objectutil.Strings.isEquals;

/**
 * @author Leon Chen
 * @since 2.3.1
 */
public class SortParser implements CommandParser<SortCommand> {
    @Override
    public SortCommand parse(Object[] command) {
        int idx = 1;
        SortCommand sort = new SortCommand();
        byte[] key = CommandParsers.toBytes(command[idx]);
        idx++;
        sort.setKey(key);
        sort.setOrder(OrderType.NONE);
        List<byte[]> getPatterns = new ArrayList<>();
        while (idx < command.length) {
            String param = CommandParsers.toRune(command[idx]);
            if (isEquals(param, "ASC")) {
                sort.setOrder(OrderType.ASC);
            } else if (isEquals(param, "DESC")) {
                sort.setOrder(OrderType.DESC);
            } else if (isEquals(param, "ALPHA")) {
                sort.setAlpha(true);
            } else if (isEquals(param, "LIMIT") && idx + 2 < command.length) {
                idx++;
                long offset = CommandParsers.toLong(command[idx]);
                idx++;
                long count = CommandParsers.toLong(command[idx]);
                sort.setLimit(new Limit(offset, count));
            } else if (isEquals(param, "STORE") && idx + 1 < command.length) {
                idx++;
                byte[] destination = CommandParsers.toBytes(command[idx]);
                sort.setDestination(destination);
            } else if (isEquals(param, "BY") && idx + 1 < command.length) {
                idx++;
                byte[] byPattern = CommandParsers.toBytes(command[idx]);
                sort.setByPattern(byPattern);
            } else if (isEquals(param, "GET") && idx + 1 < command.length) {
                idx++;
                byte[] getPattern = CommandParsers.toBytes(command[idx]);
                getPatterns.add(getPattern);
            }
            idx++;
        }
        sort.setGetPatterns(getPatterns.toArray(new byte[getPatterns.size()][]));
        return sort;
    }
}
