package io.metersphere.cdc.strategy.user.updater;

import io.metersphere.cdc.domain.enums.TestTrainUserTypeEnum;
import io.metersphere.cdc.domain.model.TestTrainUser;
import io.metersphere.cdc.strategy.user.base.AbstractUserUpdater;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EducationalAdministratorUserUpdater extends AbstractUserUpdater {

    @Autowired
    private TeacherUserUpdater teacherUserUpdater;

    @Override
    public boolean support(TestTrainUser testTrainUser) {
        if (super.validate(testTrainUser)) {
            return testTrainUser.getUserType().equals(TestTrainUserTypeEnum.EDUCATIONAL_ADMINISTRATOR);
        } else {
            log.warn("User[{}] is invalid", testTrainUser.getUserName());
        }

        return false;
    }

    @Override
    public void handle(TestTrainUser testTrainUser) {
        teacherUserUpdater.handle(testTrainUser);
    }

}
