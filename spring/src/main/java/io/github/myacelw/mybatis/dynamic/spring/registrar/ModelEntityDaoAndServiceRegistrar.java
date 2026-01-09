package io.github.myacelw.mybatis.dynamic.spring.registrar;

import io.github.myacelw.mybatis.dynamic.core.annotation.Model;
import io.github.myacelw.mybatis.dynamic.core.service.impl.Class2ModelTransferImpl;
import io.github.myacelw.mybatis.dynamic.spring.dao.BaseDao;
import io.github.myacelw.mybatis.dynamic.spring.dao.BaseTreeDao;
import io.github.myacelw.mybatis.dynamic.spring.dao.BaseTreeDaoImpl;
import io.github.myacelw.mybatis.dynamic.spring.service.BaseService;
import io.github.myacelw.mybatis.dynamic.spring.service.BaseTreeService;
import io.github.myacelw.mybatis.dynamic.spring.service.BaseTreeServiceImpl;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 模型实体DAO和Service注册器
 *
 * @author liuwei
 */
@Setter
public class ModelEntityDaoAndServiceRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware {

    private ResourceLoader resourceLoader;

    private Environment environment;

    @Override
    @SneakyThrows
    public void registerBeanDefinitions(AnnotationMetadata metadata, @NonNull BeanDefinitionRegistry registry) {
        // 创建scanner
        ClassPathScanningCandidateComponentProvider scanner = getScanner();

        // 获取到@EnableModelScan注解所有属性
        Map<String, Object> attributes = metadata.getAnnotationAttributes(EnableModelScan.class.getCanonicalName());

        // 是否自动注册Service Bean
        boolean autoServiceBean = attributes.get("autoServiceBean") == null || (boolean) attributes.get("autoServiceBean");

        // 获取指定要扫描的basePackages
        Set<String> basePackages = getBasePackages(metadata, attributes);

        // 遍历每一个basePackages
        for (String basePackage : basePackages) {
            // 通过scanner获取basePackage下的候选类(有标@Model注解的类)
            Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents(basePackage);
            // 遍历每一个候选类，如果符合条件就把他们注册到容器
            for (BeanDefinition candidateComponent : candidateComponents) {
                registerBeans(autoServiceBean, registry, candidateComponent.getBeanClassName());
            }
        }
    }

    @SneakyThrows
    private void registerBeans(boolean autoServiceBean, BeanDefinitionRegistry registry, String entityClassName) {
        Class<?> entityClass = Class.forName(entityClassName);

        // 获取实体类的ID字段信息
        List<Class2ModelTransferImpl.IdFieldInfo> idFieldInfos = Class2ModelTransferImpl.getIdFieldInfos(entityClass);

        Class<?> idClass = null;
        if (idFieldInfos.size() == 1) {
            idClass = idFieldInfos.get(0).getPropertyType();
        }

        // 生成 DAO 和 Service 的类型
        ResolvableType daoType = ResolvableType.forClassWithGenerics(BaseDao.class, idClass, entityClass);
        ResolvableType treeDaoType = ResolvableType.forClassWithGenerics(BaseTreeDao.class, idClass, entityClass);
        ResolvableType serviceType = ResolvableType.forClassWithGenerics(BaseService.class, idClass, entityClass);
        ResolvableType treeServiceType = ResolvableType.forClassWithGenerics(BaseTreeService.class, idClass, entityClass);

        // 检查容器中是否已经有相同类型的 DAO 和 Service
        if (!isBeanTypeRegistered(registry, entityClass, daoType)) {
            registerDaoBean(registry, entityClass, treeDaoType, BaseTreeDaoImpl.class);
        }

        if (autoServiceBean && !isBeanTypeRegistered(registry, entityClass, serviceType)) {
            registerServiceBean(registry, entityClass, treeServiceType, BaseTreeServiceImpl.class);
        }
    }

    private void registerDaoBean(BeanDefinitionRegistry registry, Class<?> entityClass, ResolvableType type, Class<?> implClass) {
        Constructor<?> constructor = implClass.getConstructors()[0];
        String beanName = entityClass.getName() + "DaoImpl";
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(implClass);
        builder.applyCustomizers(bd -> ((RootBeanDefinition) bd).setTargetType(type));
        builder.addConstructorArgValue(entityClass);
        builder.addConstructorArgValue(new DependencyDescriptor(new MethodParameter(constructor, 1), true));
        builder.addConstructorArgValue(new DependencyDescriptor(new MethodParameter(constructor, 2), true));
        builder.addConstructorArgValue("${" + EnableModelScan.UPDATE_MODEL_KEY + ":false}");

        registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
    }

    private void registerServiceBean(BeanDefinitionRegistry registry, Class<?> entityClass, ResolvableType type, Class<?> implClass) {
        String serviceBeanName = entityClass.getName() + "ServiceImpl";
        String daoBeanName = entityClass.getName() + "DaoImpl";

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(implClass);
        builder.applyCustomizers(bd -> ((RootBeanDefinition) bd).setTargetType(type));
        builder.addConstructorArgReference(daoBeanName);
        registry.registerBeanDefinition(serviceBeanName, builder.getBeanDefinition());
    }

    /**
     * 检查是否已经注册了相同类型的 Bean
     */
    @SneakyThrows
    private boolean isBeanTypeRegistered(BeanDefinitionRegistry registry, Class<?> entityClass, ResolvableType targetType) {
        String packageName = Arrays.stream(entityClass.getName().split("\\.")).limit(2).collect(Collectors.joining("."));

        String[] beanNames = registry.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
            if (beanDefinition.getBeanClassName() != null && beanDefinition.getBeanClassName().startsWith(packageName)) {
                ResolvableType beanType = ResolvableType.forClass(Class.forName(beanDefinition.getBeanClassName()));
                if (targetType.isAssignableFrom(beanType)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 创建扫描器
     */
    protected ClassPathScanningCandidateComponentProvider getScanner() {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false, environment) {
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                return true;
            }
        };
        scanner.setResourceLoader(resourceLoader);

        // 设置扫描器scanner扫描的过滤条件
        AnnotationTypeFilter annotationTypeFilter = new AnnotationTypeFilter(Model.class);
        scanner.addIncludeFilter(annotationTypeFilter);
        return scanner;
    }

    /**
     * 获取base packages
     */
    protected static Set<String> getBasePackages(AnnotationMetadata importingClassMetadata, Map<String, Object> attributes) {
        Set<String> basePackages = new HashSet<>();
        if (attributes != null) {
            // value 属性是否有配置值，如果有则添加
            for (String pkg : (String[]) attributes.get("value")) {
                if (StringUtils.hasText(pkg)) {
                    basePackages.add(pkg);
                }
            }

            // basePackages 属性是否有配置值，如果有则添加
            for (String pkg : (String[]) attributes.get("basePackages")) {
                if (StringUtils.hasText(pkg)) {
                    basePackages.add(pkg);
                }
            }
        }

        // 如果上面两步都没有获取到basePackages，那么这里就默认使用当前项目启动类所在的包为basePackages
        if (basePackages.isEmpty()) {
            basePackages.add(ClassUtils.getPackageName(importingClassMetadata.getClassName()));
        }

        return basePackages;
    }
}