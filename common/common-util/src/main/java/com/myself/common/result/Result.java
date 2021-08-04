package com.myself.common.result;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 全局统一返回结果类
 *
 * @author Wei
 * @since 2021/6/17
 */
@Data
@ApiModel(value = "全局统一返回结果")
public class Result<T> {

    @ApiModelProperty(value = "返回码")
    private Integer code;

    @ApiModelProperty(value = "返回消息")
    private String message;

    @ApiModelProperty(value = "返回数据")
    private T data;

    public Result() {
    }

    //操作成功
    public static <T> Result<T> ok() {
        return Result.build(null, ResultCodeEnum.SUCCESS);
    }

    public static <T> Result<T> ok(T data) {
        return build(data, ResultCodeEnum.SUCCESS);
    }

    //操作失败
    public static <T> Result<T> fail() {
        return Result.build(null, ResultCodeEnum.FAIL);
    }

    public static <T> Result<T> fail(T data) {
        return build(data, ResultCodeEnum.FAIL);
    }

    //返回消息
    public Result<T> message(String msg) {
        this.setMessage(msg);
        return this;
    }

    //返回码
    public Result<T> code(Integer code) {
        this.setCode(code);
        return this;
    }

    //构建对象
    public static <T> Result<T> build(T data, ResultCodeEnum resultCodeEnum) {
        Result<T> result = new Result<>();
        if (data != null){
            result.setData(data);
        }
        result.setCode(resultCodeEnum.getCode());
        result.setMessage(resultCodeEnum.getMessage());
        return result;
    }

}

