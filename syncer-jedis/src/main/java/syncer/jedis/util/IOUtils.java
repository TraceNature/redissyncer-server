package syncer.jedis.util;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.max;

public class IOUtils {
  private IOUtils() {
  }

  public static void closeQuietly(Socket sock) {
    // It's same thing as Apache Commons - IOUtils.closeQuietly()
    if (sock != null) {
      try {
        sock.close();
      } catch (IOException e) {
        // ignored
      }
    }
  }

  public static long sub(long v1, long v2) {
    return max(max(v1, 0) - max(v2, 0), 0);
  }

  public static long terminate(ExecutorService exec, long timeout, TimeUnit unit)
          throws InterruptedException {
    if (exec == null) return timeout;
    if (!exec.isShutdown()) exec.shutdown();
    if (timeout <= 0) return 0;
    final long now = System.nanoTime();
    exec.awaitTermination(timeout, unit);
    final long elapsedTime = System.nanoTime() - now;
    return sub(timeout, unit.convert(elapsedTime, TimeUnit.NANOSECONDS));
  }

  public static long terminateQuietly(ExecutorService exec, long timeout, TimeUnit unit) {
    final long now = System.nanoTime();
    try {
      return terminate(exec, timeout, unit);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      final long elapsedTime = System.nanoTime() - now;
      return sub(timeout, unit.convert(elapsedTime, TimeUnit.NANOSECONDS));
    }
  }
}
