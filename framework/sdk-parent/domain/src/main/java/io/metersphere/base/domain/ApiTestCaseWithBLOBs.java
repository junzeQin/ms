package io.metersphere.base.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ApiTestCaseWithBLOBs extends ApiTestCase implements Serializable {
    private String description;

    private String request;

    private static final long serialVersionUID = 1L;
}