package dev.hyune.logstatistics.config;

import dev.hyune.logstatistics.parser.LogParser;
import dev.hyune.logstatistics.parser.kokoa.KokoaLogParser;
import dev.hyune.logstatistics.parser.maver.MaverLogParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class ParserConfig {

    @Bean
    public LogParser kokoaLogParser() {
        return new KokoaLogParser();
    }

    @Bean
    public LogParser maverLogParser() {
        return new MaverLogParser();
    }

    @Bean
    public Map<String, LogParser> parsers() {
        return Map.of(
                "kokoa", kokoaLogParser(),
                "maver", maverLogParser()
        );
    }
}
