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
import syncer.replica.cmd.impl.XClaimCommand;
import syncer.replica.util.objectutil.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * @author Leon Chen
 * @since 2.6.0
 */
public class XClaimParser implements CommandParser<XClaimCommand> {
    @Override
    public XClaimCommand parse(Object[] command) {
        int idx = 1;
        byte[] key = CommandParsers.toBytes(command[idx]);
        idx++;
        byte[] group = CommandParsers.toBytes(command[idx]);
        idx++;
        byte[] consumer = CommandParsers.toBytes(command[idx]);
        idx++;
        long minIdle = CommandParsers.toLong(command[idx++]);
        List<byte[]> ids = new ArrayList<>();
        for (; idx < command.length; idx++) {
            byte[] id = CommandParsers.toBytes(command[idx]);
            if (!validId(id)){
                break;
            }
            ids.add(id);
        }
        Long idle = null;
        Long time = null;
        Long retryCount = null;
        boolean force = false;
        boolean justId = false;
        byte[] lastId = null;
        while (idx < command.length) {
            String next = CommandParsers.toRune(command[idx]);
            if (Strings.isEquals(next, "IDLE")) {
                idx++;
                idle = CommandParsers.toLong(command[idx]);
                idx++;
            } else if (Strings.isEquals(next, "TIME")) {
                idx++;
                time = CommandParsers.toLong(command[idx]);
                idx++;
            } else if (Strings.isEquals(next, "RETRYCOUNT")) {
                idx++;
                retryCount = CommandParsers.toLong(command[idx]);
                idx++;
            } else if (Strings.isEquals(next, "FORCE")) {
                idx++;
                force = true;
            } else if (Strings.isEquals(next, "JUSTID")) {
                idx++;
                justId = true;
            } else if (Strings.isEquals(next, "LASTID")) {
                idx++;
                lastId = CommandParsers.toBytes(command[idx]);
                idx++;
            } else {
                throw new UnsupportedOperationException(next);
            }
        }
        return new XClaimCommand(key, group, consumer, minIdle, ids.toArray(new byte[0][]), idle, time, retryCount, force, justId, lastId);
    }

    private boolean validId(byte[] bid) {
        if (bid == null) {
            return false;
        }
        String id = CommandParsers.toRune(bid);
        if (Objects.equals(id, "+") || Objects.equals(id, "-")) {
            return true;
        }
        int idx = id.indexOf('-');
        try {
            Long.parseLong(id.substring(0, idx)); // ms
            Long.parseLong(id.substring(idx + 1, id.length())); // seq
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
