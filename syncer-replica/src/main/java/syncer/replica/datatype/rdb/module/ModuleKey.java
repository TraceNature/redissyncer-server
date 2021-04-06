package syncer.replica.datatype.rdb.module;

/**
 * @author: Eq Zhan
 * @create: 2021-03-18
 **/
public class ModuleKey {
    private final String moduleName;
    private final int moduleVersion;

    private ModuleKey(String moduleName, int moduleVersion) {
        this.moduleName = moduleName;
        this.moduleVersion = moduleVersion;
    }

    public static ModuleKey key(String moduleName, int moduleVersion) {
        return new ModuleKey(moduleName, moduleVersion);
    }

    public String getModuleName() {
        return moduleName;
    }

    public int getModuleVersion() {
        return moduleVersion;
    }

    @Override
    public String toString() {
        return "ModuleKey{" +
                "moduleName='" + moduleName + '\'' +
                ", moduleVersion=" + moduleVersion +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ModuleKey moduleKey = (ModuleKey) o;

        if (moduleVersion != moduleKey.moduleVersion) return false;
        return moduleName.equals(moduleKey.moduleName);
    }

    @Override
    public int hashCode() {
        int result = moduleName.hashCode();
        result = 31 * result + moduleVersion;
        return result;
    }
}
