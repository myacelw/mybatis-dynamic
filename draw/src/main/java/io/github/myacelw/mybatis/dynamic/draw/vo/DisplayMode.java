package io.github.myacelw.mybatis.dynamic.draw.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 节点显示方式
 *
 * @author liuwei
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DisplayMode {

    /**
     * 显示方式名称
     */
    String name;

    /**
     * 显示的节点属性名
     */
    String nodePropertyName;

    /**
     * 第二显示节点属性名
     */
    String secondNodePropertyName;

    /**
     * 主显示Port属性名
     */
    String mainPortPropertyName;

    /**
     * 第二显示Port属性名
     */
    String secondPortPropertyName;

}
