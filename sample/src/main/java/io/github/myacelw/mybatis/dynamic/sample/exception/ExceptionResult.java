package io.github.myacelw.mybatis.dynamic.sample.exception;

import lombok.Value;

/**
 * 异常结果
 *
 * @author liuwei
 */
@Value
public class ExceptionResult {
    String message;
    String errorType;
}