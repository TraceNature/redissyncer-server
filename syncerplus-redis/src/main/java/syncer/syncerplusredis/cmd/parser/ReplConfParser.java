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
import syncer.syncerplusredis.cmd.impl.ReplConfCommand;
import syncer.syncerplusredis.cmd.impl.ReplConfGetAckCommand;
import syncer.syncerplusredis.cmd.CommandParsers;
import syncer.syncerplusredis.util.objectutil.Strings;

import static syncer.syncerplusredis.util.objectutil.Strings.isEquals;

/**
 * @author Leon Chen
 * @since 2.6.0
 */
public class ReplConfParser implements CommandParser<ReplConfCommand> {
    @Override
    public ReplConfCommand parse(Object[] command) {
        int idx = 1;
        String type = CommandParsers.toRune(command[idx]);
        idx++;
        if (Strings.isEquals(type, "GETACK")) {
            return new ReplConfGetAckCommand();
        } else {
            throw new AssertionError("parse [REPLCONF] command error." + type);
        }
    }
}
