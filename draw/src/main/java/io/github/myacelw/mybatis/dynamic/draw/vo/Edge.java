package io.github.myacelw.mybatis.dynamic.draw.vo;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 边
 *
 * @author liuwei
 */
@Data
public class Edge {

    String id;

    String sourceNodeId;

    String targetNodeId;

    /**
     * 可空
     */
    String sourcePortId;

    /**
     * 可空
     */
    String targetPortId;

    /**
     * 属性信息
     */
    Map<String, String> properties = new LinkedHashMap<>();

    String leftText;

    String rightText;

    String centerText;

    /**
     * tip提示信息
     */
    String title;

    public void putProperty(String key, String value) {
        properties.put(key, value);
    }

    public String getProperty(String key) {
        return properties.get(key);
    }


}
