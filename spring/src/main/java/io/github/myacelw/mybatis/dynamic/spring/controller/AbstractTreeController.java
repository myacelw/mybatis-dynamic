package io.github.myacelw.mybatis.dynamic.spring.controller;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.OrderItem;
import io.github.myacelw.mybatis.dynamic.spring.entity.BaseEntity;
import io.github.myacelw.mybatis.dynamic.spring.entity.BaseTreeEntity;
import io.github.myacelw.mybatis.dynamic.spring.service.BaseTreeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 抽象的树形Controller实现类
 *
 * @author liuwei
 */
public abstract class AbstractTreeController<ID, T> extends AbstractController<ID, T> {

    protected BaseTreeService<ID, T> service;

    @Autowired
    public void setService(BaseTreeService<ID, T> service) {
        super.setService(service);
        this.service = service;
    }

    @GetMapping("/tree/{id}")
    public T getTreeById(@PathVariable ID id) {
        return service.getRecursiveTreeById(id);
    }

    @PostMapping("/tree")
    public List<T> getTree(@RequestBody(required = false) List<OrderItem> orderItems) {
        return service.queryRecursiveTree(orderItems);
    }

    @GetMapping("/tree")
    public List<T> getTree() {
        List<OrderItem> orderItems = new ArrayList<>();
        if (BaseTreeEntity.class.isAssignableFrom(service.getEntityClass())) {
            orderItems.add(OrderItem.asc(BaseTreeEntity.Fields.orderString));
        } else if (BaseEntity.class.isAssignableFrom(service.getEntityClass())) {
            orderItems.add(OrderItem.desc(BaseEntity.Fields.id));
        }
        return service.queryRecursiveTree(orderItems);
    }

    /**
     * 移动数据最前面。
     */
    @GetMapping("/tree/moveToFirst/{id}")
    public void moveToFirst(@PathVariable ID id) {
        service.moveToFirst(id);
    }

    /**
     * 移动数据最后面。
     */
    @GetMapping("/tree/moveToLast/{id}")
    public void moveToLast(@PathVariable ID id) {
        service.moveToLast(id);
    }

    /**
     * 移动数据到目标节点之间。
     * next为空时移动到最后，previous为空时移动到最前，都为空时移动到最后面。
     */
    @GetMapping("/tree/move/{id}")
    public void move(@PathVariable ID id, @RequestParam(required = false) String previous, @RequestParam(required = false) String next) {
        service.move(id, previous, next);
    }

}
