package io.github.myacelw.mybatis.dynamic.spring.controller;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.condition.Condition;
import io.github.myacelw.mybatis.dynamic.spring.service.BaseService;
import io.github.myacelw.mybatis.dynamic.spring.vo.QueryRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 抽象的Controller实现类
 *
 * @author liuwei
 */
public abstract class AbstractController<ID, T> {

    protected BaseService<ID, T> service;

    @GetMapping("/{id}")
    public T getById(@PathVariable ID id, @RequestParam(required = false) List<String> joinFieldNames) {
        return service.getById(id, joinFieldNames);
    }

    @GetMapping("/list")
    public List<T> list() {
        return service.list();
    }

    @PostMapping("/insert")
    public Optional<ID> insert(@RequestBody T data) {
        return Optional.of(service.insert(data));
    }

    @PostMapping("/update")
    public void update(@RequestBody T data) {
        service.update(data);
    }

    @PostMapping("/updateOnlyNonNull")
    public void updateOnlyNonNull(@RequestBody T data) {
        service.updateOnlyNonNull(data);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable ID id) {
        service.delete(id);
    }

    @PostMapping("/batchInsert")
    public List<ID> batchInsert(@RequestBody List<T> data) {
        return service.batchInsert(data);
    }

    @PostMapping("/batchUpdate")
    public void batchUpdate(@RequestBody List<T> data) {
        service.batchUpdate(data);
    }

    @PostMapping("/batchDelete")
    public void batchDelete(@RequestParam List<ID> idList) {
        service.batchDelete(idList);
    }

    @PostMapping("/query")
    public List<T> query(@RequestBody(required = false) QueryRequest request) {
        if (request == null) {
            return service.list();
        }
        return service.query().where(request.getCondition()).orderItems(request.getOrderItems())
                .page(request.getPage()).select(request.getSelectFields()).exec();
    }

    @PostMapping("/count")
    public int count(@RequestBody(required = false) Condition condition) {
        return service.count(condition);
    }

    @Autowired
    public void setService(@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") BaseService<ID, T> service) {
        this.service = service;
    }

}
