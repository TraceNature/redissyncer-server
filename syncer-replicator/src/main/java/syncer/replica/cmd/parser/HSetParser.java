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
import syncer.replica.cmd.impl.HSetCommand;
import syncer.replica.util.objectutil.ByteArrayMap;

import static syncer.replica.cmd.CommandParsers.toBytes;

/**
 * @author Leon Chen
 * @since 2.1.0
 */
public class HSetParser implements CommandParser<HSetCommand> {

    @Override
    @SuppressWarnings("deprecation")
    public HSetCommand parse(Object[] command) {
        int idx = 1;
        byte[] key = toBytes(command[idx]);
        idx++;
        ByteArrayMap fields = new ByteArrayMap();
        byte[] firstField = null;
        byte[] firstValue = null;
        while (idx < command.length) {
            byte[] field = toBytes(command[idx]);
            idx++;
            byte[] value = idx == command.length ? null : toBytes(command[idx]);
            idx++;
            if (firstField == null) {
                firstField = field;
            }
            if (firstValue == null) {
                firstValue = value;
            }
            fields.put(field, value);
        }
        HSetCommand hSetCommand =  new HSetCommand(key, fields);
        hSetCommand.setField(firstField);
        hSetCommand.setValue(firstValue);
        return hSetCommand;
    }
}
