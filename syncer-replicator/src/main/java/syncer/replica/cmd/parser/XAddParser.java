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
import syncer.replica.cmd.impl.MaxLen;
import syncer.replica.cmd.impl.XAddCommand;
import syncer.replica.util.objectutil.ByteArrayMap;

import java.util.Objects;

import static syncer.replica.util.objectutil.Strings.isEquals;


/**
 * @author Leon Chen
 * @since 2.6.0
 */
public class XAddParser implements CommandParser<XAddCommand> {
    @Override
    public XAddCommand parse(Object[] command) {
        int idx = 1;
        byte[] key = CommandParsers.toBytes(command[idx]);
        idx++;
        MaxLen maxLen = null;
        if (isEquals(CommandParsers.toRune(command[idx]), "MAXLEN")) {
            idx++;
            boolean approximation = false;
            if (Objects.equals(CommandParsers.toRune(command[idx]), "~")) {
                approximation = true;
                idx++;
            } else if (Objects.equals(CommandParsers.toRune(command[idx]), "=")) {
                idx++;
            }
            long count = CommandParsers.toLong(command[idx]);
            idx++;
            maxLen = new MaxLen(approximation, count);
        }
        byte[] id = CommandParsers.toBytes(command[idx]);
        idx++;
        ByteArrayMap fields = new ByteArrayMap();
        while (idx < command.length) {
            byte[] field = CommandParsers.toBytes(command[idx]);
            idx++;
            byte[] value = idx == command.length ? null : CommandParsers.toBytes(command[idx]);
            idx++;
            fields.put(field, value);
        }
        return new XAddCommand(key, maxLen, id, fields);
    }
}
