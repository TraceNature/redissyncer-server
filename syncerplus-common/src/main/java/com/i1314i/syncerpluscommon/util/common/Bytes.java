package com.i1314i.syncerpluscommon.util.common;

/**
 * @author 平行时空
 * @created 2018-10-27 9:37
 **/
public class Bytes {
    public static String substring(String src, int start_idx, int end_idx){
        byte[] b = src.getBytes();
        String tgt = "";
        for(int i=start_idx; i<=end_idx; i++){
            tgt +=(char)b[i];
        }
        return tgt;
    }
}