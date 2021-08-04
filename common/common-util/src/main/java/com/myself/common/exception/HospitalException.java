package com.myself.common.exception;

import com.myself.common.result.ResultCodeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 自定义全局异常类
 *
 * @author Wei
 * @since 2021/6/19
 */

@Data
@ApiModel(value = "自定义全局异常类")
public class HospitalException extends RuntimeException {

    @ApiModelProperty(value = "异常状态码")
    private Integer code;

    //通过状态码和错误信息创建异常对象
    public HospitalException(String message, Integer code) {
        super(message);
        this.code = code;
    }

    //通过枚举类型创建异常对象
    public HospitalException(ResultCodeEnum resultCodeEnum){
        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();
    }

    public String toString() {
        return "HospitalException{" +
                "code=" + code +
                ", message" + this.getMessage() +
                '}';
    }
}
