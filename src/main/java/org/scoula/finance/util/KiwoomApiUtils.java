package org.scoula.finance.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

@Slf4j
@Component
public class KiwoomApiUtils {

    public static String createUrl(String endpoint) {
        String host = "https://mockapi.kiwoom.com";
        return host + endpoint;
    }

    public static String sendPostRequest(String endpoint, String token, String jsonData, String apiId) {
        int maxRetries = 3;
        int retryDelayMs = 500;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                URL url = new URL(createUrl(endpoint));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                connection.setRequestProperty("authorization", "Bearer " + token);
                connection.setRequestProperty("api-id", apiId);
                connection.setDoOutput(true);

                try (OutputStream os = connection.getOutputStream()) {
                    os.write(jsonData.getBytes(StandardCharsets.UTF_8));
                }

                int responseCode = connection.getResponseCode();

                // 429: Too Many Requests
                if (responseCode == 429) {
                    log.warn("Received 429 Too Many Requests. Attempt {}/{}", attempt, maxRetries);
                    Thread.sleep(retryDelayMs); // 일정 시간 대기 후 재시도
                    continue;
                }

                InputStream inputStream = (responseCode >= 200 && responseCode < 300)
                        ? connection.getInputStream()
                        : connection.getErrorStream();

                StringBuilder response = new StringBuilder();
                try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8)) {
                    while (scanner.hasNextLine()) {
                        response.append(scanner.nextLine());
                    }
                }

                return response.toString();

            } catch (IOException | InterruptedException e) {
                log.error("sendPostRequest error (attempt {}): {}", attempt, e.getMessage());
            }
        }

        log.error("sendPostRequest failed after {} attempts", maxRetries);
        return "";
    }
}
