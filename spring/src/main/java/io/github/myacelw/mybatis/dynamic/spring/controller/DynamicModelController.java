package io.github.myacelw.mybatis.dynamic.spring.controller;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.PageResult;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.util.RestRequestParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 动态模型通用控制器
 * 提供零代码 REST API 接口，支持所有已注册模型的 CRUD 操作。
 */
@Slf4j
@RestController
@RequestMapping("/api/dynamic/{modelName}")
@ConditionalOnProperty(prefix = "mybatis-dynamic.rest", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DynamicModelController {

    private final io.github.myacelw.mybatis.dynamic.core.service.ModelService modelService;

    @Autowired(required = false)
    private io.github.myacelw.mybatis.dynamic.spring.hook.CurrentUserHolder currentUserHolder;

    public DynamicModelController(io.github.myacelw.mybatis.dynamic.core.service.ModelService modelService) {
        this.modelService = modelService;
    }

    private DataManager<Object> getDataManager(String modelName) {
        io.github.myacelw.mybatis.dynamic.core.metadata.Model model = modelService.getModel(modelName);
        if (model == null) {
            throw new IllegalArgumentException("Model not found: " + modelName);
        }
        io.github.myacelw.mybatis.dynamic.core.metadata.Permission permission = null;
        if (currentUserHolder != null) {
            permission = currentUserHolder.getCurrentUserPermission(model);
        }
        return modelService.createDataManager(model, permission, null);
    }

    @GetMapping
    public PageResult<Map<String, Object>> list(@PathVariable String modelName, HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        DataManager<Object> dataManager = getDataManager(modelName);

        List<String> selectFields = null;
        String[] selectFieldsVal = parameterMap.get("selectFields");
        if (selectFieldsVal != null && selectFieldsVal.length > 0) {
            selectFields = Arrays.asList(selectFieldsVal[0].split(","));
        }

        return dataManager.pageChain()
                .select(selectFields)
                .where(RestRequestParser.parseCondition(parameterMap))
                .orderItems(RestRequestParser.parseOrderItems(parameterMap))
                .page(RestRequestParser.parsePage(parameterMap))
                .exec();
    }

    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable String modelName, @PathVariable Object id) {
        DataManager<Object> dataManager = getDataManager(modelName);
        return dataManager.getById(id);
    }

    @PostMapping
    public Object create(@PathVariable String modelName, @RequestBody Map<String, Object> data) {
        DataManager<Object> dataManager = getDataManager(modelName);
        return dataManager.insert(data);
    }

    @PutMapping
    public void update(@PathVariable String modelName, @RequestBody Map<String, Object> data) {
        DataManager<Object> dataManager = getDataManager(modelName);
        dataManager.update(data);
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable String modelName, @PathVariable Object id) {
        DataManager<Object> dataManager = getDataManager(modelName);
        return dataManager.delete(id);
    }
}