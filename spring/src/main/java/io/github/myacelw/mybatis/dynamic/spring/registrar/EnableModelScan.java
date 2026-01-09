package io.github.myacelw.mybatis.dynamic.spring.registrar;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 启动模型扫描，发现的模型将自动注册该模型的 BaseDao 和 BaseService。
 * 可以配置spring上下文参数：<br>
 * mybatis-dynamic.update-model: true/false # 是否调用根据模型更新数据库方法，默认为false。 <br>
 * mybatis-dynamic.init-data-files: # 初始化数据文件路径列表，例如 classpath:data-v1.json,classpath:data-v2.json
 *
 * @author liuwei
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({ModelEntityDaoAndServiceRegistrar.class})
public @interface EnableModelScan {

    String UPDATE_MODEL_KEY = "mybatis-dynamic.update-model";

    /**
     * 扫描的包路径
     */
    String[] value() default {};

    /**
     * 扫描的包路径
     */
    String[] basePackages() default {};

    /**
     * 是否自动注册Service Bean
     */
    boolean autoServiceBean() default true;

}