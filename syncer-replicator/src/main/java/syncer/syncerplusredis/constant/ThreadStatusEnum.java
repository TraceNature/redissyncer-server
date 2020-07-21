package syncer.syncerplusredis.constant;



import java.io.Serializable;


public enum  ThreadStatusEnum implements Serializable {
    CREATING,CREATED,RUN,STOP,PAUSE,BROKEN,RDBRUNING,COMMANDRUNING
}
