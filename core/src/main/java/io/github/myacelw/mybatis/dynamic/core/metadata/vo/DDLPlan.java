package io.github.myacelw.mybatis.dynamic.core.metadata.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DDL 执行计划
 *
 * @author liuwei
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DDLPlan {

    private List<Sql> sqlList = new ArrayList<>();

    public void addSql(Sql sql) {
        if (sql != null) {
            sqlList.add(sql);
        }
    }

    public void addAll(List<Sql> sqls) {
        if (sqls != null) {
            sqlList.addAll(sqls);
        }
    }

    public boolean isEmpty() {
        return sqlList == null || sqlList.isEmpty();
    }
}
