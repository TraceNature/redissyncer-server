package syncer.transmission.tikv;

public enum TikvKeyType {
    STRING("w","STRING"),
    LIST("l","LIST"),
    SET("s","SET"),
    ZSET("z","ZSET"),
    HASH("h","HASH"),
    ;
    TikvKeyType(String code, String des) {
        this.code = code;
        this.des = des;
    }

    private String code;
    private String des;

    public String getCode() {
        return code;
    }

    public String getDes() {
        return des;
    }

    public static TikvKeyType getTikvKeyTypeByCode(String code){
        TikvKeyType[]data=TikvKeyType.values();
        for (int i=0;i<data.length;i++){
            if(data[i].getCode().equals(code)){
                return data[i];
            }
        }
        return null;
    }

}
