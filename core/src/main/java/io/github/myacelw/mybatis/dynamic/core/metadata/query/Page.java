package io.github.myacelw.mybatis.dynamic.core.metadata.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页查询
 *
 * @author liuwei
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Page {

    /**
     * 查询页号
     */
    int current = 1;

    /**
     * 每页条数
     */
    int size = 10;

}
