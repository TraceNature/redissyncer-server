package syncer.jedis;

import syncer.jedis.util.SafeEncoder;

public enum ListPosition {
  BEFORE, AFTER;
  public final byte[] raw;

  private ListPosition() {
    raw = SafeEncoder.encode(name());
  }
}
