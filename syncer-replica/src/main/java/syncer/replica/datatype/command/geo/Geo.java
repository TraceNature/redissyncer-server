package syncer.replica.datatype.command.geo;

import java.io.Serializable;

public class Geo implements Serializable {

    private static final long serialVersionUID = 1L;

    private byte[] member;
    private double longitude;
    private double latitude;

    public Geo() {
    }

    public Geo(byte[] member, double longitude, double latitude) {
        this.member = member;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public byte[] getMember() {
        return member;
    }

    public void setMember(byte[] member) {
        this.member = member;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
