package io.github.myacelw.mybatis.dynamic.core.metadata.field;

/**
 * 关联到模型接口
 *
 * @author liuwei
 */
public interface RefModel extends Field{

    String getTargetModel();

    void setTargetModel(String targetModel);

}
