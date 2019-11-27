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
import syncerservice.syncerplusredis.cmd.impl.RestoreCommand;
import syncerservice.syncerplusredis.rdb.datatype.EvictType;
import syncerservice.syncerplusredis.cmd.CommandParsers;
import syncerservice.syncerplusredis.util.objectutil.Strings;

import static syncerservice.syncerplusredis.util.objectutil.Strings.isEquals;

/**
 * @author Leon Chen
 * @since 2.1.0
 */
public class RestoreParser implements CommandParser<RestoreCommand> {
    @Override
    public RestoreCommand parse(Object[] command) {
        int idx = 1;
        byte[] key = CommandParsers.toBytes(command[idx]);
        idx++;
        long ttl = CommandParsers.toLong(command[idx++]);
        byte[] serializedValue = CommandParsers.toBytes(command[idx]);
        idx++;
        boolean replace = false;
        boolean absTtl = false;
        EvictType evictType = EvictType.NONE;
        Long evictValue = null;
        for (; idx < command.length; idx++) {
            if (Strings.isEquals(CommandParsers.toRune(command[idx]), "REPLACE")) {
                replace = true;
            } else if (Strings.isEquals(CommandParsers.toRune(command[idx]), "ABSTTL")) {
                absTtl = true;
            } else if (Strings.isEquals(CommandParsers.toRune(command[idx]), "IDLETIME")) {
                evictType = EvictType.LRU;
                idx++;
                evictValue = CommandParsers.toLong(command[idx]);
            } else if (Strings.isEquals(CommandParsers.toRune(command[idx]), "FREQ")) {
                evictType = EvictType.LFU;
                idx++;
                evictValue = CommandParsers.toLong(command[idx]);
            } else {
                throw new UnsupportedOperationException(CommandParsers.toRune(command[idx]));
            }
        }
        return new RestoreCommand(key, ttl, serializedValue, replace, absTtl, evictType, evictValue);
    }
    
}
