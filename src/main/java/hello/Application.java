package hello;

import hello.constants.FileConstants;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.util.FileSystemUtils;

import javax.servlet.MultipartConfigElement;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootApplication
public class Application {

    private static Long MAX_FILE_SIZE =  1024 * 1024 * 20L;
    public static void main(String[] args) {
              SpringApplication.run(Application.class, args);
    }
    @Bean
    CommandLineRunner init() {
        return (args) -> {
            FileSystemUtils.deleteRecursively(new File(FileConstants.ROOT_PATH));

            Files.createDirectory(Paths.get(FileConstants.ROOT_PATH));
        };
    }

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        // Setup the application container to be accept multipart requests
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(MAX_FILE_SIZE);
        factory.setMaxRequestSize(MAX_FILE_SIZE);

        // Return the configuration to setup multipart in the container
        return factory.createMultipartConfig();
    }

}