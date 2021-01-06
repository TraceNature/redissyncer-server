package syncer.jedis.commands;

import syncer.jedis.Response;

import java.util.List;

public interface ScriptingCommandsPipeline {
  Response<Object> eval(String script, int keyCount, String... params);

  Response<Object> eval(String script, List<String> keys, List<String> args);

  Response<Object> eval(String script);

  Response<Object> evalsha(String sha1);

  Response<Object> evalsha(String sha1, List<String> keys, List<String> args);

  Response<Object> evalsha(String sha1, int keyCount, String... params);
}
