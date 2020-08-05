package syncer.syncerpluscommon.util.yml;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.text.StrSpliter;
import cn.hutool.core.util.StrUtil;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/7/24
 */
public class YmlPropUtils {
    private LinkedHashMap prop;
    private static YmlPropUtils ymlPropUtils = new YmlPropUtils();

    /**
     * 私有构造，禁止直接创建
     */
    private YmlPropUtils() {
        BootYaml yaml = new BootYaml();
        yaml.setActive("spring.profiles.active");
        yaml.setInclude("spring.profiles.include");
        yaml.setPrefix("application");
        prop = yaml.loadAs("application.yml");
    }

    /**
     * 获取单例
     *
     * @return YmlPropUtils
     */
    public static YmlPropUtils getInstance() {
        if (ymlPropUtils == null) {
            ymlPropUtils = new YmlPropUtils();
        }
        return ymlPropUtils;
    }

    /**
     * 根据属性名读取值
     * 先去主配置查询，如果查询不到，就去启用配置查询
     *
     * @param name 名称
     */
    public Object getProperty(String name) {
        LinkedHashMap param = prop;
        List<String> split = StrSpliter.split(name, StrUtil.C_DOT, true, true);
        for (int i = 0; i < split.size(); i++) {
            if (i == split.size() - 1) {
                return param.get(split.get(i));
            }
            param = Convert.convert(LinkedHashMap.class, param.get(split.get(i)));
        }
        return null;
    }
}
