package io.github.myacelw.mybatis.dynamic.core.util;

import lombok.SneakyThrows;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

/**
 * 注解工具类
 *
 * @author liuwei
 */
public class AnnotationUtil {

    @SneakyThrows
    public static Object getValue(Annotation annotation, String attributeName) {
        Method method = annotation.annotationType().getDeclaredMethod(attributeName);
        return method.invoke(annotation);
    }

    public static <A extends Annotation> A getAnnotation(AnnotatedElement annotatedElement, Class<A> annotationType) {
        return annotatedElement.getAnnotation(annotationType);
    }

    public static <A extends Annotation> A[] getAnnotations(AnnotatedElement annotatedElement, Class<A> annotationType) {
        return annotatedElement.getAnnotationsByType(annotationType);
    }

}
