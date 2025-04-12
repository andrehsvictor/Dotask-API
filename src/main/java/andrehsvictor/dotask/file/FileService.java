package andrehsvictor.dotask.file;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class FileService {

    public String readFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }

        try {
            if (filePath.startsWith("classpath:")) {
                String resourcePath = filePath.substring(10);
                ClassPathResource resource = new ClassPathResource(resourcePath);
                try (InputStream inputStream = resource.getInputStream()) {
                    return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                }
            } else {
                Path path = Path.of(filePath);
                return Files.readString(path, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + filePath, e);
        }
    }
}