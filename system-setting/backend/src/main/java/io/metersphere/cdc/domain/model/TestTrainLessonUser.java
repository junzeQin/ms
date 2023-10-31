package io.metersphere.cdc.domain.model;

import com.fasterxml.jackson.annotation.JsonAlias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

import lombok.Data;

@ApiModel("成员关系对象")
@Data
public class TestTrainLessonUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 流水id
     */
    @ApiModelProperty("主键流水号id")
    private Long id;

    /**
     * 课程id
     */
    @JsonAlias("lesson_id")
    private Long lessonId;

    private Long role;

    /**
     * 用户类型 3：老师 4：助教 5：学生 同roleId
     */
    private String userType;

    /**
     * 用户ID
     */
    @JsonAlias("user_id")
    private Long userId;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 用户账号
     */
    @JsonAlias("user_name")
    private String userName;

    /**
     * 年级
     */
    private String grade;

    /**
     * 学生姓名
     */
    private String nickName;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 手机号码
     */
    private String phonenumber;

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
     * 盐加密
     */
    private String salt;

    /**
     * 帐号状态（0正常 1停用）
     */
    private String status;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    private String delFlag;

    /**
     * 最后登录IP
     */
    private String loginIp;

    /**
     * 专业
     */
    private String major;

}
