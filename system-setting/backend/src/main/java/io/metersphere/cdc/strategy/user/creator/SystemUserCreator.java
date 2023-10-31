package io.metersphere.cdc.strategy.user.creator;

import io.metersphere.cdc.domain.enums.TestTrainUserTypeEnum;
import io.metersphere.cdc.domain.model.TestTrainUser;
import io.metersphere.cdc.strategy.user.base.AbstractUserCreator;
import io.metersphere.commons.constants.UserConstants;
import io.metersphere.service.UserService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SystemUserCreator extends AbstractUserCreator {

    @Autowired
    private UserService userService;

    @Override
    public boolean support(TestTrainUser testTrainUser) {
        if (super.validate(testTrainUser)) {
            return testTrainUser.getUserType().equals(TestTrainUserTypeEnum.SYSTEM);
        } else {
            log.warn("User[{}] is invalid", testTrainUser.getUserName());
        }

        return false;
    }

    @Override
    public void handle(TestTrainUser testTrainUser) {
        var userRequest = testTrainUser2UserRequest(testTrainUser, UserConstants.SYSTEM_USER_GROUP_IDS);
        log.info("SystemUserCreator userRequest: {}", userRequest);
        userService.insert(userRequest);
    }

}
