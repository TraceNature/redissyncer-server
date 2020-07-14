package syncer.syncerpluscommon.log;

//import ch.qos.logback.classic.spi.ILoggingEvent;
//
//import ch.qos.logback.core.spi.FilterReply;
//
//import ch.qos.logback.classic.spi.ILoggingEvent;
//import ch.qos.logback.classic.spi.IThrowableProxy;
//import ch.qos.logback.core.filter.Filter;
import java.text.DateFormat;
import java.util.Date;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/5/19   extends Filter<ILoggingEvent>
 */
public class LogFilter  {
//    @Override
//    public FilterReply decide(ILoggingEvent event) {
//
//        String exception = "";
//        IThrowableProxy iThrowableProxy1 = event.getThrowableProxy();
//        if(iThrowableProxy1!=null){
//            exception = "<span class='excehtext'>"+iThrowableProxy1.getClassName()+" "+iThrowableProxy1.getMessage()+"</span></br>";
//            for(int i=0; i<iThrowableProxy1.getStackTraceElementProxyArray().length;i++){
//                exception += "<span class='excetext'>"+iThrowableProxy1.getStackTraceElementProxyArray()[i].toString()+"</span></br>";
//            }
//        }
//
//        LoggerMessage loggerMessage = new LoggerMessage(
//                event.getMessage()
//                , DateFormat.getDateTimeInstance().format(new Date(event.getTimeStamp())),
//                event.getThreadName(),
//                event.getLoggerName(),
//                event.getLevel().levelStr,
//                exception,
//                ""
//        );
//        LoggerQueue.getInstance().push(loggerMessage);
//        return FilterReply.ACCEPT;
//    }
}
