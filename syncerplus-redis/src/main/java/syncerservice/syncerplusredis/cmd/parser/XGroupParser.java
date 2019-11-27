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
import syncerservice.syncerplusredis.cmd.CommandParsers;
import syncerservice.syncerplusredis.cmd.impl.*;

import static syncerservice.syncerplusredis.util.objectutil.Strings.isEquals;

/**
 * @author Leon Chen
 * @since 2.6.0
 */
public class XGroupParser implements CommandParser<XGroupCommand> {
    @Override
    public XGroupCommand parse(Object[] command) {
        int idx = 1;
        String next = CommandParsers.toRune(command[idx++]);
        if (isEquals(next, "CREATE")) {
            byte[] key = CommandParsers.toBytes(command[idx]);
            idx++;
            byte[] group = CommandParsers.toBytes(command[idx]);
            idx++;
            byte[] id = CommandParsers.toBytes(command[idx]);
            idx++;
            if (idx >= command.length) {
                return new XGroupCreateCommand(key, group, id, false);
            } else {
                next = CommandParsers.toRune(command[idx++]);
                if (isEquals(next, "MKSTREAM")) {
                    return new XGroupCreateCommand(key, group, id, true);
                } else {
                    throw new UnsupportedOperationException(next);
                }
            }
        } else if (isEquals(next, "SETID")) {
            byte[] key = CommandParsers.toBytes(command[idx]);
            idx++;
            byte[] group = CommandParsers.toBytes(command[idx]);
            idx++;
            byte[] id = CommandParsers.toBytes(command[idx]);
            idx++;
            return new XGroupSetIdCommand(key, group, id);
        } else if (isEquals(next, "DESTROY")) {
            byte[] key = CommandParsers.toBytes(command[idx]);
            idx++;
            byte[] group = CommandParsers.toBytes(command[idx]);
            idx++;
            return new XGroupDestroyCommand(key, group);
        } else if (isEquals(next, "DELCONSUMER")) {
            byte[] key = CommandParsers.toBytes(command[idx]);
            idx++;
            byte[] group = CommandParsers.toBytes(command[idx]);
            idx++;
            byte[] consumer = CommandParsers.toBytes(command[idx]);
            idx++;
            return new XGroupDelConsumerCommand(key, group, consumer);
        } else {
            throw new UnsupportedOperationException(next);
        }
    }
}
