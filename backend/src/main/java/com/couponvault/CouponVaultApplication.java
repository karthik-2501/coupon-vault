package com.couponvault;

import io.github.cdimascio.dotenv.Dotenv;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CouponVaultApplication {

    public static void main(String[] args) {
        loadDotenvIntoSystemProperties();
        SpringApplication.run(CouponVaultApplication.class, args);
    }

    /**
     * Loads {@code .env} from the current directory or {@code backend/} so Neon (or other) secrets
     * stay out of source control. Existing OS env vars take precedence.
     */
    static void loadDotenvIntoSystemProperties() {
        Path cwdEnv = Path.of(".env");
        Path backendEnv = Path.of("backend", ".env");
        String dir = null;
        if (Files.isRegularFile(cwdEnv)) {
            dir = ".";
        } else if (Files.isRegularFile(backendEnv)) {
            dir = "backend";
        }
        if (dir == null) {
            return;
        }
        Dotenv dotenv = Dotenv.configure().directory(dir).load();
        dotenv
                .entries()
                .forEach(
                        e -> {
                            String key = e.getKey();
                            if (System.getenv(key) == null && System.getProperty(key) == null) {
                                System.setProperty(key, e.getValue());
                            }
                        });
    }
}
