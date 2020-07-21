package syncer.syncerpluscommon.util.file;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SafeObjectInputStream extends ObjectInputStream {

    protected static final Set<String> DEFAULT_NO_DESERIALIZE_CLASS_NAMES;

    static {

        Set<String> s = new HashSet<>();
        s.add("org.apache.commons.collections.functors.InvokerTransformer");
        s.add("org.apache.commons.collections.functors.InstantiateTransformer");
        s.add("org.apache.commons.collections4.functors.InvokerTransformer");
        s.add("org.apache.commons.collections4.functors.InstantiateTransformer");
        s.add("org.codehaus.groovy.runtime.ConvertedClosure");
        s.add("org.codehaus.groovy.runtime.MethodClosure");
        s.add("org.springframework.beans.factory.ObjectFactory");
        s.add("com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl");
        s.add("org.apache.xalan.xsltc.trax.TemplatesImpl");
        DEFAULT_NO_DESERIALIZE_CLASS_NAMES = Collections.unmodifiableSet(s);
    }


    public SafeObjectInputStream(InputStream in) throws IOException {
        super(in);
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        String name = desc.getName();

        if (isBlacklisted(name)) {
            throw new SecurityException("Illegal type to deserialize: prevented for security reasons");
        }

        return super.resolveClass(desc);
    }

    private boolean isBlacklisted(String name) {
        for (String list : DEFAULT_NO_DESERIALIZE_CLASS_NAMES) {
            if (name.endsWith(list)) {
                return true;
            }
        }

        return false;
    }
}