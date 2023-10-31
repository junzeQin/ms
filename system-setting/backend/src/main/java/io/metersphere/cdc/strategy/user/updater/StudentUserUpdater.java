package io.metersphere.cdc.strategy.user.updater;

import io.metersphere.cdc.domain.enums.TestTrainUserTypeEnum;
import io.metersphere.cdc.domain.model.TestTrainUser;
import io.metersphere.cdc.strategy.user.base.AbstractUserUpdater;
import io.metersphere.commons.constants.UserConstants;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StudentUserUpdater extends AbstractUserUpdater {

    @Override
    public boolean support(TestTrainUser testTrainUser) {
        if (super.validate(testTrainUser)) {
            return testTrainUser.getUserType().equals(TestTrainUserTypeEnum.STUDENT);
        } else {
            log.warn("User[{}] is invalid", testTrainUser.getUserName());
        }

        return false;
    }

    @Override
    public void handle(TestTrainUser testTrainUser) {
        super.updateUser(testTrainUser, UserConstants.STUDENT_USER_GROUP_IDS);
    }

}
