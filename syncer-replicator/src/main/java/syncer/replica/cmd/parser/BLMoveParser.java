/*
 * Copyright 2016-2017 Leon Chen
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
import syncer.replica.cmd.impl.BLMoveCommand;
import syncer.replica.cmd.impl.blmove.DirectionType;

import static syncer.replica.util.objectutil.Strings.isEquals;

/**
 * @author Leon Chen
 * @since 3.5.0
 */
public class BLMoveParser implements CommandParser<BLMoveCommand> {
    
    @Override
    public BLMoveCommand parse(Object[] command) {
        int idx = 1;
        byte[] source = CommandParsers.toBytes(command[idx++]);
        byte[] destination =  CommandParsers.toBytes(command[idx++]);
        DirectionType from = parseDirection(CommandParsers.toRune(command[idx++]));
        DirectionType to = parseDirection(CommandParsers.toRune(command[idx++]));
        return new BLMoveCommand(source, destination, from, to);
    }
    
    private DirectionType parseDirection(String direction) {
        if (isEquals(direction, "LEFT")) {
            return DirectionType.LEFT;
        } else if (isEquals(direction, "RIGHT")) {
            return DirectionType.RIGHT;
        } else {
            throw new AssertionError("parse [BLMOVE] command error." + direction);
        }
    }
}
