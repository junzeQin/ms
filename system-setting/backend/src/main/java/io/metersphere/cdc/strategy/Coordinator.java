package io.metersphere.cdc.strategy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.metersphere.cdc.domain.SinkElement;
import io.metersphere.cdc.domain.SinkUpgradeElement;
import io.metersphere.cdc.domain.model.TestTrainLesson;
import io.metersphere.cdc.domain.model.TestTrainLessonTeam;
import io.metersphere.cdc.domain.model.TestTrainLessonTeamUser;
import io.metersphere.cdc.domain.model.TestTrainLessonUser;
import io.metersphere.cdc.domain.model.TestTrainProject;
import io.metersphere.cdc.domain.model.TestTrainUser;
import io.metersphere.cdc.strategy.base.AbstractLessonCdcOperator;
import io.metersphere.cdc.strategy.base.AbstractLessonTeamCdcOperator;
import io.metersphere.cdc.strategy.base.AbstractLessonTeamUserCdcOperator;
import io.metersphere.cdc.strategy.base.AbstractLessonUserCdcOperator;
import io.metersphere.cdc.strategy.base.AbstractProjectCdcOperator;
import io.metersphere.cdc.strategy.base.AbstractUserCdcOperator;
import io.metersphere.commons.exception.MSException;

import java.io.Serializable;
import java.util.List;
import java.util.function.Supplier;

import lombok.SneakyThrows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("Cdc:TestTrain:Operate:Coordinator")
public class Coordinator implements Serializable {

    private static final long serialVersionUID = 1L;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private List<AbstractUserCdcOperator> userOperatorList;

    @Autowired
    private List<AbstractProjectCdcOperator> projectOperatorList;

    @Autowired
    private List<AbstractLessonTeamCdcOperator> lessonTeamOperatorList;

    @Autowired
    private List<AbstractLessonUserCdcOperator> lessonUserOperatorList;

    @Autowired
    private List<AbstractLessonTeamUserCdcOperator> lessonTeamUserOperatorList;

    @Autowired
    private List<AbstractLessonCdcOperator> lessonOperatorList;

    @SneakyThrows
    public void handle(SinkElement sinkElement, String value) {
        if (null == sinkElement) {
            return;
        }
        switch (sinkElement.getSource().getTable()) {
            case AbstractUserCdcOperator.tableName:
                handleUser(value);
                break;
            case AbstractProjectCdcOperator.tableName:
                handleProject(value);
                break;
            case AbstractLessonTeamCdcOperator.tableName:
                handleLessonTeam(value);
                break;
            case AbstractLessonUserCdcOperator.tableName:
                handleLessonUser(value);
                break;
            case AbstractLessonTeamUserCdcOperator.tableName:
                handleLessonTeamUser(value);
                break;
            case AbstractLessonCdcOperator.tableName:
                handleLesson(value);
                break;
            default:
                break;
        }
    }

    @SneakyThrows
    private void handleUser(String value) {
        var sinkUser = objectMapper.readValue(value, new TypeReference<SinkUpgradeElement<TestTrainUser>>() {
        });
        var userOperator = userOperatorList.stream()
            .filter(y -> y.support(sinkUser))
            .findAny()
            .orElseThrow((Supplier<Exception>) () -> new MSException("Coordinator#handle[" + sinkUser + "] get user operator fault"));
        userOperator.handle(sinkUser);
    }

    @SneakyThrows
    private void handleProject(String value) {
        var sinkProject = objectMapper.readValue(value, new TypeReference<SinkUpgradeElement<TestTrainProject>>() {
        });
        var projectOperator = projectOperatorList.stream()
            .filter(y -> y.support(sinkProject))
            .findAny()
            .orElseThrow((Supplier<Exception>) () -> new MSException("Coordinator#handle[" + sinkProject + "] get project operator fault"));
        projectOperator.handle(sinkProject);
    }

    @SneakyThrows
    private void handleLessonTeam(String value) {
        var lessonTeam = objectMapper.readValue(value, new TypeReference<SinkUpgradeElement<TestTrainLessonTeam>>() {
        });
        var lessonTeamOperator = lessonTeamOperatorList.stream()
            .filter(y -> y.support(lessonTeam))
            .findAny()
            .orElseThrow((Supplier<Exception>) () -> new MSException("Coordinator#handle[" + lessonTeam + "] get lesson team operator fault"));
        lessonTeamOperator.handle(lessonTeam);
    }

    @SneakyThrows
    private void handleLessonUser(String value) {
        var lessonUser = objectMapper.readValue(value, new TypeReference<SinkUpgradeElement<TestTrainLessonUser>>() {
        });
        var lessonUserOperator = lessonUserOperatorList.stream()
            .filter(y -> y.support(lessonUser))
            .findAny()
            .orElseThrow((Supplier<Exception>) () -> new MSException("Coordinator#handle[" + lessonUser + "] get lesson user operator fault"));
        lessonUserOperator.handle(lessonUser);
    }

    @SneakyThrows
    private void handleLessonTeamUser(String value) {
        var lessonTeamUser = objectMapper.readValue(value, new TypeReference<SinkUpgradeElement<TestTrainLessonTeamUser>>() {
        });
        var lessonTeamUserOperator = lessonTeamUserOperatorList.stream()
            .filter(y -> y.support(lessonTeamUser))
            .findAny()
            .orElseThrow((Supplier<Exception>) () -> new MSException("Coordinator#handle[" + lessonTeamUser + "] get lesson team user operator fault"));
        lessonTeamUserOperator.handle(lessonTeamUser);
    }

    @SneakyThrows
    private void handleLesson(String value) {
        var lesson = objectMapper.readValue(value, new TypeReference<SinkUpgradeElement<TestTrainLesson>>() {
        });
        var lessonOperator = lessonOperatorList.stream()
            .filter(y -> y.support(lesson))
            .findAny()
            .orElseThrow((Supplier<Exception>) () -> new MSException("Coordinator#handle[" + lesson + "] get lesson operator fault"));
        lessonOperator.handle(lesson);
    }

}
