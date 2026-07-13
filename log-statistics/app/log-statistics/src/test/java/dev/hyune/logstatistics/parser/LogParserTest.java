package dev.hyune.logstatistics.parser;

import dev.hyune.logstatistics.parser.kokoa.KokoaLogParser;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LogParserTest {

    @Test
    void parseStream_정상파싱() throws IOException {
        // given
        LogParser parser = new KokoaLogParser();
        String logs = """
                [200][http://apis.kokoa.com/search/knowledge?apikey=23jf][IE][2012-06-10 08:00:00]
                [200][http://apis.kokoa.com/search/blog?apikey=wejf][IE][2012-06-10 08:00:02]
                [404][http://apis.kokoa.com/search/error][Firefox][2012-06-10 08:00:33]
                [10][http://apis.kokoa.com/search/news][IE][2012-06-10 08:01:37]
                """;
        InputStream inputStream = new ByteArrayInputStream(logs.getBytes(StandardCharsets.UTF_8));

        // when
        List<LogRecord> records = parser.parseStream(inputStream);

        // then
        System.out.println("\n===== 파싱 결과 =====");
        System.out.println("총 " + records.size() + "개 레코드 파싱");
        System.out.println("첫 번째 레코드:");
        System.out.println("  - 상태코드: " + records.get(0).statusCode());
        System.out.println("  - API Key: " + records.get(0).extractApiKey());
        System.out.println("  - Service ID: " + records.get(0).extractApiServiceId());
        System.out.println("===================\n");

        assertEquals(4, records.size());
        assertEquals(200, records.get(0).statusCode());
        assertEquals("23jf", records.get(0).extractApiKey());
        assertEquals("knowledge", records.get(0).extractApiServiceId());
        assertNull(records.get(3).extractApiKey());
    }

    @Test
    void parseStream_실제파일_정상파싱() throws IOException {
        // given
        LogParser parser = new KokoaLogParser();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("kokoa.log");
        assertNotNull(inputStream);

        // when
        List<LogRecord> records = parser.parseStream(inputStream);

        // then
        System.out.println("\n===== 실제 파일 파싱 =====");
        System.out.println("총 " + records.size() + "개 레코드 파싱 성공");
        System.out.println("======================\n");

        assertTrue(records.size() > 0);
    }
}
