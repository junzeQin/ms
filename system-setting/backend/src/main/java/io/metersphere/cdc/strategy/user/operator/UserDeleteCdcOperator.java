package io.metersphere.cdc.strategy.user.operator;

import io.metersphere.base.domain.UserGroupExample;
import io.metersphere.base.mapper.UserGroupMapper;
import io.metersphere.base.mapper.UserMapper;
import io.metersphere.cdc.domain.SinkUpgradeElement;
import io.metersphere.cdc.domain.enums.OperatorTypeEnum;
import io.metersphere.cdc.domain.model.TestTrainUser;
import io.metersphere.cdc.strategy.base.AbstractUserCdcOperator;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component()
@Slf4j
public class UserDeleteCdcOperator extends AbstractUserCdcOperator {

    @Autowired
    private UserGroupMapper userGroupMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean support(SinkUpgradeElement<TestTrainUser> sinkElement) {
        if (super.support(sinkElement)) {
            return sinkElement.getOperatorType().equals(OperatorTypeEnum.d);
        }

        return false;
    }

    @Override
    public void handle(SinkUpgradeElement<TestTrainUser> sinkElement) {
        var testTrainUser = sinkElement.getBefore();
        if (!super.validateUser(testTrainUser)) {
            log.warn("UserDeleteCdcOperator#handle validate user do not meet requirements: {}", testTrainUser);
            return;
        }
        deleteUser(testTrainUser.getUserName());
    }

    public void deleteUser(String userId) {
        var userGroupExample = new UserGroupExample();
        userGroupExample.createCriteria().andUserIdEqualTo(userId);
        userGroupMapper.deleteByExample(userGroupExample);

        userMapper.deleteByPrimaryKey(userId);
    }

}
