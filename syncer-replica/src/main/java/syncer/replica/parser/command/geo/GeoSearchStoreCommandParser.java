package syncer.replica.parser.command.geo;

import syncer.replica.datatype.command.CommandParser;
import syncer.replica.datatype.command.CommandParsers;
import syncer.replica.datatype.command.geo.GeoSearchStoreCommand;
import syncer.replica.datatype.command.geo.element.*;
import syncer.replica.util.strings.Strings;

/**
 *GEOSEARCHSTORE destination source [FROMMEMBER member] [FROMLONLAT longitude latitude] [BYRADIUS radius m|km|ft|mi] [BYBOX width height m|km|ft|mi] [ASC|DESC] [COUNT count [ANY]] [WITHCOORD] [WITHDIST] [WITHHASH] [STOREDIST]
 * Available since 6.2.
 *
 * Time complexity: O(N+log(M)) where N is the number of elements in the grid-aligned bounding box area around the shape provided as the filter and M is the number of items inside the shape
 *
 * This command is like GEOSEARCH, but stores the result in destination key.
 *
 * This command comes in place of the now deprecated GEORADIUS and GEORADIUSBYMEMBER.
 *
 * By default, it stores the results in the destination sorted set with their geospatial information.
 *
 * When using the STOREDIST option, the command stores the items in a sorted set populated with their distance from the center of the circle or box, as a floating-point number, in the same unit specified for that shape.
 */
public class GeoSearchStoreCommandParser implements CommandParser<GeoSearchStoreCommand> {

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
            String token = CommandParsers.toRune(command[i]);
            if (Strings.isEquals(token, "FROMMEMBER")) {
                i++;
                fromMember = new FromMember(CommandParsers.toBytes(command[i]));
            } else if (Strings.isEquals(token, "FROMLONLAT")) {
                i++;
                double longitude = CommandParsers.toDouble(command[i]);
                i++;
                double latitude = CommandParsers.toDouble(command[i]);
                fromLonLat = new FromLonLat(longitude, latitude);
            } else if (Strings.isEquals(token, "BYRADIUS")) {
                i++;
                double radius = CommandParsers.toDouble(command[i]);
                i++;
                UnitType unit = parseUnit(CommandParsers.toRune(command[i]));
                byRadius = new ByRadius(radius, unit);
            } else if (Strings.isEquals(token, "BYBOX")) {
                i++;
                double width = CommandParsers.toDouble(command[i]);
                i++;
                double height = CommandParsers.toDouble(command[i]);
                i++;
                UnitType unit = parseUnit(CommandParsers.toRune(command[i]));
                byBox = new ByBox(width, height, unit);
            } else if (Strings.isEquals(token, "ASC")) {
                orderType = OrderType.ASC;
            } else if (Strings.isEquals(token, "DESC")) {
                orderType = OrderType.DESC;
            } else if (Strings.isEquals(token, "COUNT")) {
                i++;
                count = new Count(CommandParsers.toInt(command[i]));
            } else if (Strings.isEquals(token, "WITHCOORD")) {
                withCoord = true;
            } else if (Strings.isEquals(token, "WITHDIST")) {
                withDist = true;
            } else if (Strings.isEquals(token, "WITHHASH")) {
                withHash = true;
            } else if (Strings.isEquals(token, "STOREDIST")) {
                storeDist = true;
            } else {
                throw new AssertionError("parse [GEOSEARCHSTORE] command error." + token);
            }
        }
        return new GeoSearchStoreCommand(destination, source, fromMember, fromLonLat, byRadius, byBox, count, orderType, withCoord, withDist, withHash, storeDist);
    }

    private UnitType parseUnit(String token) {
        if (Strings.isEquals(token, "M")) {
            return UnitType.M;
        } else if (Strings.isEquals(token, "KM")) {
            return UnitType.KM;
        } else if (Strings.isEquals(token, "FT")) {
            return UnitType.FT;
        } else if (Strings.isEquals(token, "MI")) {
            return UnitType.MI;
        } else {
            throw new AssertionError("parse [GEOSEARCHSTORE] command error." + token);
        }
    }
}
