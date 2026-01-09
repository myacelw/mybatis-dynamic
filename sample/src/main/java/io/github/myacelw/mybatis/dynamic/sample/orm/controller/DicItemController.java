package io.github.myacelw.mybatis.dynamic.sample.orm.controller;

import io.github.myacelw.mybatis.dynamic.sample.orm.entity.DicItem;
import io.github.myacelw.mybatis.dynamic.spring.controller.AbstractController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 字典项Controller
 *
 * @author liuwei
 */
@RestController
@RequestMapping("/dicItem/")
public class DicItemController extends AbstractController<Integer, DicItem> {

}
