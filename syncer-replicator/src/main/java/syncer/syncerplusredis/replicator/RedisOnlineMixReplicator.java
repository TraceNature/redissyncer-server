package syncer.syncerplusredis.replicator;

import syncer.syncerpluscommon.util.taskType.SyncerTaskType;
import syncer.syncerplusredis.constant.TaskStatusType;
import syncer.syncerplusredis.entity.Configuration;
import syncer.syncerplusredis.event.PostCommandSyncEvent;
import syncer.syncerplusredis.event.PreCommandSyncEvent;
import syncer.syncerplusredis.io.PeekableInputStream;
import syncer.syncerplusredis.io.RedisInputStream;
import syncer.syncerplusredis.rdb.RdbParser;
import syncer.syncerplusredis.util.MultiSyncTaskManagerutils;
import syncer.syncerplusredis.util.TaskDataManagerUtils;
import syncer.syncerplusredis.util.objectutil.Strings;
import syncer.syncerplusredis.util.type.Tuples;
import lombok.extern.slf4j.Slf4j;
import syncer.syncerplusredis.exception.IncrementException;
import syncer.syncerplusredis.cmd.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;


/**
 * 在线混合文件
 */
@Slf4j
public class RedisOnlineMixReplicator extends AbstractReplicator {
    protected ReplyParser replyParser;
    protected PeekableInputStream peekable;

    public RedisOnlineMixReplicator(File file, Configuration configuration) throws FileNotFoundException {
        this(new FileInputStream(file), configuration);
    }

    public RedisOnlineMixReplicator(InputStream in, Configuration configuration) {
        Objects.requireNonNull(in);
        Objects.requireNonNull(configuration);
        this.configuration = configuration;
        if (in instanceof PeekableInputStream) {
            this.peekable = (PeekableInputStream) in;
        } else {
            in = this.peekable = new PeekableInputStream(in);
        }
        this.inputStream = new RedisInputStream(in, this.configuration.getBufferSize());
        this.inputStream.setRawByteListeners(this.rawByteListeners);
        this.replyParser = new ReplyParser(inputStream, new RedisCodec());
        builtInCommandParserRegister();
        if (configuration.isUseDefaultExceptionListener()) {
            addExceptionListener(new DefaultExceptionListener());
        }
    }

    @Override
    public void open() throws IOException, IncrementException {
        super.open();
        if (!compareAndSet(Status.DISCONNECTED, Status.CONNECTED)) {
            return;
        }
        try {
            doOpen();
        } catch (UncheckedIOException e) {
            if (!(e.getCause() instanceof EOFException)) {
                throw e.getCause();
            }
        } finally {
            doClose();
            doCloseListener(this);
        }
    }

