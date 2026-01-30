package io.github.myacelw.mybatis.dynamic.core.service;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.myacelw.mybatis.dynamic.core.exception.BaseException;
import io.github.myacelw.mybatis.dynamic.core.exception.data.DataNotFoundException;
import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.BasicField;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.Field;
import io.github.myacelw.mybatis.dynamic.core.metadata.field.ToManyField;
import io.github.myacelw.mybatis.dynamic.core.util.Assert;
import io.github.myacelw.mybatis.dynamic.core.util.ObjectUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.ibatis.parsing.GenericTokenParser;
import org.apache.ibatis.scripting.xmltags.OgnlCache;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 模型数据加载工具
 *
 * @author liuwei
 */
@RequiredArgsConstructor
public class ModelDataLoader {

    private final static ObjectMapper OM = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
            .build();

    private final ModelService modelService;

    @Setter
    private Class<?> idType = Integer.class;

    /**
     * 获取模型并更新
     *
     * @param filePath 模型配置文件，为json格式
     */
    public Map<String, Model> updateAndRegister(String filePath) {
        Map<String, Model> models = getModels(filePath);
        models.forEach((modelName, model) -> {
            modelService.updateAndRegister(model);
        });
        return models;
    }

    /**
     * 更新模型
     */
    @SneakyThrows
    public Map<String, Model> getModels(String filePath) {
        Map<String, Model> models = new LinkedHashMap<>();

        //读取并初始化模型配置
        List<Model> modelList;
        try (InputStream inputStream = getInputStream(filePath)) {
            modelList = OM.readerForListOf(Model.class).readValue(inputStream);
        }
        for (Model model : modelList) {
            initModel(model);
            models.put(model.getName(), model);
        }
        return models;
    }

    protected void initModel(Model model) {
        model.getFields().forEach(field -> {
            if (field instanceof BasicField && field.getJavaClass() == null) {
                ((BasicField) field).setJavaClass(String.class);
            }
        });
        if (model.getPrimaryKeyFields() == null) {
            model.addCommonFieldsIfNotExist(idType);
        }
    }

    private InputStream getInputStream(String resourcePath) throws IOException {
        if (resourcePath.startsWith("file:")) {
            return Files.newInputStream(Paths.get(resourcePath.substring("file:".length())));
        }

        String path = resourcePath;
        if (path.startsWith("classpath:")) {
            path = path.substring("classpath:".length());
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        if (is == null) {
            is = ModelDataLoader.class.getResourceAsStream(path);
        }
        if (is == null) {
            throw new FileNotFoundException("Cannot find resource '" + resourcePath + "' in classpath.");
        }
        return is;
    }

    /**
     * 初始化模型数据
     *
     * @param initJsonFilePatterns 初始化数据文件模板，为json格式，格式如：
     * @code [
     * {"model": "Company", "method":"insertOrUpdate", "data": [{"id" : "c1", "name" : "公司A"}]},
     * {"model": "Company", "data": {"id" : "c2", "name" : "公司B"}}
     * ]
     */
    @SneakyThrows
    public Map<String, List<Map<String, Object>>> initModelData(SqlSessionFactory sqlSessionFactory, String... initJsonFilePatterns) {
        return initModelData(sqlSessionFactory, getResources(initJsonFilePatterns));
    }

    @SneakyThrows
    public Map<String, List<Map<String, Object>>> initModelData(SqlSessionFactory sqlSessionFactory, List<String> initDataFiles) {
        return initModelData(sqlSessionFactory, getResources(initDataFiles == null ? null : initDataFiles.toArray(new String[0])));
    }

    @SneakyThrows
    public Map<String, List<Map<String, Object>>> initModelData(SqlSessionFactory sqlSessionFactory, @NonNull Collection<URL> initJsonFileResources) {
        Map<String, List<Map<String, Object>>> result = new HashMap<>();

        for (URL resource : initJsonFileResources) {
            //读取并初始化模型数据
            try (InputStream inputStream = resource.openStream()) {
                try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
                    List<Map<String, Object>> dataList = OM.readerForListOf(Map.class).readValue(inputStream);
                    for (Map<String, Object> data : dataList) {
                        initDataInsert(sqlSession, data, result);
                    }
                }
            }
        }

        return result;
    }


