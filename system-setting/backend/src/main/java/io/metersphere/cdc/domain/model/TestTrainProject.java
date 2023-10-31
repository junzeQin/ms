package io.metersphere.cdc.domain.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;

import io.metersphere.cdc.domain.enums.TestTrainDeleteFlagEnum;
import io.swagger.annotations.ApiModel;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 项目对象 train_project
 *
 * @author neuedu
 * @date 2023-04-04
 */
@ApiModel("项目对象")
@Accessors(chain = true)
@Data
public class TestTrainProject implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 项目id
     */
    @JsonAlias("project_id")
    private Long projectId;

    /**
     * 父级id
     */
    @JsonAlias("parent_id")
    private Long parentId;

    /**
     * 课程id
     */
    @JsonAlias("lesson_id")
    private Long lessonId;

    /**
     * 项目名称
     */
    @JsonAlias("project_name")
    private String projectName;

    /**
     * 节点等级
     */
    @JsonAlias("project_level")
    private Integer projectLevel;

    /**
     * 项目来源
     */
    @JsonAlias("project_source")
    private String projectSource;

    /**
     * 祖级列表
     */
    @JsonAlias("project_ancestors")
    private String projectAncestors;

    /**
     * 项目序号
     */
    @JsonAlias("project_sort")
    private Integer projectSort;

    @JsonAlias("project_description")
    private String projectDescription;

    /**
     * 学时数
     */
    @JsonAlias("teaching_hour")
    private BigDecimal teachingHour;

    /**
     * 授课模式，字典表类型teaching_mode
     */
    @JsonAlias("teaching_mode")
    private String teachingMode;

    /**
     * 技术栈，字典编码technology_stack
     */
    @JsonAlias("technology_stack")
    private String technologyStack;

    /**
     * 状态：(0：未发布1：已发布）
     */
    private String status;

    /**
     * 锁
     */
    private Integer version;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    @JsonAlias("del_flag")
    private String userDeleteFlagStr;

    @JsonAlias("create_by")
    private String createBy;

    @JsonIgnore
    private TestTrainDeleteFlagEnum userDeleteFlag;

}
