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

package syncerservice.syncerplusredis.cmd.parser;

import syncerservice.syncerplusredis.cmd.CommandParser;
import syncerservice.syncerplusredis.cmd.impl.LInsertCommand;
import syncerservice.syncerplusredis.cmd.impl.LInsertType;
import syncerservice.syncerplusredis.cmd.CommandParsers;

import static syncerservice.syncerplusredis.util.objectutil.Strings.isEquals;

/**
 * @author Leon Chen
 * @since 2.1.0
 */
public class LInsertParser implements CommandParser<LInsertCommand> {
    @Override
    public LInsertCommand parse(Object[] command) {
        int idx = 1;
        LInsertType lInsertType = null;
        byte[] key = CommandParsers.toBytes(command[idx]);
        idx++;
        String keyword = CommandParsers.toRune(command[idx++]);
        if (isEquals(keyword, "BEFORE")) {
            lInsertType = LInsertType.BEFORE;
        } else if (isEquals(keyword, "AFTER")) {
            lInsertType = LInsertType.AFTER;
        }
        byte[] pivot = CommandParsers.toBytes(command[idx]);
        idx++;
        byte[] value = CommandParsers.toBytes(command[idx]);
        idx++;
        return new LInsertCommand(key, lInsertType, pivot, value);
    }

}
