package syncer.jedis;

import syncer.jedis.util.SafeEncoder;

public enum GeoUnit {
  M, KM, MI, FT;

  public final byte[] raw;

  GeoUnit() {
    raw = SafeEncoder.encode(this.name().toLowerCase());
  }
}
