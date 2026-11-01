package ptit.e23cnpm01.pikachu.server.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Đọc server.properties (cổng, thông tin kết nối CSDL); xem server/server.properties.example.
 *
 * <p>Tệp nằm ở thư mục chạy server (thư mục server/ khi chạy bằng Gradle); đường dẫn khác thì truyền
 * -Dpikachu.config=đường/dẫn.
 *
 * <p>Phụ trách: Dev 2.
 */
public final class ServerConfig {
    private ServerConfig() {
    }

    public static Properties load() {
        Path path = Paths.get(System.getProperty("pikachu.config", "server.properties"));
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(path);
             Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            p.load(reader);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + path.toAbsolutePath()
                    + " (copy server.properties.example and fill it in)", e);
        }
        return p;
    }
}
