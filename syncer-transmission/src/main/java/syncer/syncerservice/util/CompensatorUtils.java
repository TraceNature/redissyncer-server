package syncer.syncerservice.util;

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
        return true;
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
        }
        return false;
    }

    public static void main(String[] args) {
        CompensatorUtils compensatorUtils=new CompensatorUtils();
        System.out.println(compensatorUtils.isObjectSuccess(1L));
    }

}
