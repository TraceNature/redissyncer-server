package syncer.jedis;

public enum BitOP {
  AND, OR, XOR, NOT;

  public final byte[] raw;

  private BitOP() {
    this.raw = syncer.jedis.util.SafeEncoder.encode(name());
  }
}
