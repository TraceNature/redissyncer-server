package syncer.syncerreplication.util.type;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/4/8
 */
public class Tuples {
    public static <T1, T2> SyncerTuple2<T1, T2> of(T1 t1, T2 t2) {
        return new SyncerTuple2<>(t1, t2);
    }

    public static <T1, T2, T3> SyncerTuple3<T1, T2, T3> of(T1 t1, T2 t2, T3 t3) {
        return new SyncerTuple3<>(t1, t2, t3);
    }

    public static <T1, T2, T3, T4> SyncerTuple4<T1, T2, T3, T4> of(T1 t1, T2 t2, T3 t3, T4 t4) {
        return new SyncerTuple4<>(t1, t2, t3, t4);
    }

    public static <T1, T2, T3, T4, T5> SyncerTuple5<T1, T2, T3, T4, T5> of(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5) {
        return new SyncerTuple5<>(t1, t2, t3, t4, t5);
    }
}
