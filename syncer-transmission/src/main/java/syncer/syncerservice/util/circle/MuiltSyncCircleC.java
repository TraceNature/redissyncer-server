package syncer.syncerservice.util.circle;

import org.springframework.util.StringUtils;
import syncer.syncerjedis.Protocol;
import syncer.syncerpluscommon.util.md5.MD5Utils;
import syncer.syncerplusredis.cmd.impl.DefaultCommand;
import syncer.syncerservice.util.common.Strings;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author zhanenqiang
 * @Description 新版辅助key生成
 * @Date 2020/9/23
 */
public class MuiltSyncCircleC {

    final static String CIRCLE_HEAD="syncer$$#circle-";

    private MultiSyncCommands multiSyncCommandsNode;

    public MuiltSyncCircleC(MultiSyncCommands multiSyncCommandsNode) {
        this.multiSyncCommandsNode = multiSyncCommandsNode;
    }


    /**
     * 判断是否是辅助key
     *
     * @param nodeId
     * @return
     */


    public StringBuilder circleKey(String nodeId){
        return  new StringBuilder(CIRCLE_HEAD).append(nodeId).append("-");
    }

    public boolean hasKey(String[]data,String key){
        List<String> dataList = Arrays.asList(data).stream().filter(s -> !StringUtils.isEmpty(s)).collect(Collectors.toList());
        if (dataList!=null&&dataList.size()>0&&dataList.get(0).startsWith(key)) {
            return true;
        }
        return false;
    }


    /**
     * 判断是否为辅助key
     * @param command
     * @param nodeId
     * @return
     */
    public boolean isCircleKey(DefaultCommand command, String nodeId){
        Predicate<DefaultCommand> commandPredicate=new Predicate<DefaultCommand>() {
            @Override
            public boolean test(DefaultCommand command) {
                String key = circleKey(nodeId).toString();
                Objects.requireNonNull(command.getCommand());
                String scomand=Strings.byteToString(command.getCommand());
                //判断psetex命令
                if (Protocol.Command.PSETEX.toString().equalsIgnoreCase(scomand)){
                    String[] data = Strings.byteToString(command.getArgs());
                    if(hasKey(data,key)){
                        return true;
                    }
                }else if(Protocol.Command.DEL.toString().equalsIgnoreCase(scomand)){
                    String[] data = Strings.byteToString(command.getArgs());
                    if(hasKey(data,key)){
                        return true;
                    }
                }

                return false;
            }
        };

        return commandPredicate.test(command);
    }


    /**
     * 根据command和nodeId生成md5
     *   syncer$$#circle-command-md5
     * @param command
     * @param nodeId
     * @return
     */
    public String getMd5(DefaultCommand command, String nodeId) {
        StringBuilder stringBuilder =circleKey(nodeId);
        stringBuilder.append(Strings.byteToString(command.getCommand())).append("-");
        stringBuilder.append(MD5Utils.getMD5(getStringCommand(command)));
        return stringBuilder.toString();
    }

    String getStringCommand(DefaultCommand command) {
        StringBuilder stringBuilder = new StringBuilder();
        if (command.getCommand() != null) {
            stringBuilder.append(Strings.byteToString(command.getCommand()).toUpperCase());
        }


        if (command.getArgs() != null && command.getArgs().length > 0) {
            String[] data = Strings.byteToString(command.getArgs());
            for (String str : data
            ) {
                if (StringUtils.isEmpty(str)) {
                    continue;
                }
                stringBuilder.append(str);
            }
        }
        return replace(stringBuilder.toString());
    }


    String replace(String str) {
        String destination = "";
        if (str != null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            destination = m.replaceAll("");
        }
        return destination;
    }
}
