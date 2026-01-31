package uz.hemis.api.legacy.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import java.time.format.DateTimeFormatter;

/**
 * OLD-HEMIS CUBA Platform bilan mos sana formatlari:
 * - LocalDate → "yyyy-MM-dd"
 * - LocalDateTime → "yyyy-MM-dd HH:mm:ss.SSS"
 */
@Configuration
@RequiredArgsConstructor
public class LegacyJacksonConfig {

    private final ObjectMapper objectMapper;

    @PostConstruct
    public void configureDateFormat() {
        JavaTimeModule module = new JavaTimeModule();
        module.addSerializer(java.time.LocalDate.class,
                new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        module.addSerializer(java.time.LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));

        objectMapper.registerModule(module);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
