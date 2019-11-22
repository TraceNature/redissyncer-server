package syncer.syncerplusservice.util.Regex;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 平行时空
 * @created 2018-12-30 0:14
 **/
public class RegexUtil {
    public static void main(String[] args) {
        String str = "<?xml version='1.0' encoding='UTF-8'?><ufinterface billtype='gl' filename='e:\1.xml' isexchange='Y' proc='add' receiver='1060337@1060337-003' replace='Y' roottag='sendresult' sender='01' successful='Y'><sendresult><billpk></billpk><bdocid>w764</bdocid><filename>e:\1.xml</filename><resultcode>1</resultcode><resultdescription>单据w764开始处理...单据w764处理完毕!</resultdescription><content>2017.09-记账凭证-1</content></sendresult><sendresult><billpk></billpk><bdocid>w1007</bdocid><filename>e:\1.xml</filename><resultcode>1</resultcode><resultdescription>单据w1007开始处理...单据w1007处理完毕!</resultdescription><content>2017.10-记账凭证-1</content></sendresult><sendresult><billpk></billpk><bdocid>w516</bdocid><filename>e:\1.xml</filename><resultcode>1</resultcode><resultdescription>单据w516开始处理...单据w516处理完毕!</resultdescription><content>2017.07-记账凭证-50</content></sendresult></ufinterface>";
        //String str = "abc3443abcfgjhgabcgfjabc";
        String rgex = "redis_version:(.*?)\n";

        System.out.println((new RegexUtil()).getSubUtil(str,rgex));
        List<String> lists = (new RegexUtil()).getSubUtil(str,rgex);
        for (String string : lists) {
            System.out.println(string);
        }
        System.out.println((new RegexUtil()).getSubUtilSimple(str, rgex));
    }
    /**
     * 正则表达式匹配两个指定字符串中间的内容
     * @param soap
     * @return
     */
    public static List<String> getSubUtil(String soap, String rgex){
        List<String> list = new ArrayList<String>();
        Pattern pattern = Pattern.compile(rgex);// 匹配的模式
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
        Pattern pattern = Pattern.compile(rgex);// 匹配的模式
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
        Pattern pattern = Pattern.compile(rgex);// 匹配的模式
        Matcher m = pattern.matcher(soap);
        while(m.find()){
            return m.group(1);
        }
        return "";
    }


    public static List<String> getSubUtilSimpleList(String soap,String rgex,int n){
        Pattern pattern = Pattern.compile(rgex);// 匹配的模式

        Matcher m = pattern.matcher(soap);
        List<String>regList=new ArrayList<>();

        while(m.find()){
            for(int i=1;i<=n;i++){
                regList.add(m.group(i));
            }

//            regList.add(m.group(0));
//            return m.group(1);
        }
        return regList;
    }

    public static List<String> getSubUtilSimpleList(String soap,String rgex){
        Pattern pattern = Pattern.compile(rgex);// 匹配的模式

        Matcher m = pattern.matcher(soap);
        List<String>regList=new ArrayList<>();

        while(m.find()){

            regList.add(m.group());
//            regList.add(m.group(0));
//            return m.group(1);
        }
        return regList;
    }
}
