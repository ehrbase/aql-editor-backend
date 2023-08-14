package org.ehrbase.aqleditor.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ehrbase.openehr.sdk.aql.serializer.AqlQueryObjectMapperProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author Stefan Spiska
 */
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {

        return AqlQueryObjectMapperProvider.getObjectMapper();
    }
}
