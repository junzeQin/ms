package io.metersphere.cdc.domain.model;

import com.fasterxml.jackson.annotation.JsonAlias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

import lombok.Data;

@ApiModel("课程对象")
@Data
public class TestTrainLesson implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 课程id
     */
    @ApiModelProperty("课程id")
    @JsonAlias("lesson_id")
    private Long lessonId;

    /**
     * 课程名称
     */
    @ApiModelProperty("课程名称")
    @JsonAlias("lesson_name")
    private String lessonName;

    /**
     * 教学班名称
     */
    @ApiModelProperty("教学班名称")
    @JsonAlias("class_name")
    private String className;

    /**
     * 任课教师
     */
    @ApiModelProperty("任课教师id")
    @JsonAlias("user_id")
    private Long userId;

    /**
     * 学年
     */
    @ApiModelProperty("学年")
    @JsonAlias("school_year")
    private Long schoolYear;

    /**
     * 学期
     */
    @ApiModelProperty("学期")
    private Long term;

    /**
     * 状态：(0：未发布1：已发布）
     */
    @ApiModelProperty("状态：(0：未发布1：已发布）")
    private String status;

    @ApiModelProperty("版本")
    private Long version;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    @ApiModelProperty("删除标志（0代表存在 2代表删除）")
    @JsonAlias("del_flag")
    private String delFlag;

    /**
     * 序号
     */
    @ApiModelProperty("序号")
    private int sort;

}
