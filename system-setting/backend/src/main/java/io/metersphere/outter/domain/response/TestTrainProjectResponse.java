package io.metersphere.outter.domain.response;

import io.metersphere.cdc.domain.model.TestTrainProject;
import io.swagger.annotations.ApiModel;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@ApiModel("项目返回对象")
public class TestTrainProjectResponse extends TestTrainProject {

    private static final long serialVersionUID = 1L;

}
