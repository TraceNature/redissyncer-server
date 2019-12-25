package syncer.syncerjedis;

public enum BitOP {
  AND, OR, XOR, NOT;

  public final byte[] raw;

  private BitOP() {
    this.raw = syncer.syncerjedis.util.SafeEncoder.encode(name());
  }
}
