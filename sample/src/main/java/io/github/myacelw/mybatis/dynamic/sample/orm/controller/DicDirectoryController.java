package io.github.myacelw.mybatis.dynamic.sample.orm.controller;

import io.github.myacelw.mybatis.dynamic.sample.orm.entity.DicDirectory;
import io.github.myacelw.mybatis.dynamic.spring.controller.AbstractTreeController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 字典目录
 *
 * @author liuwei
 */
@RestController
@RequestMapping("/dicDirectory/")
public class DicDirectoryController extends AbstractTreeController<Integer, DicDirectory> {

}
