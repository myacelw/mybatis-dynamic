package io.github.myacelw.mybatis.dynamic.draw;

import io.github.myacelw.mybatis.dynamic.draw.controller.DrawController;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 配置类
 *
 * @author liuwei
 */
@Configuration
@Import(DrawController.class)
public class DynamicModelDrawAutoConfiguration {


}

