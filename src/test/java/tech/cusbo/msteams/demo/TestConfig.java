package tech.cusbo.msteams.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import tech.cusbo.msteams.demo.inboundevent.GraphEventsEncryptionService;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public GraphEventsEncryptionService mockGraphEventsEncryptionService(ObjectMapper mapper) {

        return new GraphEventsEncryptionService(mapper) {
            @Override
            public void init() {

            }
        };
    }
}