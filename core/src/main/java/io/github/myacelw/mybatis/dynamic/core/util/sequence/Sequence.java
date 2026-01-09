package io.github.myacelw.mybatis.dynamic.core.util.sequence;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * 雪花算法序列号生成接口。
 *
 * @author liuwei
 */
public interface Sequence {

    /**
     * 获取下一个 ID
     *
     * @return 下一个 ID
     */
    long nextId();


    /**
     * 提供一个静态方法来获取执行器的Map
     * 所有需要使用执行器的地方，都通过这个方法来获取
     * @return a map of command class to execution instance
     */
    static Sequence getInstance() {
        return SequenceHolder.SEQUENCE;
    }

    class SequenceHolder {
        private static final Sequence SEQUENCE = initialize();

        private static Sequence initialize() {
            ServiceLoader<Sequence> loader = ServiceLoader.load(Sequence.class, Sequence.class.getClassLoader());

            Iterator<Sequence> iterator = loader.iterator();
            if (iterator.hasNext()) {
                return iterator.next();
            } else {
                return new DefaultSequence();
            }
        }
    }

}
