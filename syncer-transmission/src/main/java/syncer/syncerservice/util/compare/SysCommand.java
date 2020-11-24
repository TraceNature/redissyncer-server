package syncer.syncerservice.util.compare;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/9/21
 */
public class SysCommand {
    public static void exeCmd(String commandStr) {
        BufferedReader br = null;
        try {
            Process p = Runtime.getRuntime().exec(commandStr);
            br = new BufferedReader(new InputStreamReader(p.getInputStream(),"GBK"));
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            System.out.println(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally
        {
            if (br != null)
            {
                try {
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public static void main(String[] args) {
        String commandStr = "D:\\github\\redissyncer-service-new\\compare\\7746ED3A024449BB830F7C91A23FBC96\\rediscompare.exe compare single2single  --saddr 114.67.100.239:6379 --spassword redistest0102 --taddr 114.67.100.240:6379 --tpassword  redistest0102  --comparetimes 3";
        String commandStr1 = "ipconfig";
        SysCommand.exeCmd(commandStr1);
    }
}
