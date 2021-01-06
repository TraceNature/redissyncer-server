package syncer.transmission.util;

import syncer.replica.rdb.sync.datatype.DataType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/7/22
 */
public class DataTypeUtils {
    static Map<DataType,Integer> dataTypeIntegerMap=new HashMap<>();

    static {
        init();
    }



    static void  init(){
        dataTypeIntegerMap.put(DataType.SET,1);
        dataTypeIntegerMap.put(DataType.LIST,2);
        dataTypeIntegerMap.put(DataType.HASH,3);
        dataTypeIntegerMap.put(DataType.ZSET,4);
        dataTypeIntegerMap.put(DataType.MODULE,5);
        dataTypeIntegerMap.put(DataType.STREAM,6);
        dataTypeIntegerMap.put(DataType.FRAGMENTATION,7);
        dataTypeIntegerMap.put(DataType.FRAGMENTATION_NUM,8);
        dataTypeIntegerMap.put(DataType.KEY_DISCARDED_BY_DBMAPPER_RULE,9);
        dataTypeIntegerMap.put(DataType.ABANDONED,10);


    }

    public static int getType(DataType dataType){
        if(dataTypeIntegerMap.containsKey(dataType)){
            return dataTypeIntegerMap.get(dataType);
        }
        return 12;
    }
}