    @SneakyThrows
    private static Collection<URL> getResources(String[] paths) {
        if (ObjectUtil.isEmpty(paths)) {
            return Collections.emptyList();
        }

        Set<URL> resources = new LinkedHashSet<>();

        for (String path : paths) {
            if (path.startsWith("file:")) {
                resources.add(new File(path.substring("file:".length())).toURI().toURL());
            } else {
                String classpathPath = path;
                if (classpathPath.startsWith("classpath:")) {
                    classpathPath = classpathPath.substring("classpath:".length());
                }
                if (classpathPath.startsWith("/")) {
                    classpathPath = classpathPath.substring(1);
                }
                Enumeration<URL> urlEnum = Thread.currentThread().getContextClassLoader().getResources(classpathPath);
                while (urlEnum.hasMoreElements()) {
                    resources.add(urlEnum.nextElement());
                }
            }
        }
        return resources;
    }

    @SneakyThrows
    private void initDataInsert(SqlSession sqlSession, Map<String, Object> data, Map<String, List<Map<String, Object>>> result) {
        if (data.size() == 1 && data.containsKey("#")) {
            return;
        }
        String modelName = (String) data.get("model");
        Assert.notNull(modelName, "Init data configuration error, 'model' not set: " + ellipsisJson(data));

        Object rowOrList = data.get("data");
        Assert.notNull(rowOrList, "Model data 'data' not configured: " + ellipsisJson(data));

        DataManager<Object> dataManager = this.modelService.getDataManager(modelName, sqlSession);
        Assert.notNull(dataManager, "Model [" + modelName + "] not found, " + ellipsisJson(data));

        String method = (String) data.get("method");

        List<String> updateFields = getUpdateFields(data);

        if (rowOrList instanceof Collection) {
            ((Collection<?>) rowOrList).forEach(t -> handelRowData(method, (Map) t, updateFields, dataManager, result));
        } else {
            handelRowData(method, (Map) rowOrList, updateFields, dataManager, result);
        }
    }

    private static List<String> getUpdateFields(Map<String, Object> data) {
        Object updateFieldsTemp = data.get("updateFields");
        List<String> updateFields = null;
        if (updateFieldsTemp instanceof Collection) {
            updateFields = new ArrayList<>();
            for (Object t : ((Collection<?>) updateFieldsTemp)) {
                updateFields.add((String) t);
            }
        } else if (updateFieldsTemp instanceof String) {
            updateFields = Collections.singletonList((String) updateFieldsTemp);
        }
        return updateFields;
    }

    private void handelRowData(String method, Map<String, Object> rowData, List<String> updateFields, DataManager<Object> manager, Map<String, List<Map<String, Object>>> initDataMap) {
        if (method == null) {
            method = "insertOrUpdate";
        }
        if ("delete".equals(method) || "logicDelete".equals(method)) {
            deleteRowData(manager.getModel(), rowData, t -> manager.delete(t));
        } else if ("physicalDelete".equals(method)) {
            deleteRowData(manager.getModel(), rowData, t -> manager.delete(t, true));
        } else if ("insert".equals(method) || "insertOrUpdate".equals(method)) {
            insertOrUpdateRowData(method, rowData, updateFields, manager, initDataMap);
        } else {
            throw new ModelDataLoaderException("Init data configuration error, 'method' invalid: " + ellipsisJson(rowData));
        }
    }

    private void deleteRowData(Model model, Map<String, Object> rowData, Consumer<Object> onDelete) {
        Object id = getIdValue(model, rowData);
        try {
            onDelete.accept(id);
        } catch (DataNotFoundException e) {
            //数据不存在忽略异常
        }
    }

