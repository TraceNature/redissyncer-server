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
import syncerservice.syncerplusredis.cmd.impl.SAddCommand;
import syncerservice.syncerplusredis.cmd.CommandParsers;

/**
 * @author Leon Chen
 * @since 2.1.0
 */
public class SAddParser implements CommandParser<SAddCommand> {


    @Override
    public SAddCommand parse(Object[] command) {
        int idx = 1, newIdx = 0;
        byte[] key = CommandParsers.toBytes(command[idx]);
        idx++;
        byte[][] members = new byte[command.length - 2][];
        while (idx < command.length) {
            members[newIdx] = CommandParsers.toBytes(command[idx]);
            newIdx++;
            idx++;
        }
        return new SAddCommand(key, members);
    }

}
