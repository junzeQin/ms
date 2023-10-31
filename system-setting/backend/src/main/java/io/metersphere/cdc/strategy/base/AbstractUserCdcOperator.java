package io.metersphere.cdc.strategy.base;

import io.metersphere.base.domain.UserExample;
import io.metersphere.base.mapper.UserMapper;
import io.metersphere.cdc.domain.SinkUpgradeElement;
import io.metersphere.cdc.domain.enums.TestTrainDeleteFlagEnum;
import io.metersphere.cdc.domain.enums.TestTrainUserStatusEnum;
import io.metersphere.cdc.domain.enums.TestTrainUserTypeEnum;
import io.metersphere.cdc.domain.model.TestTrainUser;
import io.metersphere.cdc.strategy.user.base.AbstractOperateCoordinator;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;

public abstract class AbstractUserCdcOperator implements IOperator<SinkUpgradeElement<TestTrainUser>>, Serializable {

    private static final long serialVersionUID = 1L;

    public static final String tableName = "sys_user";

    @Autowired
    private UserMapper userMapper;

    @Autowired
    @Qualifier("Cdc:TestTrain:User:Create:Coordinator")
    private AbstractOperateCoordinator createCoordinator;

    @Autowired
    @Qualifier("Cdc:TestTrain:User:Update:Coordinator")
    private AbstractOperateCoordinator updateCoordinator;

    @Override
    public boolean support(SinkUpgradeElement<TestTrainUser> sinkElement) {
        var source = sinkElement.getSource();
        var operatorType = sinkElement.getOperatorType();
        if (null == source || null == operatorType) {
            return false;
        }
        if (!source.getTable().equals(tableName)) {
            return false;
        }

        return true;
    }

    protected boolean validateUser(TestTrainUser testTrainUser) {
        if (null == testTrainUser) {
            return false;
        }
        var userStatusStr = testTrainUser.getUserStatusStr();
        if (StrUtil.isBlank(userStatusStr)) {
            return false;
        }
        var userStatus = TestTrainUserStatusEnum.getByCode(userStatusStr);
        if (null == userStatus) {
            return false;
        }
        testTrainUser.setUserStatus(userStatus);
        // if (userStatus.equals(TestTrainUserStatusEnum.DEACTIVATE)) {
        //     return false;
        // }
        var userDeleteFlagStr = testTrainUser.getUserDeleteFlagStr();
        if (StrUtil.isBlank(userDeleteFlagStr)) {
            return false;
        }
        var userDeleteFlag = TestTrainDeleteFlagEnum.getByCode(userDeleteFlagStr);
        if (null == userDeleteFlag) {
            return false;
        }
        testTrainUser.setUserDeleteFlag(userDeleteFlag);
        // if (deleteFlag.equals(TestTrainUserDeleteFlagEnum.DELETE)) {
        //     return false;
        // }
        var userTypeStr = testTrainUser.getUserTypeStr();
        if (StrUtil.isBlank(userTypeStr)) {
            return false;
        }
        var userType = TestTrainUserTypeEnum.getByCode(userTypeStr);
        if (null == userType) {
            return false;
        }
        testTrainUser.setUserType(userType);

        return true;
    }

    protected void handle(TestTrainUser testTrainUser) {
        var queryUser = new UserExample();
        queryUser.createCriteria()
            .andIdEqualTo(testTrainUser.getUserName());
        var userList = userMapper.selectByExample(queryUser);
        if (CollectionUtil.isEmpty(userList)) {
            createCoordinator.handle(testTrainUser);
        } else {
            testTrainUser.setMsUser(userList.get(0));
            updateCoordinator.handle(testTrainUser);
        }
    }

}
