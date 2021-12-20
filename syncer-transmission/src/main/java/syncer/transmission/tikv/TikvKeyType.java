package syncer.transmission.tikv;

public enum TikvKeyType {
    STRING(0,"STRING"),
    LIST(1,"LIST")
    ;
    TikvKeyType(int code, String des) {
        this.code = code;
        this.des = des;
    }

    private int code;
    private String des;

    public int getCode() {
        return code;
    }

    public String getDes() {
        return des;
    }

    public static TikvKeyType getTikvKeyTypeByCode(int code){
        TikvKeyType[]data=TikvKeyType.values();
        for (int i=0;i<data.length;i++){
            if(data[i].getCode()==code){
                return data[i];
            }
        }
        return null;
    }

}
