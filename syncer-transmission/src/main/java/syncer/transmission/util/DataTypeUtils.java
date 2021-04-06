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

package syncer.transmission.util;


import syncer.replica.util.type.KvDataType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/7/22
 */
public class DataTypeUtils {
    static Map<KvDataType,Integer> dataTypeIntegerMap=new HashMap<>();

    static {
        init();
    }



    static void  init(){
        dataTypeIntegerMap.put(KvDataType.SET,1);
        dataTypeIntegerMap.put(KvDataType.LIST,2);
        dataTypeIntegerMap.put(KvDataType.HASH,3);
        dataTypeIntegerMap.put(KvDataType.ZSET,4);
        dataTypeIntegerMap.put(KvDataType.MODULE,5);
        dataTypeIntegerMap.put(KvDataType.STREAM,6);
        dataTypeIntegerMap.put(KvDataType.FRAGMENTATION,7);
        dataTypeIntegerMap.put(KvDataType.FRAGMENTATION_NUM,8);
        dataTypeIntegerMap.put(KvDataType.KEY_DISCARDED_BY_DBMAPPER_RULE,9);
        dataTypeIntegerMap.put(KvDataType.ABANDONED,10);


    }

    public static int getType(KvDataType dataType){
        if(dataTypeIntegerMap.containsKey(dataType)){
            return dataTypeIntegerMap.get(dataType);
        }
        return 12;
    }
}
