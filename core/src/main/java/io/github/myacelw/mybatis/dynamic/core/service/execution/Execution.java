package io.github.myacelw.mybatis.dynamic.core.service.execution;

import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.Command;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 命令执行器接口
 *
 * @param <R>
 */
public interface Execution<ID, R, C extends Command> {

    /**
     * 得到可以处理的命令类
     */
    Class<? extends Command> getCommandClass();

    R exec(C command, DataManager<ID> dataManager);

    default int getPriority() {
        return 100;
    }

    /**
     * 提供一个静态方法来获取执行器的Map
     * 所有需要使用执行器的地方，都通过这个方法来获取
     * @return a map of command class to execution instance
     */
    static Map<Class<?>, Execution> getExecutions() {
        return ExecutionHolder.EXECUTIONS;
    }

    /**
     * 使用一个私有的静态内部类来持有实例。
     * JVM保证，只有在ExecutionHolder类第一次被访问时（即getExecutions()第一次被调用时），
     * 它内部的静态字段才会被初始化。
     * 这就完美地实现了延迟加载。
     */
    @Slf4j
    class ExecutionHolder {
        private static final Map<Class<?>, Execution> EXECUTIONS = initializeExecutions();

        private static Map<Class<?>, Execution> initializeExecutions() {
            // 明确使用线程上下文类加载器，这在多模块和复杂环境中是最佳实践
            ServiceLoader<Execution> loader = ServiceLoader.load(Execution.class, Execution.class.getClassLoader());

            Map<Class<?>, Execution> result = new HashMap<>();

            List<Execution> list = new ArrayList<>();
            loader.forEach(list::add);

            // 检查是否加载成功
            if (list.isEmpty()) {
                // 使用日志框架打印警告，而不是System.out
                log.warn("ServiceLoader found no implementations for Execution interface. Check META-INF/services configuration.");
                log.warn("The ClassLoader used was: {}", Thread.currentThread().getContextClassLoader().getClass().getName());

                throw new RuntimeException("No Execution implementations found. Please check your META-INF/services configuration.");
            }

            list.stream()
                    .sorted(Comparator.comparingInt(Execution::getPriority))
                    .forEach(execution -> result.put(execution.getCommandClass(), execution));

            log.debug("########### Command Executioner List Initialized ########### Command Executioner List Initialized ");
            result.forEach((k, v) -> log.debug("Command: {}, Execution class: {}, Priority: {}", k.getSimpleName(), v.getClass().getName(), v.getPriority()));

            // 可以返回一个不可修改的Map，更安全
            return java.util.Collections.unmodifiableMap(result);
        }
    }


}
