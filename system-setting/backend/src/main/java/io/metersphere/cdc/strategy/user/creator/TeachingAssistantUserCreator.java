package io.metersphere.cdc.strategy.user.creator;

import io.metersphere.cdc.domain.enums.TestTrainUserTypeEnum;
import io.metersphere.cdc.domain.model.TestTrainUser;
import io.metersphere.cdc.strategy.user.base.AbstractUserCreator;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TeachingAssistantUserCreator extends AbstractUserCreator {

    @Autowired
    private TeacherUserCreator teacherUserCreator;

    @Override
    public boolean support(TestTrainUser testTrainUser) {
        if (super.validate(testTrainUser)) {
            return testTrainUser.getUserType().equals(TestTrainUserTypeEnum.TEACHING_ASSISTANT);
        } else {
            log.warn("User[{}] is invalid", testTrainUser.getUserName());
        }

        return false;
    }

    @Override
    public void handle(TestTrainUser testTrainUser) {
        teacherUserCreator.handle(testTrainUser);
    }

}
