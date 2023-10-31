package io.metersphere.outter.domain.enums;


public enum ResultEnum {

    SUCCESS(200, "接口调用成功") {

    },
    FAILED(500, "接口调用失败") {

    },
    FORBIDDEN(403, "没有权限访问资源") {

    };

    private final Integer code;

    private final String message;

    ResultEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
