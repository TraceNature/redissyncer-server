package syncer.syncerpluscommon.util.yml;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.StrSpliter;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.List;
/**
 * @author zhanenqiang
 * @Description 描述
 * @Date 2020/7/24
 */
public class BootYaml extends Yaml {
    /**
     * 环境配置路径的键值
     */
    private String active;
    /**
     * 引入yml的键值
     */
    private String include;
    /**
     * 配置文件的前缀
     */
    private String prefix;

    /**
     * <p>方法名称：根据application.yml转化为LinkedHashMap.</p>
     * <p>详细描述：会解析spring.profiles.active启用的配置和spring.profiles.include引入的文件.</p>
     * <p>创建时间：2019-07-10 17:39:38</p>
     * <p>创建作者：李兴武</p>
     * <p>修改记录：</p>
     *
     * @param path application.yml
     * @return the linked hash map
     * @author "lixingwu"
     */
    public LinkedHashMap loadAs(String path) {
        // 组合一个map，把启用的配置，引入的文件组合起来
        LinkedHashMap<String, Object> mapAll = new LinkedHashMap<>();
        LinkedHashMap<String, Object> mainMap = yml2Map(path);
        // 读取启用的配置
        Object active = mainMap.get(this.active);
        if (!ObjectUtil.isNull(active)) {
            mapAll.putAll(yml2Map(StrUtil.format("{}-{}.yml", this.prefix, active)));
        }
        // 加载引入的yml
        Object include = mainMap.get(this.include);
        // include是使用逗号分隔开的，需要切割一下
        List<String> split = StrSpliter.split(Convert.toStr(include), StrUtil.C_COMMA, true, true);
        for (String inc : split) {
            mapAll.putAll(yml2Map(StrUtil.format("{}-{}.yml", this.prefix, inc)));
        }
        // 主配置覆盖其他配置
        mapAll.putAll(mainMap);
        // 把map转化为字符串
        String mapString = MapUtil.joinIgnoreNull(mapAll, "\n", "=");
        // 再把map字符串转化为yamlStr字符串
        String yamlStr = properties2YamlStr(mapString);
        // 使用Yaml构建LinkedHashMap
        return super.loadAs(yamlStr, LinkedHashMap.class);
    }

    /**
     * <p>方法名称：Yml 格式转 LinkedHashMap.</p>
     * <p>详细描述：转载自 https://www.cnblogs.com/xujingyang/p/10613206.html .</p>
     * <p>创建时间：2019-07-10 09:30:19</p>
     * <p>创建作者：李兴武</p>
     * <p>修改记录：</p>
     *
     * @param path Yml路径
     * @author "lixingwu"
     */
    public LinkedHashMap<String, Object> yml2Map(String path) {
        final String dot = ".";
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        ClassPathResource resource = new ClassPathResource(path);
        // 文件不存在，置空
        if (ObjectUtil.isNull(resource)) {
            return map;
        }
        BufferedReader reader = resource.getReader(Charset.defaultCharset());
        try {
            YAMLFactory yamlFactory = new YAMLFactory();
            YAMLParser parser = yamlFactory.createParser(reader);
            StringBuilder key = new StringBuilder();
            String value;
            JsonToken token = parser.nextToken();
            while (token != null) {
                if (!JsonToken.START_OBJECT.equals(token)) {
                    if (JsonToken.FIELD_NAME.equals(token)) {
                        if (key.length() > 0) {
                            key.append(dot);
                        }
                        key.append(parser.getCurrentName());

                        token = parser.nextToken();
                        if (JsonToken.START_OBJECT.equals(token)) {
                            continue;
                        }
                        value = parser.getText();
                        map.put(key.toString(), value);

                        int dotOffset = key.lastIndexOf(dot);
                        if (dotOffset > 0) {
                            key = new StringBuilder(key.substring(0, dotOffset));
                        }
                    } else if (JsonToken.END_OBJECT.equals(token)) {
                        int dotOffset = key.lastIndexOf(dot);
                        if (dotOffset > 0) {
                            key = new StringBuilder(key.substring(0, dotOffset));
                        } else {
                            key = new StringBuilder();
                        }
                    }
                }
                token = parser.nextToken();
            }
            parser.close();
            return map;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * <p>方法名称：Properties内容转化为yaml内容.</p>
     * <p>详细描述：.</p>
     * <p>创建时间：2019-07-10 15:06:48</p>
     * <p>创建作者：李兴武</p>
     * <p>修改记录：</p>
     *
     * @param content Properties内容
     * @return the string
     * @author "lixingwu"
     */
    public String properties2YamlStr(String content) {
        // 临时生成yml
        String filePath = FileUtil.getTmpDirPath() + "/temp.yml";

        JsonParser parser;
        JavaPropsFactory factory = new JavaPropsFactory();
        try {
            parser = factory.createParser(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            YAMLFactory yamlFactory = new YAMLFactory();
            YAMLGenerator generator = yamlFactory.createGenerator(FileUtil.getOutputStream(filePath));
            JsonToken token = parser.nextToken();
            while (token != null) {
                if (JsonToken.START_OBJECT.equals(token)) {
                    generator.writeStartObject();
                } else if (JsonToken.FIELD_NAME.equals(token)) {
                    generator.writeFieldName(parser.getCurrentName());
                } else if (JsonToken.VALUE_STRING.equals(token)) {
                    generator.writeString(parser.getText());
                } else if (JsonToken.END_OBJECT.equals(token)) {
                    generator.writeEndObject();
                }
                token = parser.nextToken();
            }
            parser.close();
            generator.flush();
            generator.close();
            // 读取临时生成yml的内容
            String ymlContent = FileUtil.readUtf8String(filePath);
            // 删除临时生成yml
            FileUtil.del(filePath);
            return ymlContent;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public String getInclude() {
        return include;
    }

    public void setInclude(String include) {
        this.include = include;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }


}
