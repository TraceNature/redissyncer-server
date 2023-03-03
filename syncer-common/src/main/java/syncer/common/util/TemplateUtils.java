// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// See the License for the specific language governing permissions and
// limitations under the License.
package syncer.common.util;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.DateConverter;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.*;

/**
 * @author 平行时空
 * @created 2018-04-21 8:53
 **/

public class TemplateUtils {

    private static Properties prop=null;
    private static InputStream in=null;
    private static OutputStream out=null;
    private static String  resourcepath=null;
    public static   String mobileTemplates;
    public static boolean status=true;


    public  static String pcTemplates;

        public final static String[] AGENT = { "Android", "iPhone", "iPod", "iPad", "Windows Phone", "MQQBrowser" };
        public static boolean checkAgentIsMobile(String ua) {
             String[] agent = TemplateUtils.AGENT;
            boolean flag = false;
             if (!ua.contains("Windows NT") || (ua.contains("Windows NT") && ua.contains("compatible; MSIE 9.0;"))) {
                       // 排除 苹果桌面系统
                     if (!ua.contains("Windows NT") && !ua.contains("Macintosh")) {
                             for (String item : agent) {
                                   if (ua.contains(item)) {
                                             flag = true;
                                              break;
                                   }
                             }
                     }
             }
               return flag;
            }


        public static boolean isMoblie(HttpServletRequest request){
            String ua=request.getHeader("User-Agent");
            boolean flag= TemplateUtils.checkAgentIsMobile(ua);
            return flag;
        }



    /**
     * 判断是否是微信访问
     *
     * @param request
     * @return
     */
    public static boolean isWeChat(HttpServletRequest request) {
        String userAgent = request.getHeader("user-agent").toLowerCase();
        return userAgent == null || userAgent.indexOf("micromessenger") == -1 ? false : true;
    }


    public static void setPropertiesPath(String resourcepath1){
         resourcepath=resourcepath1;
    }
    public static String getPropertiesdata(String name) throws IOException{
            return TemplateUtils.getPropertiesdata(resourcepath,name);
    }

    /**
     * 获取配置文件信息
     * @param resourcepath
     * @param name
     * @return
     * @throws IOException
     */
    public synchronized static String getPropertiesdata(String resourcepath, String name) {
        String value=null;
        try {
            prop = new Properties();

            in = TemplateUtils.class.getClassLoader().getResourceAsStream(
                    resourcepath);
            prop.load(new InputStreamReader(in, "utf-8"));
            value= prop.getProperty(name);
        } catch (IOException e) {
            try {
                in.close();
            } catch (IOException e1) {

            }
        }finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return value;
    }


    /**
     * 修改或添加键值对 如果key存在，修改, 反之，添加。
     * @param filePath 文件路径，即文件所在包的路径，例如：java/util/config.properties
     * @param key 键
     * @param value 键对应的值
     */
    public synchronized static void writeData(String filePath, String key, String value) {

        try {
            prop = new Properties();
            in = TemplateUtils.class.getClassLoader().getResourceAsStream(
                    filePath);
            prop.load(new InputStreamReader(in, "utf-8"));
            prop.setProperty(key,value);
            String path=null;
            String pat=Thread.currentThread().getContextClassLoader().getResource("").getPath();
            if(Objects.nonNull(pat)){
                path=pat+filePath;
            }else {
                path=filePath;
            }
            FileOutputStream file = new FileOutputStream(path);
            //将Properties中的属性列表（键和元素对）写入输出流
          //  prop.store(new OutputStreamWriter( Thread.currentThread().getContextClassLoader().getResource("").getPath()+filePath);
            prop.store(file, "");
        } catch (IOException e) {
            try {
                in.close();
            } catch (IOException e1) {

            }
        }finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }



    public  static String getUrl(HttpServletRequest request)throws Exception {
            Properties prop = new Properties();
            InputStream in = TemplateUtils.class.getClassLoader().getResourceAsStream(
                    "other.properties");
            prop.load(new InputStreamReader(in, "utf-8"));

             mobileTemplates=prop.getProperty("templates.mobile");
             pcTemplates=prop.getProperty("templates.pc");
             in.close();
            String url="";
            boolean ismoblie= TemplateUtils.isMoblie(request);
            if(ismoblie){
                url= mobileTemplates+"/";
            }else{
                url=pcTemplates+"/";
            }
            return url;
        }


    /**
     * 获取uuid
     * @return
     */
    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    public static <T> T toBean(Map map, Class<T> clazz) {
        try {
            T bean = clazz.newInstance();
            ConvertUtils.register(new DateConverter(), Date.class);
            BeanUtils.populate(bean, map);
            return bean;
        } catch (Exception var3) {
            throw new RuntimeException(var3);
        }
    }
}
