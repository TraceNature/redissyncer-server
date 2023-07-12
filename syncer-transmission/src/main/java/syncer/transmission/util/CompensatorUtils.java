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

import lombok.extern.slf4j.Slf4j;
import syncer.replica.util.strings.Strings;
import syncer.transmission.compensator.PipeLineCompensatorEnum;

/**
 * 补偿机制相关工具
 */

@Slf4j
public class CompensatorUtils {

    /**
     * 判断String返回值是否执行成功
     *
     * @param res
     * @return
     */
    public boolean isStringSuccess(String res) {
        if ("PONG".equalsIgnoreCase(res)) {
            return true;
        }
        if ("OK".equalsIgnoreCase(res)) {
            return true;
        }

        if ("null".equalsIgnoreCase(res)) {
            return true;
        }

        if ((!"OK".equalsIgnoreCase(res) && !"PONG".equalsIgnoreCase(res)) || res.indexOf("error") >= 0) {
            return false;
        }

        return true;
    }


    /**
     * 判断Long返回值是否执行成功
     *
     * @param res
     * @return
     */
    public boolean isLongSuccess(Long res) {
//        if(res<0){
//            return false;
//        }
        return true;
    }

    /**
     * 组合判断
     *
     * @param res
     * @return
     */
    public boolean isObjectSuccess(Object res) {
        if (res instanceof Integer[]) {
            return true;
        } else if (res instanceof Long[]) {
            return true;
        } else if (res instanceof String[]) {
            return true;
        } else if (res instanceof Object[]) {
            return true;
        } else if (res instanceof String) {
            return isStringSuccess((String) res);
        } else if (res instanceof Long) {
            return isLongSuccess((Long) res);
        } else if (res instanceof Integer) {
            return isLongSuccess((Long) res);
        } else if (res instanceof byte[]) {
            return isByteSuccess((byte[]) res);
        }
        return false;
    }

    private boolean isByteSuccess(byte[] res) {
        String data = Strings.byteToString(res);
        try {
            long result = Long.valueOf(data);
            return true;
        } catch (Exception e) {

        }

        try {
            int result = Integer.valueOf(data);
            return true;
        } catch (Exception e) {

        }
        if (data.indexOf("ERROR") >= 0) {
            log.info("ERROR String :{}", data);
            return false;
        }

//        if((!"OK".equalsIgnoreCase(data)&&!"PONG".equalsIgnoreCase(data))||data.indexOf("error")>=0){
//            System.out.println("String :"+data);
//            return false;
//        }

        try {
            int a = Integer.parseInt(data);
            return true;
//            if(a>=0){
//                return true;
//            }
        } catch (Exception e) {

        }
        try {
            long a = Long.valueOf(data);
            return true;
//            if(a>=0L){
//                return true;
//            }
        } catch (Exception e) {

        }
        if ("PONG".equalsIgnoreCase(data)) {
            return true;
        }


        if ("OK".equalsIgnoreCase(data)) {
            return true;
        }
        return true;
    }

    public String getRes(Object res) {
        if (res == null) {
            return "返回值结果为NULL";
        }
        if (res instanceof String) {
            return String.valueOf(res);
        } else if (res instanceof Long) {

            return String.valueOf(res);
        } else if (res instanceof Integer) {
            return String.valueOf(res);
        } else if (res instanceof byte[]) {
            return Strings.byteToString((byte[]) res);
        }
        return "";
    }


    /**
     *     * byte[] 转  long
     * <p>
     *     * 2016年9月30日
     * <p>
     *    
     */
    public long bytesToLong(byte[] b) {
        long temp = 0;
        long res = 0;
        for (int i = 0; i < 8; i++) {
            temp = b[i] & 0xff;
            temp <<= 8 * i;
            res |= temp;
        }
        return res;
    }

    public boolean isIdempotentCommand(byte[] cmd) {
        String stringCmd = Strings.byteToString(cmd);

        PipeLineCompensatorEnum cmdEnum = null;
        try {
            cmdEnum = PipeLineCompensatorEnum.valueOf(stringCmd.toUpperCase());

            if (null == cmd) {
                cmdEnum = PipeLineCompensatorEnum.COMMAND;
            }
        } catch (Exception e) {
            cmdEnum = PipeLineCompensatorEnum.COMMAND;
        }
        if (cmdEnum.equals(PipeLineCompensatorEnum.INCR)) {
            return true;
        } else if (cmdEnum.equals(PipeLineCompensatorEnum.INCRBY)) {
            return true;
        } else if (cmdEnum.equals(PipeLineCompensatorEnum.INCRBYFLOAT)) {
            return true;
        } else if (cmdEnum.equals(PipeLineCompensatorEnum.DECR)) {
            return true;
        } else if (cmdEnum.equals(PipeLineCompensatorEnum.DECRBY)) {
            return true;
        } else if (cmdEnum.equals(PipeLineCompensatorEnum.APPEND)) {
            return true;
        }
        return false;
    }

    public PipeLineCompensatorEnum getIdempotentCommand(byte[] cmd) {
        String stringCmd = Strings.byteToString(cmd);

        PipeLineCompensatorEnum cmdEnum = null;
        try {
            cmdEnum = PipeLineCompensatorEnum.valueOf(stringCmd.toUpperCase());
            if (null == cmd) {
                cmdEnum = PipeLineCompensatorEnum.COMMAND;
            }
        } catch (Exception e) {
            cmdEnum = PipeLineCompensatorEnum.COMMAND;
        }


        if (cmdEnum.equals(PipeLineCompensatorEnum.INCR)) {
            return PipeLineCompensatorEnum.INCR;
        } else if (cmdEnum.equals(PipeLineCompensatorEnum.INCRBY)) {
            return PipeLineCompensatorEnum.INCRBY;
        } else if (cmdEnum.equals(PipeLineCompensatorEnum.INCRBYFLOAT)) {
            return PipeLineCompensatorEnum.INCRBYFLOAT;
        } else if (cmdEnum.equals(PipeLineCompensatorEnum.DECR)) {
            return PipeLineCompensatorEnum.DECR;
        } else if (cmdEnum.equals(PipeLineCompensatorEnum.DECRBY)) {
            return PipeLineCompensatorEnum.DECRBY;
        } else if (cmdEnum.equals(PipeLineCompensatorEnum.APPEND)) {
            return PipeLineCompensatorEnum.APPEND;
        }
        return null;
    }


}
