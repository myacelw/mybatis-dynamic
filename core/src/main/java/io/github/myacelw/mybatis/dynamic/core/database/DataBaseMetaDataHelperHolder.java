package io.github.myacelw.mybatis.dynamic.core.database;

import lombok.Getter;
import lombok.Setter;

/**
 * DataBaseMetaDataHelper 持有者，用于在没有注入的地方获取 DataBaseMetaDataHelper
 *
 * @author liuwei
 */
public class DataBaseMetaDataHelperHolder {

    @Getter
    @Setter
    private static DataBaseMetaDataHelper metaDataHelper;

}
