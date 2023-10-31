package io.metersphere.cdc.domain.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.metersphere.base.domain.User;
import io.metersphere.cdc.domain.enums.TestTrainDeleteFlagEnum;
import io.metersphere.cdc.domain.enums.TestTrainUserStatusEnum;
import io.metersphere.cdc.domain.enums.TestTrainUserTypeEnum;

import java.io.Serializable;

import lombok.Data;

@Data
public class TestTrainUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @JsonAlias("user_id")
    private Long userId;

    /**
     * 部门ID
     */
    @JsonAlias("dept_id")
    private Long deptId;

    /**
     * 用户账号
     */
    @JsonAlias("user_name")
    private String userName;

    /**
     * 用户昵称
     */
    @JsonAlias("nick_name")
    private String nickName;

    @JsonAlias("user_type")
    private String userTypeStr;

    @JsonIgnore
    private TestTrainUserTypeEnum userType;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 手机号码
     */
    @JsonProperty("phonenumber")
    private String phoneNumber;

    /**
     * 用户性别
     */
    private String sex;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 密码
     */
    private String password;

    /**
     * 帐号状态（0正常 1停用）
     */
    @JsonAlias("status")
    private String userStatusStr;

    @JsonIgnore
    private TestTrainUserStatusEnum userStatus;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    @JsonAlias("del_flag")
    private String userDeleteFlagStr;

    @JsonIgnore
    private TestTrainDeleteFlagEnum userDeleteFlag;

    @JsonIgnore
    private transient User msUser;

}
