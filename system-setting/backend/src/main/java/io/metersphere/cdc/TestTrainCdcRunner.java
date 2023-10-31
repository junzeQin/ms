package io.metersphere.cdc;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TestTrainCdcRunner implements CommandLineRunner {

    @Autowired
    private TestTrainEnvironment testTrainEnvironment;

    @Override
    public void run(String... args) throws Exception {
        testTrainEnvironment.execute();
    }

}
