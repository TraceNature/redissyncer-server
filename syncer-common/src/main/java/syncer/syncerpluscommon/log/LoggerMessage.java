package syncer.syncerpluscommon.log;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author zhanenqiang
 * @Description 在线日志实时打印
 * @Date 2020/5/19
 */
@Getter@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoggerMessage {
    private String body;
    private String timestamp;
    private String threadName;
    private String className;
    private String level;

    private String exception;
    private String cause;
}
