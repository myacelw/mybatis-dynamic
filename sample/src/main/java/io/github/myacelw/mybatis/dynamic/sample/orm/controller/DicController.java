package io.github.myacelw.mybatis.dynamic.sample.orm.controller;

import io.github.myacelw.mybatis.dynamic.sample.mybatis.SqlExecListener;
import io.github.myacelw.mybatis.dynamic.sample.orm.entity.Dic;
import io.github.myacelw.mybatis.dynamic.sample.orm.entity.enums.Status;
import io.github.myacelw.mybatis.dynamic.spring.controller.AbstractController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 字典Controller
 *
 * @author liuwei
 */
@Slf4j
@RestController
@RequestMapping("/dic/")
@RequiredArgsConstructor
public class DicController extends AbstractController<Integer, Dic> implements SqlExecListener {

    @GetMapping("/list")
    public List<Dic> list() {
        try (SqlExecListener listener = SqlExecListener.listen(this)) {
            return service.list();
        }
    }

    @GetMapping("/test")
    public Dic getDic() {
        Dic dic = new Dic();
        dic.setStatus(Status.Valid);
        return dic;
    }

    @Override
    public void onBeforeExecute(String sql, List<Object> parameterValues) {
        log.info("onBeforeExecute: {}, {}", sql, parameterValues);
    }
}
