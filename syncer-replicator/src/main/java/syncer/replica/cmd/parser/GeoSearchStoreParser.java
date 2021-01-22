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
import syncer.replica.cmd.impl.ByRadius;
import syncer.replica.cmd.impl.GeoSearchStoreCommand;
import syncer.replica.cmd.impl.OrderType;
import syncer.replica.cmd.impl.geo.*;

import static syncer.replica.cmd.CommandParsers.toRune;
import static syncer.replica.util.objectutil.Strings.isEquals;

/**
 * @author Leon Chen
 * @since 3.5.0
 */
public class GeoSearchStoreParser implements CommandParser<GeoSearchStoreCommand> {
    
    @Override
    public GeoSearchStoreCommand parse(Object[] command) {
        int idx = 1;
        byte[] destination = CommandParsers.toBytes(command[idx++]);
        byte[] source = CommandParsers.toBytes(command[idx++]);
        boolean withCoord = false;
        boolean withDist = false;
        boolean withHash = false;
        boolean storeDist = false;
        FromMember fromMember = null;
        FromLonLat fromLonLat = null;
        ByRadius byRadius = null;
        ByBox byBox = null;
        OrderType orderType = OrderType.NONE;
        Count count = null;
        for (int i = idx; i < command.length; i++) {
            String token = toRune(command[i]);
            if (isEquals(token, "FROMMEMBER")) {
                i++;
                fromMember = new FromMember(CommandParsers.toBytes(command[i]));
            } else if (isEquals(token, "FROMLONLAT")) {
                i++;
                double longitude = CommandParsers.toDouble(command[i]);
                i++;
                double latitude = CommandParsers.toDouble(command[i]);
                fromLonLat = new FromLonLat(longitude, latitude);
            } else if (isEquals(token, "BYRADIUS")) {
                i++;
                double radius = CommandParsers.toDouble(command[i]);
                i++;
                UnitType unit = parseUnit(toRune(command[i]));
                byRadius = new ByRadius(radius, unit);
            } else if (isEquals(token, "BYBOX")) {
                i++;
                double width = CommandParsers.toDouble(command[i]);
                i++;
                double height = CommandParsers.toDouble(command[i]);
                i++;
                UnitType unit = parseUnit(toRune(command[i]));
                byBox = new ByBox(width, height, unit);
            } else if (isEquals(token, "ASC")) {
                orderType = OrderType.ASC;
            } else if (isEquals(token, "DESC")) {
                orderType = OrderType.DESC;
            } else if (isEquals(token, "COUNT")) {
                i++;
                count = new Count(CommandParsers.toInt(command[i]));
            } else if (isEquals(token, "WITHCOORD")) {
                withCoord = true;
            } else if (isEquals(token, "WITHDIST")) {
                withDist = true;
            } else if (isEquals(token, "WITHHASH")) {
                withHash = true;
            } else if (isEquals(token, "STOREDIST")) {
                storeDist = true;
            } else {
                throw new AssertionError("parse [GEOSEARCHSTORE] command error." + token);
            }
        }
        return new GeoSearchStoreCommand(destination, source, fromMember, fromLonLat, byRadius, byBox, count, orderType, withCoord, withDist, withHash, storeDist);
    }
    
    private UnitType parseUnit(String token) {
        if (isEquals(token, "M")) {
            return UnitType.M;
        } else if (isEquals(token, "KM")) {
            return UnitType.KM;
        } else if (isEquals(token, "FT")) {
            return UnitType.FT;
        } else if (isEquals(token, "MI")) {
            return UnitType.MI;
        } else {
            throw new AssertionError("parse [GEOSEARCHSTORE] command error." + token);
        }
    }
}
