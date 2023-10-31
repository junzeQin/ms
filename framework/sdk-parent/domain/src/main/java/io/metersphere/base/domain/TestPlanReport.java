package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class TestPlanReport implements Serializable {
    private String id;

    private String testPlanId;

    private Long createTime;

    private Long updateTime;

    private String name;

    private String status;

    private String triggerMode;

    private String creator;

    private Long startTime;

    private Long endTime;

    private Boolean isApiCaseExecuting;

    private Boolean isScenarioExecuting;

    private Boolean isPerformanceExecuting;

    private String principal;

    private String components;

    private Boolean isNew;

    private Boolean isUiScenarioExecuting;

    private String runInfo;

    private static final long serialVersionUID = 1L;
}