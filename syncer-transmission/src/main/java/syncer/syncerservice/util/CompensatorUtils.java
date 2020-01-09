package syncer.syncerservice.util;

import syncer.syncerservice.util.common.Strings;

/**
 * 补偿机制相关工具
 */
public class CompensatorUtils {

    /**
     * 判断String返回值是否执行成功
     * @param res
     * @return
     */
    public boolean isStringSuccess(String res){
        if(!"OK".equalsIgnoreCase(res)||res.indexOf("error")>=0){
            return false;
        }
        if("OK".equalsIgnoreCase(res)){
            return true;
        }
        return false;
    }


    /**
     * 判断Long返回值是否执行成功
     * @param res
     * @return
     */
    public boolean isLongSuccess(Long res){
        if(res<0){
            return false;
        }
        return true;
    }

    /**
     * 组合判断
     * @param res
     * @return
     */
    public boolean isObjectSuccess(Object res){
        if(res instanceof String){
            return isStringSuccess((String) res);
        }else if(res instanceof Long){
            return isLongSuccess((Long) res);
        }else if(res instanceof Integer){
            return isLongSuccess((Long) res);
        }else if(res instanceof byte[]){
            return isByteSuccess((byte[]) res);
        }
        System.out.println(res.getClass());
        return false;
    }

    private boolean isByteSuccess(byte[] res) {
        String data=Strings.byteToString(res);
        if(!"OK".equalsIgnoreCase(data)||data.indexOf("error")>=0){
            return false;
        }

        try {
            int  a= Integer.parseInt(data);
            if(a>=0){
                return true;
            }
        }catch (Exception e){

        }

        try {
            long  a= Long.valueOf(data);
            if(a>=0L){
                return true;
            }
        }catch (Exception e){

        }

        if("OK".equalsIgnoreCase(data)){
            return true;
        }
        return false;
    }


    public String getRes(Object res){
        if(res instanceof String){
            return String.valueOf(res);
        }else if(res instanceof Long){
            return String.valueOf(res);
        }else if(res instanceof Integer){
            return String.valueOf(res);
        }else if(res instanceof  byte[]){
            return Strings.byteToString((byte[]) res);
        }
        return "";
    }

    public static void main(String[] args) {
        CompensatorUtils compensatorUtils=new CompensatorUtils();
        System.out.println(compensatorUtils.isObjectSuccess(1L));
    }




}
