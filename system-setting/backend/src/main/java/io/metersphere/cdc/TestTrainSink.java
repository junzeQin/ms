package io.metersphere.cdc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;

import io.metersphere.cdc.domain.SinkElement;
import io.metersphere.cdc.strategy.Coordinator;
import io.metersphere.commons.utils.ApplicationContextHolder;

import lombok.extern.slf4j.Slf4j;

import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TestTrainSink extends RichSinkFunction<String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
    }

    @Override
    public void invoke(String value, Context context) {
        try {
            var sinkElement = objectMapper.readValue(value, new TypeReference<SinkElement>() {
            });
            var coordinator = ApplicationContextHolder.getBean("Cdc:TestTrain:Operate:Coordinator", Coordinator.class);
            coordinator.handle(sinkElement, value);
        } catch (Exception e) {
            log.error("TestTrainSink#invoke[{}] handle error: {}", value, Throwables.getStackTraceAsString(e));
        }
    }

}