    private void insertOrUpdateRowData(String method, Map<String, Object> rowData, List<String> updateFields, DataManager<Object> manager, Map<String, List<Map<String, Object>>> initDataMap) {
        Object id = getIdValue(manager.getModel(), rowData);
        initRowData(rowData, initDataMap);
        Map<String, Object> oldRowData = manager.getByIdChain().id(id).selectFields(manager.getModel().getPrimaryKeyFields()).nullNotThrowException().exec();
        if (oldRowData == null) {
            try {
                manager.insertDisableGenerateId(rowData);
            } catch (Exception e) {
                throw new ModelDataLoaderException("Error writing init data for model [" + manager.getModel().getName() + "], " + e.getMessage() + ", insert data: " + ellipsisJson(rowData), e);
            }
        } else if ("insertOrUpdate".equals(method) || "update".equals(method)) {
            try {
                manager.updateChain().id(id).data(rowData).updateFields(updateFields).exec();
            } catch (Exception e) {
                throw new ModelDataLoaderException("Error writing init data for model [" + manager.getModel().getName() + "], " + e.getMessage() + ", update data: " + ellipsisJson(rowData), e);
            }
        }

        for (Field field : manager.getModel().getFields()) {
            if (oldRowData == null || (("insertOrUpdate".equals(method) || "update".equals(method)) && (updateFields == null || updateFields.contains(field.getName())))) {
                if (field instanceof ToManyField && !ObjectUtil.isEmpty(rowData.get(field.getName()))) {
                    ToManyField toManyField = (ToManyField) field;
                    List<Map<String, Object>> relData = (List<Map<String, Object>>) rowData.get(field.getName());
                    DataManager<Object> relDataManager = this.modelService.getDataManager(toManyField.getTargetModel(), manager.getModelContext().getSqlSession());
                    relData.forEach(t -> {
                                if (toManyField.getJoinTargetFields().length == 1) {
                                    t.put(toManyField.getJoinTargetFields()[0], id);
                                } else {
                                    List ids = (List) id;
                                    for (int i = 0; i < toManyField.getJoinTargetFields().length; i++) {
                                        t.put(toManyField.getJoinTargetFields()[i], ids.get(i));
                                    }
                                }

                                relDataManager.insertOrUpdate(t);
                            }
                    );
                }
            }
        }

        List<Map<String, Object>> store = initDataMap.computeIfAbsent(manager.getModel().getName(), k -> new ArrayList<>());
        store.add(rowData);
    }

    private Object getIdValue(Model model, Map<String, Object> rowData) {
        String[] primaryKeyFieldNames = model.getPrimaryKeyFields();
        Object id;
        if (primaryKeyFieldNames.length == 1) {
            id = rowData.get(primaryKeyFieldNames[0]);
        } else {
            id = Arrays.stream(primaryKeyFieldNames).map(rowData::get).collect(Collectors.toList());
        }
        if (ObjectUtil.isEmpty(id)) {
            throw new ModelDataLoaderException("Init data configuration error, 'id' not set: " + ellipsisJson(rowData));
        }
        return id;
    }

    @SneakyThrows
    private static String ellipsisJson(Object data) {
        int maxLength = 100;

        String str = OM.writeValueAsString(data);
        if (str.length() <= maxLength) {
            return str;
        } else {
            return str.substring(0, maxLength - 3) + "...";
        }
    }

    /**
     * 初始化寒暑假
     */
    private void initRowData(Map<String, Object> rowData, Map<String, List<Map<String, Object>>> initDataMap) {
        for (String key : rowData.keySet()) {
            Object value = rowData.get(key);
            if (value instanceof List) {
                List<Object> list = (List<Object>) value;
                for (int i = 0; i < list.size(); i++) {
                    Object value2 = list.get(i);
                    Object newValue = expression(initDataMap, value2);
                    if (newValue != null) {
                        list.set(i, newValue);
                    }
                }
            } else if (value instanceof String) {
                Object newValue = expression(initDataMap, value);
                if (newValue != null) {
                    rowData.put(key, newValue);
                }
            }
        }
    }

    private Object expression(Map<String, List<Map<String, Object>>> initDataMap, Object value) {
        if (value instanceof String) {
            String expression = (String) value;
            if (expression.startsWith("${") && expression.endsWith("}")) {
                expression = expression.substring(2, expression.length() - 1);
                return ognlGet("${", "}", expression, initDataMap);
            }
        }
        return null;
    }

    private static Object ognlGet(String openToken, String closeToken, String expression, Object object) {
        GenericTokenParser paramParser = new GenericTokenParser(openToken, closeToken, (m) -> m);
        return OgnlCache.getValue(paramParser.parse(expression), object);
    }

    public static class ModelDataLoaderException extends BaseException {

        public ModelDataLoaderException(String message, Throwable cause) {
            super(message, cause);
        }

        public ModelDataLoaderException(String message) {
            super(message);
        }
    }
}