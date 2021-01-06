package syncer.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/3/25
 */
public class MD5Utils {

    private static final String ENCODING_ALGORITHM = "MD5";

    private static byte[] md5sum(byte[] data) {
        try {
            MessageDigest mdTemp = MessageDigest.getInstance(ENCODING_ALGORITHM);
            mdTemp.update(data);
            return mdTemp.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /* 将data数组转换为16进制字符串 */
    private static String convertToHexString(byte data[]) {
        StringBuffer strBuffer = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            strBuffer.append(Integer.toHexString(0xff & data[i]));
        }
        return strBuffer.toString();
    }

    private static byte[] md5sum(File file) {
        InputStream fis = null;
        byte[] buffer = new byte[1024];
        int numRead = 0;
        MessageDigest md5;
        try {
            fis = new FileInputStream(file);
            md5 = MessageDigest.getInstance(ENCODING_ALGORITHM);
            while ((numRead = fis.read(buffer)) > 0) {
                md5.update(buffer, 0, numRead);
            }
            return md5.digest();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取字符串的MD5值
     */
    public static String getMD5(String str) {
        if (str != null && str.length() > 0){
            return new String(convertToHexString(md5sum(str.getBytes())));
        }else{
            return null;
        }

    }

    /**
     * 获取文件的MD5值
     */
    public static String getMD5(File file) {
        if (file != null && file.exists()){
            return new String(convertToHexString(md5sum(file)));
        }else{
            return null;
        }

    }
}