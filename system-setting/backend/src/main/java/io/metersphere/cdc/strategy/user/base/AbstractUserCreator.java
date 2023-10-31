package io.metersphere.cdc.strategy.user.base;

import io.metersphere.base.mapper.GroupMapper;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.base.mapper.WorkspaceMapper;
import io.metersphere.cdc.domain.enums.TestTrainDeleteFlagEnum;
import io.metersphere.cdc.domain.enums.TestTrainUserStatusEnum;
import io.metersphere.cdc.domain.model.TestTrainUser;
import io.metersphere.request.member.UserRequest;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public abstract class AbstractUserCreator extends AbstractUserOperator {

    @Autowired
    private WorkspaceMapper workspaceMapper;

    @Autowired
    private GroupMapper groupMapper;

    @Autowired
    private ProjectMapper projectMapper;

    protected boolean validate(TestTrainUser testTrainUser) {
        if (testTrainUser.getUserStatus().equals(TestTrainUserStatusEnum.DEACTIVATE)) {
            return false;
        }
        if (testTrainUser.getUserDeleteFlag().equals(TestTrainDeleteFlagEnum.DELETE)) {
            return false;
        }

        return true;
    }

    protected UserRequest testTrainUser2UserRequest(TestTrainUser testTrainUser, String... userGroupIds) {
        var userRequest = super.testTrainUser2UserRequest(testTrainUser);
        userRequest.setGroups(super.mapperUserGroups(userGroupIds));

        return userRequest;
    }

}
