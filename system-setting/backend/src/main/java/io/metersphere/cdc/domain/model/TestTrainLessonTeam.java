package io.metersphere.cdc.domain.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;

import io.metersphere.cdc.domain.enums.TestTrainDeleteFlagEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 学生小组对象 train_team
 */
@ApiModel("班课小组对象")
@Accessors(chain = true)
@Data
public class TestTrainLessonTeam implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 小组id
     */
    @ApiModelProperty("小组id")
    @JsonAlias("team_id")
    private Long teamId;

    @ApiModelProperty("项目id")
    @JsonAlias("project_id")
    private Long projectId;

    @ApiModelProperty("所属课程id")
    @JsonAlias("lesson_id")
    private Long lessonId;

    /**
     * 小组名称
     */
    @ApiModelProperty("小组名称")
    @JsonAlias("team_name")
    private String teamName;

    /**
     * 版本
     */
    @ApiModelProperty("版本")
    private Long version;

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
