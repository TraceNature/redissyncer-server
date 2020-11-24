package syncer.syncerservice.util.compare;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.FileCopyUtils;
import syncer.syncerpluscommon.service.SqlFileExecutor;
import syncer.syncerpluscommon.util.common.TemplateUtils;
import syncer.syncerpluscommon.util.db.SqliteUtil;
import syncer.syncerpluscommon.util.file.FileUtils;
import syncer.syncerservice.util.common.OsUtils;

import java.io.*;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/9/17
 */
@Slf4j
public class WinRedisCompare {
    //先创建一个新的空线程
    private static Process process=null;
    public static void main(String[] args) {
        try {
            getValue();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(true){
            return;
        }
        Runtime rt = Runtime.getRuntime();//java的API，获得一个对象

        try {
            int type=1;
            if("windows".equalsIgnoreCase(OsUtils.getOsSystem())){
                type=2;
            }

            String basePath=SqliteUtil.getSysPath();
            StringBuilder path=new StringBuilder(basePath).append("\\compare\\").append(TemplateUtils.uuid());
            String comparePath=path.toString();
            String exePath=path.append("\\rediscompare.exe").toString();

//            String
            if(!FileUtils.existsFile(comparePath)){
                FileUtils.mkdirs(comparePath);
            }

            try {

//                InputStream input = SqlFileExecutor.class.getClassLoader().getResourceAsStream(
//                        "compare/rediscompare.exe");
                InputStream input = new FileInputStream(
                        "D:\\github\\redissyncer-service-new\\syncer-webapp\\src\\main\\resources\\compare\\rediscompare.exe");
                FileOutputStream output = new FileOutputStream(exePath);
                FileCopyUtils.copy(input, output);
                log.info("校验脚本准备完毕");
            }catch (Exception e){
                log.info("准备校验脚本失败");
                throw e;
            }

            /**
            try {
                String cmd=new StringBuilder(exePath).append(" compare single2single  --saddr ")
                        .append("114.67.100.239:6379 ").append("--spassword ")
                        .append("redistest0102 ").append("--taddr ").append("114.67.100.240:6379 ")
                        .append("--tpassword  ").append("redistest0102 ").append(" --comparetimes 3").toString();



                System.out.println(cmd);

                Process p = rt.exec(cmd);// 启动另一个进程来执行命令
                BufferedInputStream in = new BufferedInputStream(p.getInputStream());
                BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
                String lineStr;
                while ((lineStr = inBr.readLine()) != null){
                    System.out.println(lineStr);// 打印输出信息

                }
                    //获得命令执行后在控制台的输出信息
                //检查命令是否执行失败。
                if (p.waitFor() != 0) {
                    if (p.exitValue() == 1)//p.exitValue()==0表示正常结束，1：非正常结束
                    {
                        System.err.println("命令执行失败!");
                    }
                }
                inBr.close();
                in.close();
            }catch (Exception e){
                e.printStackTrace();
            }

             */
            BufferedReader br=null;
            BufferedReader brError=null;

            try {
                //String cmd=new StringBuilder("").append(exePath).append(" compare single2single  --saddr ")

                String cmd=new StringBuilder("D:\\github\\redissyncer-service-new\\syncer-webapp\\src\\main\\resources\\compare\\rediscompare.exe")
                        .append(" ").append(" compare single2single  --saddr ")
                        .append("114.67.100.239:6379 ").append("--spassword ")
                        .append("redistest0102 ").append("--taddr ").append("114.67.100.240:6379 ")
                        .append("--tpassword  ").append("redistest0102 ").append(" --comparetimes 3").toString();
                System.out.println(cmd);
                //执行exe  cmd可以为字符串(exe存放路径)也可为数组，调用exe时需要传入参数时，可以传数组调用(参数有顺序要求)
                Process p = Runtime.getRuntime().exec(cmd);
                String line = null;
                br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                brError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                while ((line = br.readLine()) != null  || (line = brError.readLine()) != null) {
                    //输出exe输出的信息以及错误信息
                    System.out.println(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (brError!=null){
                    brError.close();
                }
            }



//            System.out.println("用户的当前工作目录:/n"+System.getProperty("user.dir"));
//            rt.exec("D:\\我的文档\\下载\\BeyondCompare\\BeyondCompare\\BCompare.exe");//找到这个路径，直接调用即可

        }catch (Exception e){
            e.printStackTrace();
        }

    }



    public static String  getValue() throws IOException{
        Runtime mt =Runtime.getRuntime();

        File  myfile =new File("D:\\github\\redissyncer-service-new\\syncer-webapp\\src\\main\\resources\\compare","rediscompare.exe");
        String   path =myfile.getAbsolutePath();
        String[] cmd = {path,"compare single2single  --saddr 114.67.100.239:6379 --spassword redistest0102 --taddr 114.67.100.240:6379 --tpassword  redistest0102  --comparetimes 3 1>compare.log 2>compare_err.log"};

        try {
            //启动线程
            process = Runtime.getRuntime().exec(cmd);
            System.out.println("创建exe进程成功\n");
        }
        catch(IOException e) {
            System.err.println("创建进程时出错\n" + e);
            System.exit(1);
        }


        //调用ConsleTestArea 里面的方法
        InputStream in=process.getInputStream();

        //<1>创建字节数组输出流，用来输出读取到的内容
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //<2>创建缓存大小
        byte[] buffer = new byte[1024]; // 1KB
        //每次读取到内容的长度
        int len = -1;
        //<3>开始读取输入流中的内容
        while ((len = in.read(buffer)) != -1) { //当等于-1说明没有数据可以读取了
            baos.write(buffer, 0, len);   //把读取到的内容写到输出流中
            //<4> 把字节数组转换为字符串
            String content = baos.toString();

            System.out.println("结果："+content);
        }
        //<4> 把字节数组转换为字符串
        String content = baos.toString();

        System.out.println("结果："+content);



        in.close();
        baos.close();

        return content;
    }

} // AppOutputCapture
