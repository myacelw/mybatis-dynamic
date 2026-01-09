package io.github.myacelw.mybatis.dynamic.core.service.chain;

/**
 * 命令处理链
 *
 * @author liuwei
 */
public interface Chain<R> {
    R exec();
}
