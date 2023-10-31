package io.metersphere.cdc.domain.enums;


/**
 * @author Punkhoo
 */
public enum TestTrainDeleteFlagEnum {

    EXIST("0", "存在"),

    DELETE("2", "删除"),
    ;

    private final String code;

    private final String value;

    TestTrainDeleteFlagEnum(String code, String value) {
        this.code = code;
        this.value = value;
    }

    public String getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    public static TestTrainDeleteFlagEnum getByCode(String code) {
        for (var value : TestTrainDeleteFlagEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }

        return null;
    }

}
