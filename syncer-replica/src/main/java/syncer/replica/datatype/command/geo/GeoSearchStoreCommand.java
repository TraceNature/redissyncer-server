package syncer.replica.datatype.command.geo;

import syncer.replica.datatype.command.AbstractBaseCommand;
import syncer.replica.datatype.command.geo.element.*;

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
public class GeoSearchStoreCommand extends AbstractBaseCommand {

    private static final long serialVersionUID = 1L;

    private byte[] destination;
    private byte[] source;
    private FromMember fromMember;
    private FromLonLat fromLonLat;
    private ByRadius byRadius;
    private ByBox byBox;
    private Count count;
    private OrderType orderType = OrderType.NONE;
    private boolean withCoord;
    private boolean withDist;
    private boolean withHash;
    private boolean storeDist;

    public GeoSearchStoreCommand() {
    }

    public GeoSearchStoreCommand(byte[] destination, byte[] source, FromMember fromMember, FromLonLat fromLonLat,
                                 ByRadius byRadius, ByBox byBox, Count count, OrderType orderType,
                                 boolean withCoord, boolean withDist, boolean withHash, boolean storeDist) {
        this.destination = destination;
        this.source = source;
        this.fromMember = fromMember;
        this.fromLonLat = fromLonLat;
        this.byRadius = byRadius;
        this.byBox = byBox;
        this.count = count;
        this.orderType = orderType;
        this.withCoord = withCoord;
        this.withDist = withDist;
        this.withHash = withHash;
        this.storeDist = storeDist;
    }

    public byte[] getDestination() {
        return destination;
    }

    public void setDestination(byte[] destination) {
        this.destination = destination;
    }

    public byte[] getSource() {
        return source;
    }

    public void setSource(byte[] source) {
        this.source = source;
    }

    public FromMember getFromMember() {
        return fromMember;
    }

    public void setFromMember(FromMember fromMember) {
        this.fromMember = fromMember;
    }

    public FromLonLat getFromLonLat() {
        return fromLonLat;
    }

    public void setFromLonLat(FromLonLat fromLonLat) {
        this.fromLonLat = fromLonLat;
    }

    public ByRadius getByRadius() {
        return byRadius;
    }

    public void setByRadius(ByRadius byRadius) {
        this.byRadius = byRadius;
    }

    public ByBox getByBox() {
        return byBox;
    }

    public void setByBox(ByBox byBox) {
        this.byBox = byBox;
    }

    public Count getCount() {
        return count;
    }

    public void setCount(Count count) {
        this.count = count;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    public boolean isWithCoord() {
        return withCoord;
    }

    public void setWithCoord(boolean withCoord) {
        this.withCoord = withCoord;
    }

    public boolean isWithDist() {
        return withDist;
    }

    public void setWithDist(boolean withDist) {
        this.withDist = withDist;
    }

    public boolean isWithHash() {
        return withHash;
    }

    public void setWithHash(boolean withHash) {
        this.withHash = withHash;
    }

    public boolean isStoreDist() {
        return storeDist;
    }

    public void setStoreDist(boolean storeDist) {
        this.storeDist = storeDist;
    }
}
