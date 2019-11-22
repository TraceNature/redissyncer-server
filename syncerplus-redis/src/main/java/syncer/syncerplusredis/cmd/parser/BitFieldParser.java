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
import syncer.syncerplusredis.cmd.CommandParsers;
import syncer.syncerplusredis.util.objectutil.Strings;
import syncer.syncerplusredis.cmd.impl.*;

import java.util.ArrayList;
import java.util.List;

import static syncer.syncerplusredis.util.objectutil.Strings.isEquals;

/**
 * @author Leon Chen
 * @since 2.1.0
 */
public class BitFieldParser implements CommandParser<BitFieldCommand> {

    @Override
    public BitFieldCommand parse(Object[] command) {
        int idx = 1;
        byte[] key = CommandParsers.toBytes(command[idx]);
        idx++;
        List<Statement> list = new ArrayList<>();
        if (idx < command.length) {
            String token;
            do {
                idx = parseStatement(idx, command, list);
                if (idx >= command.length) break;
                token = CommandParsers.toRune(command[idx]);
            }
            while (token != null && (Strings.isEquals(token, "GET") || Strings.isEquals(token, "SET") || Strings.isEquals(token, "INCRBY")));
        }
        List<OverFlow> overflows = null;
        if (idx < command.length) {
            overflows = new ArrayList<>();
            do {
                OverFlow overFlow = new OverFlow();
                idx = parseOverFlow(idx, command, overFlow);
                overflows.add(overFlow);
                if (idx >= command.length) break;
            } while (Strings.isEquals(CommandParsers.toRune(command[idx]), "OVERFLOW"));
        }

        return new BitFieldCommand(key, list, overflows);
    }

    private int parseOverFlow(int i, Object[] params, OverFlow overFlow) {
        int idx = i;
        accept(CommandParsers.toRune(params[idx++]), "OVERFLOW");
        OverFlowType overflow;
        String keyword = CommandParsers.toRune(params[idx++]);
        if (Strings.isEquals(keyword, "WRAP")) {
            overflow = OverFlowType.WRAP;
        } else if (Strings.isEquals(keyword, "SAT")) {
            overflow = OverFlowType.SAT;
        } else if (Strings.isEquals(keyword, "FAIL")) {
            overflow = OverFlowType.FAIL;
        } else {
            throw new AssertionError("parse [BITFIELD] command error." + keyword);
        }
        List<Statement> list = new ArrayList<>();
        if (idx < params.length) {
            String token;
            do {
                idx = parseStatement(idx, params, list);
                if (idx >= params.length) break;
                token = CommandParsers.toRune(params[idx]);
            }
            while (token != null && (Strings.isEquals(token, "GET") || Strings.isEquals(token, "SET") || Strings.isEquals(token, "INCRBY")));
        }
        overFlow.setOverFlowType(overflow);
        overFlow.setStatements(list);
        return idx;
    }

    private int parseStatement(int i, Object[] params, List<Statement> list) {
        int idx = i;
        String keyword = CommandParsers.toRune(params[idx++]);
        Statement statement;
        if (Strings.isEquals(keyword, "GET")) {
            GetTypeOffset getTypeOffset = new GetTypeOffset();
            idx = parseGet(idx - 1, params, getTypeOffset);
            statement = getTypeOffset;
        } else if (Strings.isEquals(keyword, "SET")) {
            SetTypeOffsetValue setTypeOffsetValue = new SetTypeOffsetValue();
            idx = parseSet(idx - 1, params, setTypeOffsetValue);
            statement = setTypeOffsetValue;
        } else if (Strings.isEquals(keyword, "INCRBY")) {
            IncrByTypeOffsetIncrement incrByTypeOffsetIncrement = new IncrByTypeOffsetIncrement();
            idx = parseIncrBy(idx - 1, params, incrByTypeOffsetIncrement);
            statement = incrByTypeOffsetIncrement;
        } else {
            return i;
        }
        list.add(statement);
        return idx;
    }

    private int parseIncrBy(int i, Object[] params, IncrByTypeOffsetIncrement incrByTypeOffsetIncrement) {
        int idx = i;
        accept(CommandParsers.toRune(params[idx++]), "INCRBY");
        byte[] type = CommandParsers.toBytes(params[idx]);
        idx++;
        byte[] offset = CommandParsers.toBytes(params[idx]);
        idx++;
        long increment = CommandParsers.toLong(params[idx++]);
        incrByTypeOffsetIncrement.setType(type);
        incrByTypeOffsetIncrement.setOffset(offset);
        incrByTypeOffsetIncrement.setIncrement(increment);
        return idx;
    }

    private int parseSet(int i, Object[] params, SetTypeOffsetValue setTypeOffsetValue) {
        int idx = i;
        accept(CommandParsers.toRune(params[idx++]), "SET");
        byte[] type = CommandParsers.toBytes(params[idx]);
        idx++;
        byte[] offset = CommandParsers.toBytes(params[idx]);
        idx++;
        long value = CommandParsers.toLong(params[idx++]);
        setTypeOffsetValue.setType(type);
        setTypeOffsetValue.setOffset(offset);
        setTypeOffsetValue.setValue(value);
        return idx;
    }

    private int parseGet(int i, Object[] params, GetTypeOffset getTypeOffset) {
        int idx = i;
        accept(CommandParsers.toRune(params[idx++]), "GET");
        byte[] type = CommandParsers.toBytes(params[idx]);
        idx++;
        byte[] offset = CommandParsers.toBytes(params[idx]);
        idx++;
        getTypeOffset.setType(type);
        getTypeOffset.setOffset(offset);
        return idx;
    }

    private void accept(String actual, String expect) {
        if (Strings.isEquals(actual, expect)) return;
        throw new AssertionError("expect " + expect + " but actual " + actual);
    }

}
