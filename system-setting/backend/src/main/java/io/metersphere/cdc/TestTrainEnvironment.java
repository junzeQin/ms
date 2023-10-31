package io.metersphere.cdc;

import com.ververica.cdc.connectors.mysql.source.MySqlSource;
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema;

import java.util.Properties;

import lombok.SneakyThrows;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class TestTrainEnvironment implements InitializingBean {

    @Value("${cdc.testTrain.mysql.hostname:}")
    private String hostname;

    @Value("${cdc.testTrain.mysql.port:}")
    private Integer port;

    @Value("${cdc.testTrain.mysql.username:}")
    private String username;

    @Value("${cdc.testTrain.mysql.password:}")
    private String password;

    @Value("${cdc.testTrain.mysql.databaseList:}")
    private String[] databases;

    @Value("${cdc.testTrain.mysql.tableList:}")
    private String[] tables;

    @Autowired
    private TestTrainSink testTrainSink;

    private StreamExecutionEnvironment environment;

    @SneakyThrows
    @Async
    public void execute() {
        environment.execute();
    }

    private MySqlSource<String> initSource() {
        var properties = new Properties();
        properties.put("decimal.handling.mode", "string");

        return MySqlSource.<String>builder()
            .hostname(hostname)
            .port(port)
            .username(username)
            .password(password)
            .databaseList(databases)
            .tableList(tables)
            .debeziumProperties(properties)
            .deserializer(new JsonDebeziumDeserializationSchema())
            .includeSchemaChanges(false)
            .build();
    }

    @Override
    public void afterPropertiesSet() {
        var mySqlSource = initSource();
        var configuration = new Configuration();
        configuration.setInteger(RestOptions.PORT, 8848);
        var environment = StreamExecutionEnvironment.getExecutionEnvironment(configuration);
        environment.enableCheckpointing(10000)
            .fromSource(mySqlSource, WatermarkStrategy.noWatermarks(), "Test Train MySql Source")
            .addSink(testTrainSink)
            .name("TestTrainEnvironment");
        this.environment = environment;
    }

}
