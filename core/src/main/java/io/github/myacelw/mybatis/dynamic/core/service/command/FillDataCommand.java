package io.github.myacelw.mybatis.dynamic.core.service.command;

import io.github.myacelw.mybatis.dynamic.core.metadata.query.Join;
import lombok.*;

import java.util.List;

/**
 * 填充数据命令
 *
 * @author liuwei
 */
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@NoArgsConstructor
public class FillDataCommand implements Command {

    /**
     * 待填充的数据
     */
    @NonNull
    List data;

    /**
     * 需要填充的关联或关联表字段
     */
    List<FillField> fillFields;


    @Data
    @AllArgsConstructor
    @RequiredArgsConstructor
    @NoArgsConstructor
    public static class FillField {
        /**
         * 需要填充的关联或关联表字段名
         */
        @NonNull
        String fieldName;

        /**
         * 关联或关联表查询进行的Join
         */
        List<Join> joins;

        /**
         * 关联或关联表查询的返回字段
         */
        List<String> selectFields;
    }

}