    public RedisOnlineMixReplicator(String fileUrl, Configuration configuration, String taskId) {
        try {

            URL url = new URL(fileUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //设置超时间为3秒
            conn.setConnectTimeout(3 * 1000);
            //防止屏蔽程序抓取而返回403错误
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            //得到输入流
            InputStream in = conn.getInputStream();

            Objects.requireNonNull(in);
            Objects.requireNonNull(configuration);
            this.configuration = configuration;
            if (in instanceof PeekableInputStream) {
                this.peekable = (PeekableInputStream) in;
            } else {
                in = this.peekable = new PeekableInputStream(in);
            }
            this.inputStream = new RedisInputStream(in, this.configuration.getBufferSize());
            this.inputStream.setRawByteListeners(this.rawByteListeners);
            this.replyParser = new ReplyParser(inputStream, new RedisCodec());
            builtInCommandParserRegister();
            if (configuration.isUseDefaultExceptionListener()) {
                addExceptionListener(new DefaultExceptionListener());
            }

        } catch (Exception e) {
            try {
                if(!SyncerTaskType.isMultiTask(taskId)){
                    TaskDataManagerUtils.updateThreadStatusAndMsg(taskId,"文件在线读取异常", TaskStatusType.BROKEN);
                }else {
                    MultiSyncTaskManagerutils.setGlobalNodeStatus(taskId,"文件在线读取异常", TaskStatusType.BROKEN);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】", taskId, "文件下载异常");
        }
    }

    @Override
    public void open(String taskId) throws IOException, IncrementException {

        super.open();
        if (!compareAndSet(Status.DISCONNECTED, Status.CONNECTED)) {
            return;
        }
        try {
            doOpen(taskId);
        } catch (UncheckedIOException e) {
            if (!(e.getCause() instanceof EOFException)){
                throw e.getCause();
            }
        } finally {
            doClose();
            doCloseListener(this);
        }


    }

    protected void doOpen() throws IOException {
        configuration.setReplOffset(0L);
        if (peekable.peek() == 'R') {
            RdbParser parser = new RdbParser(inputStream, this);
            configuration.setReplOffset(parser.parse());
        }
        if (getStatus() != Status.CONNECTED){
            return;
        }
        submitEvent(new PreCommandSyncEvent());
        try {
            final long[] offset = new long[1];
            while (getStatus() == Status.CONNECTED) {
                Object obj = replyParser.parse(len -> offset[0] = len);
                if (obj instanceof Object[]) {
                    if (verbose() && log.isDebugEnabled()) {
                        log.debug(Strings.format((Object[]) obj));
                    }
                    Object[] raw = (Object[]) obj;
                    CommandName name = CommandName.name(Strings.toString(raw[0]));

//                    //jimdb 首次解析
//                    if(name.equals("TRANSMIT")){
//                        CommandName first_parser_name = CommandName.name("TRANSMITFIRSTPARSER");
//                        final JimDbFirstCommandParser firstparser= (JimDbFirstCommandParser) commands.get(first_parser_name);
//                        if(firstparser!=null){
//                            raw=firstparser.parse(raw).getCommand();
//                        }
//                    }

                    final CommandParser<? extends Command> parser;
                    if ((parser = commands.get(name)) == null) {
                        log.warn("command [{}] not register. raw command:{}", name, Strings.format(raw));
                        configuration.addOffset(offset[0]);
                        offset[0] = 0L;
                        continue;
                    }
                    final long st = configuration.getReplOffset();
                    final long ed = st + offset[0];
                    submitEvent(parser.parse(raw), Tuples.of(st, ed));
                } else {
                    log.warn("unexpected redis reply:{}", obj);
                }
                configuration.addOffset(offset[0]);
                offset[0] = 0L;
            }
        } catch (EOFException ignore) {
            submitEvent(new PostCommandSyncEvent());
        }
    }


    protected void doOpen(String taskId) throws IOException {
        try {
            configuration.setReplOffset(0L);
            if (peekable.peek() == 'R') {
                RdbParser parser = new RdbParser(inputStream, this);
                configuration.setReplOffset(parser.parse());
            }
            if (getStatus() != Status.CONNECTED) {
                return;
            }
            submitEvent(new PreCommandSyncEvent());
            try {
                final long[] offset = new long[1];
                while (getStatus() == Status.CONNECTED) {
                    Object obj = replyParser.parse(len -> offset[0] = len);
                    if (obj instanceof Object[]) {
                        if (verbose() && log.isDebugEnabled()) {
                            log.debug(Strings.format((Object[]) obj));
                        }
                        Object[] raw = (Object[]) obj;
                        CommandName name = CommandName.name(Strings.toString(raw[0]));

                        //jimdb 首次解析
//                        if(name.equals("TRANSMIT")){
//                            CommandName first_parser_name = CommandName.name("TRANSMITFIRSTPARSER");
//                            final JimDbFirstCommandParser firstparser= (JimDbFirstCommandParser) commands.get(first_parser_name);
//                            if(firstparser!=null){
//                                raw=firstparser.parse(raw).getCommand();
//                            }
//                        }

                        final CommandParser<? extends Command> parser;
                        if ((parser = commands.get(name)) == null) {
                            log.warn("command [{}] not register. raw command:{}", name, Strings.format(raw));
                            configuration.addOffset(offset[0]);
                            offset[0] = 0L;
                            continue;
                        }
                        final long st = configuration.getReplOffset();
                        final long ed = st + offset[0];
                        submitEvent(parser.parse(raw), Tuples.of(st, ed));
                    } else {
                        log.warn("unexpected redis reply:{}", obj);
                    }
                    configuration.addOffset(offset[0]);
                    offset[0] = 0L;
                }
            } catch (EOFException ignore) {
                submitEvent(new PostCommandSyncEvent());
            }

        } catch (Exception ignore) {
            try {
                if(!SyncerTaskType.isMultiTask(taskId)){
                    TaskDataManagerUtils.updateThreadStatusAndMsg(taskId,ignore.getMessage(), TaskStatusType.BROKEN);
                }else {
                    MultiSyncTaskManagerutils.setGlobalNodeStatus(taskId,ignore.getMessage(), TaskStatusType.BROKEN);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            log.warn("任务Id【{}】异常停止，停止原因【{}】", taskId, ignore.getMessage());
        }
    }


}
