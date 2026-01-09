package io.github.myacelw.mybatis.dynamic.core.service.chain;

import io.github.myacelw.mybatis.dynamic.core.service.DataManager;
import io.github.myacelw.mybatis.dynamic.core.service.command.Command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 命令处理链
 *
 * @param <R> 返回值类型
 * @param <C> 命令类型
 * @param <B> 链式调用类型

 * @author liuwei
 */
public abstract class AbstractChain<ID, R, C extends Command, B extends AbstractChain<ID, R, C, B>> implements Chain<R> {

    protected final DataManager<ID> dataManager;

    protected final C command;

    public AbstractChain(DataManager<ID> dataManager, Supplier<C> createCommand) {
        this.dataManager = dataManager;
        this.command = createCommand.get();
    }

    protected final C build() {
        return command;
    }

    protected final B self() {
        return (B) this;
    }

    @Override
    public final R exec() {
        return dataManager.execCommand(build());
    }

    /**
     * 集合中放入新元素
     */
    @SafeVarargs
    public static <T> void add(Supplier<List<T>> getter, Consumer<List<T>> setter, T... data) {
        if (data != null) {
            List<T> list = getList(getter, setter);
            Collections.addAll(list, data);
        }
    }

    public static <T> void add(Supplier<List<T>> getter, Consumer<List<T>> setter, T data) {
        if (data != null) {
            List<T> list = getList(getter, setter);
            list.add(data);
        }
    }

    public static <T> void add(Supplier<List<T>> getter, Consumer<List<T>> setter, Collection<T> data) {
        if (data != null) {
            List<T> list = getList(getter, setter);
            list.addAll(data);
        }
    }

    public static <T> List<T> getList(Supplier<List<T>> getter, Consumer<List<T>> setter) {
        List<T> list = getter.get();
        if (list == null) {
            list = new ArrayList<>();
            setter.accept(list);
        }
        return list;
    }

}
