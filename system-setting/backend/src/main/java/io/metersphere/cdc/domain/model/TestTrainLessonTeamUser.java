package io.metersphere.cdc.domain.model;

import com.fasterxml.jackson.annotation.JsonAlias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

import lombok.Data;
import lombok.experimental.Accessors;

@ApiModel("小组用户对象")
@Accessors(chain = true)
@Data
public class TestTrainLessonTeamUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("关系id")
    private Long id;

    @ApiModelProperty("用户id")
    @JsonAlias("user_id")
    private Long userId;

    @JsonAlias("user_name")
    private String userName;

    private String nickName;

    @ApiModelProperty("是否是组长")
    private Boolean leader;

    @ApiModelProperty("小组id")
    @JsonAlias("team_id")
    private Long teamId;

    @ApiModelProperty("项目id")
    @JsonAlias("project_id")
    private Long projectId;

    @ApiModelProperty("项目名称")
    private String projectName;

    @ApiModelProperty("所属课程id")
    private Long lessonId;

    @ApiModelProperty("小组名称")
    private String teamName;

    @ApiModelProperty("版本")
    private Long version;

    @ApiModelProperty("删除标志（0代表存在 2代表删除）")
    private String delFlag;

}
