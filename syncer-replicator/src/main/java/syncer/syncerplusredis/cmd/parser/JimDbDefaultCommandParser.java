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
import syncer.syncerplusredis.cmd.impl.DefaultCommand;
import syncer.syncerplusredis.util.objectutil.Strings;

/**
 * [transmit bj 3 set y y]
 * JimDb增量命令解析
 */
public class JimDbDefaultCommandParser implements CommandParser<DefaultCommand> {
    @Override
    public DefaultCommand parse(Object[] command) {
        byte[][] args = new byte[command.length - 4][];
        for (int i = 4, j = 0; i < command.length; i++) {
            if (command[i] instanceof Long) {
                args[j++] = String.valueOf(command[i]).getBytes();
            } else if (command[i] instanceof byte[]) {
                args[j++] = (byte[]) command[i];
            } else if (command[i] instanceof Object[]) {
                throw new UnsupportedOperationException(Strings.format(command));
            }
        }
        return new DefaultCommand((byte[]) command[3], args);
    }
}
