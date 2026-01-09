package io.github.myacelw.mybatis.dynamic.core.service.impl;

import io.github.myacelw.mybatis.dynamic.core.metadata.Model;
import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.Command;
import io.github.myacelw.mybatis.dynamic.core.service.execution.Execution;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * 数据管理器实现类
 *
 * @author liuwei
 */
@Slf4j
public class DataManagerImpl<ID> implements DataManager<ID> {

    @Getter
    private final ModelContext modelContext;

    public DataManagerImpl(ModelContext modelContext) {
        this.modelContext = modelContext;
    }

    @Override
    public Model getModel() {
        return modelContext.getModel();
    }

    @Override
    public <R> R execCommand(@NonNull Command command) {
        Execution execution = Execution.getExecutions().get(command.getClass());
        if (execution == null) {
            throw new RuntimeException("没有找到命令类型" + command.getClass().getSimpleName() + "对应的命令执行器");
        }
        return (R) execution.exec(command, this);
    }

//    /**
//     * 递归查询字段路径对应的模型，并得到数据管理器
//     *
//     * @param fieldPath                        字段路径
//     * @param dataManagerGetter                数据管理器获取器
//     * @return 模型和数据管理器Map
//     */
//    private Map<String, DataManager<?>> getDataManagers(String fieldPath, DataManagerGetter dataManagerGetter, boolean throwException) {
//        int index = fieldPath.indexOf(".");
//        String f1;
//        String f2;
//        if (index > 0) {
//            f1 = fieldPath.substring(0, index);
//            f2 = fieldPath.substring(index + 1);
//        } else {
//            f1 = fieldPath;
//            f2 = null;
//        }
//        Field field = modelContext.getPermissionedField(f1);
//        if (field == null) {
//            if (throwException) {
//                throw new JoinFieldException("40039584", "没有找到join字段'" + fieldPath + "'");
//            } else {
//                return Collections.emptyMap();
//            }
//        }
//        if (!(field instanceof ToOneField || field instanceof ToManyField)) {
//            if (throwException) {
//                throw new JoinFieldException("40039584", "join字段'" + fieldPath + "'不是 Rel、RelTable、SubTable、ExtTable、RefRel类型");
//            } else {
//                return Collections.emptyMap();
//            }
//        }
//
//        Map<String, DataManager<?>> result = new HashMap<>();
//
//        String refModelName = ((RefModel) field).getTargetModel();
//        DataManagerImpl<?> dataManager = (DataManagerImpl<?>) dataManagerGetter.getDataManager(refModelName);
//        result.put(f1, dataManager);
//
//        if (f2 != null) {
//            Map<String, DataManager<?>> child = dataManager.getDataManagers(f2, dataManagerGetter, throwException);
//            child.forEach((k, v) -> result.put(f1 + "." + k, v)
//            );
//        }
//
//        return result;
//    }

}
