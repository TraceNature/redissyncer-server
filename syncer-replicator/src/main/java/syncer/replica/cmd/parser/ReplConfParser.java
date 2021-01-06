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
import syncer.replica.cmd.impl.ReplConfCommand;
import syncer.replica.cmd.impl.ReplConfGetAckCommand;
import syncer.replica.util.objectutil.Strings;


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
