package syncer.syncerplusservice.util.file;

import java.io.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class JDSafeObjectInputStream extends ObjectInputStream {
    private Map<Long, Integer> classBlackList = new ConcurrentHashMap();

    public JDSafeObjectInputStream(InputStream in) throws IOException {
        super(in);
        this.classBlackList.put(-9030616758866828325L, 0);
        this.classBlackList.put(-8879969380201176639L, 0);
        this.classBlackList.put(-8424997852788830781L, 0);
        this.classBlackList.put(-7351430959676265986L, 0);
        this.classBlackList.put(-5549076877372674830L, 0);
        this.classBlackList.put(-4980094252148295498L, 0);
        this.classBlackList.put(-4784181656485585985L, 0);
        this.classBlackList.put(-4519235433996878101L, 0);
        this.classBlackList.put(-4227631860103686460L, 0);
        this.classBlackList.put(-2291619803571459675L, 0);
        this.classBlackList.put(-1811306045128064037L, 0);
        this.classBlackList.put(-864440709753525476L, 0);
        this.classBlackList.put(-595628298371744396L, 0);
        this.classBlackList.put(-137502678685784725L, 0);
        this.classBlackList.put(149041899799857404L, 0);
        this.classBlackList.put(199236452327460050L, 0);
        this.classBlackList.put(415980942713546887L, 0);
        this.classBlackList.put(1135685418585828802L, 0);
        this.classBlackList.put(1879837174626197802L, 0);
        this.classBlackList.put(2164749833121980361L, 0);
        this.classBlackList.put(3041236390478092030L, 0);
        this.classBlackList.put(5929029401661601669L, 0);
        this.classBlackList.put(6194596722027621084L, 0);
        this.classBlackList.put(7661065842829347047L, 0);
        this.classBlackList.put(7981148566008458638L, 0);
    }

    public JDSafeObjectInputStream(InputStream in, Set<String> blackList) throws IOException {
        super(in);
        this.setClassBlackList(blackList);
    }

    public void setClassBlackList(Set<String> blackList) {
        this.classBlackList.clear();
        Iterator iter = blackList.iterator();

        while(iter.hasNext()) {
            long hash = fnv1a_64((String)iter.next());
            this.classBlackList.put(hash, 0);
        }

    }

    private static long fnv1a_64(String key) {
        long hashCode = -3750763034362895579L;

        for(int i = 0; i < key.length(); ++i) {
            char ch = key.charAt(i);
            hashCode ^= (long)ch;
            hashCode *= 1099511628211L;
        }

        return hashCode;
    }

    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        String className = desc.getName();
        long classHash = fnv1a_64(className);
        if (this.classBlackList.containsKey(classHash)) {
            throw new InvalidClassException("UnSafe deserialization attempt", className);
        } else {
            return super.resolveClass(desc);
        }
    }

    public void addBlackList(String name) {
        if (name != null && name.length() != 0) {
            long hash = fnv1a_64(name);
            this.classBlackList.put(hash, 0);
        }
    }
}
