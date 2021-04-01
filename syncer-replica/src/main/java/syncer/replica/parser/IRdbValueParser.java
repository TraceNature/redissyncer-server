package syncer.replica.parser;

import syncer.replica.io.RedisInputStream;

import java.io.IOException;

/**
 * @author: Eq Zhan
 * @create: 2021-03-16
 **/
public interface IRdbValueParser {
    <T> T parseString(RedisInputStream in, int version) throws IOException;

    <T> T parseList(RedisInputStream in, int version) throws IOException;

    <T> T parseSet(RedisInputStream in, int version) throws IOException ;

    <T> T parseZSet(RedisInputStream in, int version) throws IOException;

    <T> T parseZSet2(RedisInputStream in, int version) throws IOException;

    <T> T parseHash(RedisInputStream in, int version) throws IOException;

    <T> T parseHashZipMap(RedisInputStream in, int version) throws IOException;

    <T> T parseListZipList(RedisInputStream in, int version) throws IOException;

    <T> T parseSetIntSet(RedisInputStream in, int version) throws IOException;

    <T> T parseZSetZipList(RedisInputStream in, int version) throws IOException;

    <T> T parseHashZipList(RedisInputStream in, int version) throws IOException;

    <T> T parseListQuickList(RedisInputStream in, int version) throws IOException;

    <T> T parseModule(RedisInputStream in, int version) throws IOException;

    <T> T parseModule2(RedisInputStream in, int version) throws IOException;

    <T> T parseStreamListPacks(RedisInputStream in, int version) throws IOException;
}
