package io.metersphere.cdc.domain.enums;


/**
 * @author Punkhoo
 */
public enum TestTrainUserStatusEnum {

    ACTIVATE("0", "正常"),

    DEACTIVATE("1", "停用"),
    ;

    private final String code;

    private final String value;

    TestTrainUserStatusEnum(String code, String value) {
        this.code = code;
        this.value = value;
    }

    public String getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    public static TestTrainUserStatusEnum getByCode(String code) {
        for (var value : TestTrainUserStatusEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }

        return null;
    }

}
