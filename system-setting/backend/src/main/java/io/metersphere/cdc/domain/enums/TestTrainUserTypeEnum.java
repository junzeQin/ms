package io.metersphere.cdc.domain.enums;


/**
 * @author Punkhoo
 */
public enum TestTrainUserTypeEnum {

    SYSTEM("1", "系统用户"),

    TEACHER("3", "老师"),

    TEACHING_ASSISTANT("4", "助教"),

    EDUCATIONAL_ADMINISTRATOR("2", "教务"),

    STUDENT("5", "学生"),
    ;

    private final String code;

    private final String value;

    TestTrainUserTypeEnum(String code, String value) {
        this.code = code;
        this.value = value;
    }

    public String getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    public static TestTrainUserTypeEnum getByCode(String code) {
        for (var value : TestTrainUserTypeEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }

        return null;
    }

}
