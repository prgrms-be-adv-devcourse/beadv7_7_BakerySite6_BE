package dev.hyune.logstatistics.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface LogParser {
    List<LogRecord> parseStream(InputStream inputStream) throws IOException;
}
