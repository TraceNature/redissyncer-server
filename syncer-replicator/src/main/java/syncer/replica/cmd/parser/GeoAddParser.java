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
import syncer.replica.cmd.impl.Geo;
import syncer.replica.cmd.impl.GeoAddCommand;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Leon Chen
 * @since 2.1.0
 */
public class GeoAddParser implements CommandParser<GeoAddCommand> {
    @Override
    public GeoAddCommand parse(Object[] command) {
        int idx = 1;
        byte[] key = CommandParsers.toBytes(command[idx]);
        idx++;
        List<Geo> list = new ArrayList<>();
        while (idx < command.length) {
            double longitude = CommandParsers.toDouble(command[idx++]);
            double latitude = CommandParsers.toDouble(command[idx++]);
            byte[] member = CommandParsers.toBytes(command[idx]);
            idx++;
            list.add(new Geo(member, longitude, latitude));
        }
        Geo[] geos = new Geo[list.size()];
        list.toArray(geos);
        return new GeoAddCommand(key, geos);
    }

}
