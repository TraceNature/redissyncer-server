package syncer.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/12/15
 */
public class RegexUtil {
    /**
     * 正则表达式匹配两个指定字符串中间的内容
     * @param soap
     * @return
     */
    public static List<String> getSubUtil(String soap, String rgex){
        List<String> list = new ArrayList<String>();
        // 匹配的模式
        Pattern pattern = Pattern.compile(rgex);
        Matcher m = pattern.matcher(soap);
        while (m.find()) {
            int i = 1;
            list.add(m.group(i));
            i++;
        }
        return list;
    }



    public static List<List<String>> getSubListUtil(String soap, String rgex,int num){
        List<List<String>> list = new ArrayList<List<String>>();
        // 匹配的模式
        Pattern pattern = Pattern.compile(rgex);
        Matcher m = pattern.matcher(soap);
        while (m.find()) {
            List<String>minList=new ArrayList<>();
            for(int i=1;i<=num;i++){
                minList.add(m.group(i));
            }
            list.add(minList);
        }
        return list;
    }

    /**
     * 返回单个字符串，若匹配到多个的话就返回第一个，方法与getSubUtil一样
     * @param soap
     * @param rgex
     * @return
     */
    public static String getSubUtilSimple(String soap,String rgex){
        // 匹配的模式
        Pattern pattern = Pattern.compile(rgex);
        Matcher m = pattern.matcher(soap);
        while(m.find()){
            return m.group(1);
        }
        return "";
    }


    public static List<String> getSubUtilSimpleList(String soap,String rgex,int n){
        // 匹配的模式
        Pattern pattern = Pattern.compile(rgex);
        Matcher m = pattern.matcher(soap);
        List<String>regList=new ArrayList<>();
        while(m.find()){
            for(int i=1;i<=n;i++){
                regList.add(m.group(i));
            }
        }
        return regList;
    }

    public static List<String> getSubUtilSimpleList(String soap,String rgex){
        // 匹配的模式
        Pattern pattern = Pattern.compile(rgex);
        Matcher m = pattern.matcher(soap);
        List<String>regList=new ArrayList<>();
        while(m.find()){
            regList.add(m.group());
        }
        return regList;
    }
}
