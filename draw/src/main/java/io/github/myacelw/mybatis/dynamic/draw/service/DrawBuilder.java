package io.github.myacelw.mybatis.dynamic.draw.service;


import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.*;
import io.github.myacelw.mybatis.dynamic.draw.vo.*;
import lombok.Setter;
import org.eclipse.elk.alg.layered.options.LayeredOptions;
import org.eclipse.elk.core.RecursiveGraphLayoutEngine;
import org.eclipse.elk.core.options.CoreOptions;
import org.eclipse.elk.core.util.BasicProgressMonitor;
import org.eclipse.elk.graph.ElkEdge;
import org.eclipse.elk.graph.ElkNode;
import org.eclipse.elk.graph.util.ElkGraphUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 绘制类生成器
 *
 * @author liuwei
 */
public class DrawBuilder {

    @Setter
    private int headHeight = 36;

    @Setter
    private int portHeight = 24;

    @Setter
    private int width = 220;

    private final List<Node> nodes = new ArrayList<>();

    private final List<Edge> edges = new ArrayList<>();

    public Draw build(List<Model> models) {
        for (Model model : models) {
            Node node = new Node();
            node.setId(model.getName());

            Map<PropertyName, String> properties = new LinkedHashMap<>();
            properties.put(PropertyName.name, model.getName());
            properties.put(PropertyName.comment, model.getTableDefine().getComment() == null ? model.getName() : model.getTableDefine().getComment());
            properties.put(PropertyName.tableName, model.getSchemaAndTableName());

            node.setTitle(properties.entrySet().stream().map(entry -> entry.getKey().getDisplayName() + ": " + entry.getValue()).collect(Collectors.joining("\n")));

            properties.put(PropertyName.name_comment, model.getTableDefine().getComment() == null ? model.getName() : model.getName() + " " + model.getTableDefine().getComment());
            properties.forEach((key, value) -> node.putProperty(key.name(), value));

            List<? extends Field> fields = model.getFields();
            addPorts(model, fields, node);
            nodes.add(node);
        }

        Draw draw = new Draw();
        draw.setNodes(nodes);

        Set<String> nodeIds = nodes.stream().map(Node::getId).collect(Collectors.toSet());
        //去除不存在目标的连线
        draw.setEdges(edges.stream().filter(t -> nodeIds.contains(t.getTargetNodeId())).collect(Collectors.toList()));

        doSetNodeXY(draw);
        return draw;
    }

    protected void doSetNodeXY(Draw draw) {
        Map<String, ElkNode> elkNodes = new LinkedHashMap<>();
        List<ElkEdge> elkEdges = new ArrayList<>();

        // 创建一个ELK图
        ElkNode elkGraph = ElkGraphUtil.createGraph();
        for (Node node : draw.getNodes()) {
            node.setWidth(this.width);
            node.setHeight(this.headHeight + this.portHeight * node.getPorts().size());

            // 创建一个节点
            ElkNode elkNode = ElkGraphUtil.createNode(elkGraph);
            elkNode.setIdentifier(node.getId());
            elkNode.setWidth(node.getWidth());
            elkNode.setHeight(node.getHeight());

            elkNodes.put(node.getId(), elkNode);
        }

        for (Edge edge : draw.getEdges()) {
            ElkNode source = elkNodes.get(edge.getSourceNodeId());
            ElkNode target = elkNodes.get(edge.getTargetNodeId());

            if (source != null && target != null) {
                // 创建一条边
                ElkEdge elkEdge = ElkGraphUtil.createSimpleEdge(source, target);
                elkEdges.add(elkEdge);
            }
        }

        // 设置布局算法选项
        elkGraph.setProperty(CoreOptions.ALGORITHM, LayeredOptions.ALGORITHM_ID);
        elkGraph.setProperty(LayeredOptions.SPACING_BASE_VALUE, 50d);

        // 执行布局
        RecursiveGraphLayoutEngine elkLayoutEngine = new RecursiveGraphLayoutEngine();
        elkLayoutEngine.layout(elkGraph, new BasicProgressMonitor());

        for (Node node : draw.getNodes()) {
            if (elkNodes.containsKey(node.getId())) {
                ElkNode elkNode = elkNodes.get(node.getId());
                node.setX((int) elkNode.getX());
                node.setY((int) elkNode.getY());
            }
        }
    }


    private void addPorts(Model model, List<? extends Field> fields, Node node) {
        for (Field field : fields) {
            if (field instanceof BasicField) {
                Port port = getPort((BasicField) field);
                node.getPorts().add(port);
            } else if (field instanceof ToOneField) {
                Port port = getToOnePort(node, (ToOneField) field);
                node.getPorts().add(port);
            } else if (field instanceof ToManyField) {
                Port port = getToManyFieldPort(node, (ToManyField) field);
                node.getPorts().add(port);
            } else if (field instanceof GroupField) {
                List<Port> ports = getFieldGroupFieldPort((GroupField) field);
                node.getPorts().addAll(ports);
            }
        }
    }

