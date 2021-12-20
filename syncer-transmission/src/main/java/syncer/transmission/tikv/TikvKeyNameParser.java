package syncer.transmission.tikv;

import syncer.replica.exception.TikvKeyErrorException;
import syncer.replica.util.strings.Strings;

public class TikvKeyNameParser {
    private final static String START="*";
    private final static String splicN="_";
    private final static Integer spN=4;
    public TikvKey parser(byte[]key,TikvKeyType tikvType) throws TikvKeyErrorException {
        String stringKey= Strings.toString(key);
        if(!stringKey.startsWith(START)){
            throw new TikvKeyErrorException("key error");
        }
        if(TikvKeyType.STRING.equals(tikvType)){
            return parserString(key);
        }else if(TikvKeyType.LIST.equals(tikvType)){
            return parserList(key);

        }
        return null;
    }

    /**
     * *{instId}_{dbNum}_{commandType}_{keyName}_{index}
     * @param key
     * @return
     * @throws TikvKeyErrorException
     */
    TikvKey parserString(byte[]key) throws TikvKeyErrorException{
        String stringKey= Strings.toString(key);
        String[] stringKeys=stringKey.split("");
        StringBuilder aloneKey=new StringBuilder();
        TikvKey tikvKey=TikvKey.builder().build();
        int index=0;
        for (int i=1;i<stringKeys.length;i++){
            if(splicN.equals(stringKeys[i])){
                if(index==0){
                    tikvKey.setInstId(aloneKey.toString());
                }else if (index==1){
                    tikvKey.setCurrentDbNumber(Long.valueOf(aloneKey.toString()));
                }else if (index==2){
                    tikvKey.setKeyType(TikvKeyType.getTikvKeyTypeByCode(Integer.valueOf(aloneKey.toString())));
                    String lastKey = stringKey.substring(i+1, stringKey.length());
                    tikvKey.setStringKey(lastKey);
                    tikvKey.setKey(lastKey.getBytes());
                }else{
                    break;
                }
                index++;
                aloneKey.delete(0,aloneKey.length());
                continue;
            }
            aloneKey.append(stringKeys[i]);
        }

        return tikvKey;
    }

    /**
     * *{instId}_{dbNum}_{commandType}_{keyName}_{index}
     * @param key
     * @return
     * @throws TikvKeyErrorException
     */
    TikvKey parserList(byte[]key){
        String stringKey= Strings.toString(key);
        String[] stringKeys=stringKey.split("");
        StringBuilder aloneKey=new StringBuilder();
        TikvKey tikvKey=TikvKey.builder().build();
        int index=0;
        for (int i=1;i<stringKeys.length;i++){
            if(splicN.equals(stringKeys[i])){
                if(index==0){
                    tikvKey.setInstId(aloneKey.toString());
                }else if (index==1){
                    tikvKey.setCurrentDbNumber(Long.valueOf(aloneKey.toString()));
                }else if (index==2){
                    tikvKey.setKeyType(TikvKeyType.getTikvKeyTypeByCode(Integer.valueOf(aloneKey.toString())));
                    String lastKey = stringKey.substring(i+1,stringKey.lastIndexOf(splicN));
                    tikvKey.setStringKey(lastKey);
                    tikvKey.setKey(lastKey.getBytes());
                    tikvKey.setIndex(Integer.valueOf(stringKey.substring(stringKey.lastIndexOf(splicN)+1,stringKey.length())));
                }else{
                    break;
                }
                index++;
                aloneKey.delete(0,aloneKey.length());
                continue;
            }
            aloneKey.append(stringKeys[i]);
        }

        return tikvKey;
    }


//    public static String getKey(String instId,long currentDbNumber,TikvKeyType keyType,byte[]key){
//        if(TikvKeyType.STRING.equals(keyType)){
//            return getStringKey(instId,currentDbNumber,keyType,key);
//        }
//
//    }

    /**
     * *{instId}_{dbNum}_{commandType}_{keyName}
     * @param instId
     * @param currentDbNumber
     * @param keyType
     * @param key
     * @return
     * @throws TikvKeyErrorException
     */
    public String getStringKey(String instId,long currentDbNumber,TikvKeyType keyType,byte[]key){
        StringBuilder stringKey=new StringBuilder(START);
        stringKey.append(instId)
                .append(splicN)
                .append(currentDbNumber)
                .append(splicN)
                .append(keyType.getCode())
                .append(splicN)
                .append(Strings.byteToString(key));
        return stringKey.toString();
    }


    /**
     * *{instId}_{dbNum}_{commandType}_{keyName}_{index}
     * @param instId
     * @param currentDbNumber
     * @param keyType
     * @param key
     * @return
     * @throws TikvKeyErrorException
     */
    public String getListKey(String instId,long currentDbNumber,TikvKeyType keyType,byte[]key,int index){
        StringBuilder stringKey=new StringBuilder(START);
        stringKey.append(instId)
                .append(splicN)
                .append(currentDbNumber)
                .append(splicN)
                .append(keyType.getCode())
                .append(splicN)
                .append(Strings.byteToString(key))
                .append(splicN)
                .append(index);
        return stringKey.toString();
    }

    public static void main(String[] args) throws TikvKeyErrorException {
        TikvKeyNameParser tikvKeyNameParser=new TikvKeyNameParser();
        String key=tikvKeyNameParser.getStringKey("{instantId}",0,TikvKeyType.LIST,"{key}".getBytes());
        System.out.println(key);
        System.out.println(tikvKeyNameParser.parser(key.getBytes(),TikvKeyType.STRING));
    }
}
