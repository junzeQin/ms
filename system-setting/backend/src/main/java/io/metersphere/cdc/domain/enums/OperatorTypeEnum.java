package io.metersphere.cdc.domain.enums;

public enum OperatorTypeEnum {

    r("读取"),

    c("创建"),

    u("更新"),

    d("删除"),
    ;

    private final String desc;

    OperatorTypeEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

}
