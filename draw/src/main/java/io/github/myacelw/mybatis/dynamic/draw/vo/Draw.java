package io.github.myacelw.mybatis.dynamic.draw.vo;

import lombok.Data;

import java.util.List;

/**
 * 画图
 *
 * @author liuwei
 */
@Data
public class Draw {
    /**
     * 节点列表
     */
    List<Node> nodes;

    /**
     * 连线
     */
    List<Edge> edges;
}
