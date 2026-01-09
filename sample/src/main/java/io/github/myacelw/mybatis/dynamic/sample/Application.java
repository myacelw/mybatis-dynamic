package io.github.myacelw.mybatis.dynamic.sample;


import io.github.myacelw.mybatis.dynamic.spring.hook.CurrentUserHolder;
import io.github.myacelw.mybatis.dynamic.spring.registrar.EnableModelScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * 程序入口
 *
 * @author liuwei
 */
@SpringBootApplication
@EnableModelScan
public class Application {

    @Bean
    public CurrentUserHolder currentUserHolder() {
        return () -> "admin";
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
