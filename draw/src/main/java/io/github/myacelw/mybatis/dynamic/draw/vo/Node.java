package io.github.myacelw.mybatis.dynamic.draw.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 图卡片节点，保存模型或模型衍生出来的实体。
 *
 * @author liuwei
 */
@Data
public class Node {

    String id;

    /**
     * 属性信息
     */
    Map<String, String> properties = new LinkedHashMap<>();

    /**
     * tip提示信息
     */
    String title;

    /**
     * 属性插槽
     */
    List<Port> ports = new ArrayList<>();


    Integer x;

    Integer y;

    Integer height;

    Integer width;

    public void putProperty(String key, String value) {
        properties.put(key, value);
    }

    public String getProperty(String key) {
        return properties.get(key);
    }


}
