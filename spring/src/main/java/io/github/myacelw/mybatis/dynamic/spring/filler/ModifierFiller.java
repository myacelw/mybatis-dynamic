package io.github.myacelw.mybatis.dynamic.spring.filler;

import io.github.myacelw.mybatis.dynamic.core.service.filler.AbstractModifierFiller;
import io.github.myacelw.mybatis.dynamic.spring.hook.CurrentUserHolder;

/**
 * 修改人填充器
 *
 * @author liuwei
 */
public class ModifierFiller extends AbstractModifierFiller {

    private final CurrentUserHolder currentUserHolder;

    public ModifierFiller(CurrentUserHolder currentUserHolder) {
        this.currentUserHolder = currentUserHolder;
    }

    @Override
    protected String getCurrentUser() {
        return currentUserHolder.getCurrentUserId();
    }
}
