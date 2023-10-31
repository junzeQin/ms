package io.metersphere.cdc.domain;

import com.fasterxml.jackson.annotation.JsonAlias;

import io.metersphere.cdc.domain.enums.OperatorTypeEnum;

import java.io.Serializable;

import lombok.Data;

@Data
public class SinkElement implements Serializable {

    private static final long serialVersionUID = 1L;

    private Source source;

    @JsonAlias("op")
    private OperatorTypeEnum operatorType;

    @Data
    public static class Source implements Serializable {

        private static final long serialVersionUID = 1L;

        private String db;

        private String table;

    }

}