    private List<Port> getFieldGroupFieldPort(GroupField groupField) {
        List<Port> ports = new ArrayList<>();
        for (BasicField field : groupField.getFields()) {
            Port port = new Port();
            port.setId(groupField.getName() + "." + field.getName());

            Map<PropertyName, String> properties = new LinkedHashMap<>();
            properties.put(PropertyName.name, groupField.getName() + "." + field.getName());
            properties.put(PropertyName.fieldType, field.getClass().getSimpleName());
            properties.put(PropertyName.require, field.getColumnDefinition().getNotNull() == Boolean.TRUE ? "是" : "否");

            String comment2 = field.getColumnDefinition().getComment() == null ? field.getName() : field.getColumnDefinition().getComment();
            properties.put(PropertyName.comment, comment2);

            properties.put(PropertyName.javaClass, field.getJavaClass() == null ? "" : field.getJavaClass().getSimpleName());
            properties.put(PropertyName.columnName, field.getColumnName());

            properties.put(PropertyName.columnType, field.getColumnDefinition().getColumnType() != null ? field.getColumnDefinition().getColumnType() : "");

            properties.forEach((key, value) -> port.putProperty(key.name(), value));
            port.setTitle(properties.entrySet().stream().map(entry -> entry.getKey().getDisplayName() + ": " + entry.getValue()).collect(Collectors.joining("\n")));

            ports.add(port);
        }
        return ports;
    }

    private Port getToManyFieldPort(Node node, ToManyField field) {
        Port port = new Port();
        port.setId(field.getName());

        Map<PropertyName, String> properties = new LinkedHashMap<>();
        properties.put(PropertyName.name, field.getName());
        properties.put(PropertyName.fieldType, field.getClass().getSimpleName());

        String clazz = field.getJavaClass() == null ? "Map" : field.getJavaClass().getSimpleName();
        properties.put(PropertyName.javaClass, "List<" + clazz + ">");

        port.setTitle(
                properties.entrySet().stream().map(entry -> entry.getKey().getDisplayName() + ": " + entry.getValue()).collect(Collectors.joining("\n"))
                        + "\n" + "关联模型: " + field.getTargetModel()
        );

        properties.put(PropertyName.columnName, "[" + field.getTargetModel() + "]");
        properties.put(PropertyName.columnType, "");

        properties.forEach((key, value) -> port.putProperty(key.name(), value));

        Edge edge = new Edge();
        edge.setId(node.getId() + "." + field.getName());
        edge.setSourceNodeId(node.getId());
        edge.setSourcePortId(port.getId());
        edge.setTargetNodeId(field.getTargetModel());
        edge.setCenterText("*");
        edge.setTitle("目标表: " + field.getTargetModel()
                + "\n" + "外键: " + String.join(", ", field.getJoinTargetFields()));
        edges.add(edge);
        return port;
    }

    private Port getToOnePort(Node node, ToOneField field) {
        Port port = new Port();
        port.setId(field.getName());

        Map<PropertyName, String> properties = new LinkedHashMap<>();
        properties.put(PropertyName.name, field.getName());
        properties.put(PropertyName.fieldType, field.getClass().getSimpleName());

        String clazz = field.getJavaClass() == null ? "Map" : field.getJavaClass().getSimpleName();
        properties.put(PropertyName.javaClass, clazz);


        port.setTitle(
                properties.entrySet().stream().map(entry -> entry.getKey().getDisplayName() + ": " + entry.getValue()).collect(Collectors.joining("\n"))
                        + "\n" + "关联模型: " + field.getTargetModel() +
                        "\n" + "关联字段: " + String.join(", ", field.getJoinLocalFields())
        );

        properties.put(PropertyName.columnName, "[" + field.getTargetModel() + "]");
        properties.put(PropertyName.columnType, "");

        properties.forEach((key, value) -> port.putProperty(key.name(), value));
        
        if (!Objects.equals(field.getTargetModel(), node.getId())) {
            Edge edge = new Edge();
            edge.setId(node.getId() + "." + field.getName());
            edge.setSourceNodeId(node.getId());
            edge.setSourcePortId(port.getId());
            edge.setTargetNodeId(field.getTargetModel());
            edge.setCenterText("0..1");
            edges.add(edge);
        }
        return port;
    }

    private Port getPort(BasicField field) {
        Port port = new Port();
        port.setId(field.getName());

        Map<PropertyName, String> properties = new LinkedHashMap<>();
        properties.put(PropertyName.name, field.getName());
        properties.put(PropertyName.fieldType, field.getClass().getSimpleName());
        properties.put(PropertyName.require, field.getColumnDefinition().getNotNull() == Boolean.TRUE ? "是" : "否");
        properties.put(PropertyName.comment, field.getColumnDefinition().getComment() == null ? field.getName() : field.getColumnDefinition().getComment());

        properties.put(PropertyName.javaClass, field.getJavaClass() == null ? "" : field.getJavaClass().getSimpleName());
        properties.put(PropertyName.columnName, field.getColumnName());

        properties.put(PropertyName.columnType, field.getColumnDefinition().getColumnType() != null ? field.getColumnDefinition().getColumnType() : "");

        properties.forEach((key, value) -> port.putProperty(key.name(), value));
        port.setTitle(properties.entrySet().stream().map(entry -> entry.getKey().getDisplayName() + ": " + entry.getValue()).collect(Collectors.joining("\n")));
        return port;
    }

}
