package io.github.myacelw.mybatis.dynamic.spring.filler;

import io.github.myacelw.mybatis.dynamic.core.service.filler.AbstractCreatorFiller;
import io.github.myacelw.mybatis.dynamic.spring.hook.CurrentUserHolder;

/**
 * 创建人填充器
 *
 * @author liuwei
 */
public class CreatorFiller extends AbstractCreatorFiller {

    private final CurrentUserHolder currentUserHolder;

    public CreatorFiller(CurrentUserHolder currentUserHolder) {
        this.currentUserHolder = currentUserHolder;
    }

    @Override
    protected String getCurrentUser() {
        return currentUserHolder.getCurrentUserId();
    }
}
