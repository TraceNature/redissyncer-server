package syncer.replica.exception;

/**
 * Redis登陆失败异常
 * @author: Eq Zhan
 * @create: 2021-01-21
 **/
public class RedisAuthErrorException extends Exception{
    public RedisAuthErrorException(String message) {
        super(message);
    }
}

