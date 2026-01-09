package io.github.myacelw.mybatis.dynamic.draw.vo;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 节点内的插槽，保存模型属性信息。
 *
 * @author liuwei
 */
@Data
public class Port {

    String id;

    Map<String, String> properties = new LinkedHashMap<>();

    String title;

    public void putProperty(String key, String value) {
        properties.put(key, value);
    }

    public String getProperty(String key) {
        return properties.get(key);
    }

}
