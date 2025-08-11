package org.scoula.user.util;

import okhttp3.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Random;
import java.util.UUID;

@Component
public class NicknameGenerator {

    private static final Logger log = LoggerFactory.getLogger(NicknameGenerator.class);

    @Value("${gpt.api.key}")
    private String apiKey;

    private static final MediaType JSON = MediaType.parse("application/json");

    // 1) OkHttpClient는 앱 전역에서 재사용 (스레드세이프)
    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(5))
            .readTimeout(Duration.ofSeconds(15))
            .writeTimeout(Duration.ofSeconds(10))
            .build();

    private final String[] fallbackVerbs = {"코딩하는", "주식하는", "청약하는", "절약하는", "분석하는"};
    private final String[] fallbackNouns = {"토끼", "부엉이", "호랑이", "모몽가", "너구리", "펭귄", "도치", "햄스터"};
    private final Random random = new Random();

    public String generateNickname() {
        try {
            String prompt = "랜덤한 {금융 관련 동사}하는 {동물 또는 캐릭터} 형태의 한국어 닉네임을 한 개만 출력해줘. 예: '주식하는 토끼'";

            JSONObject requestBody = new JSONObject()
                    .put("model", "gpt-4o")  // 필요시 gpt-4o-mini
                    .put("messages", new org.json.JSONArray()
                            .put(new JSONObject()
                                    .put("role", "user")
                                    .put("content", prompt)))
                    .put("temperature", 0.9);

            log.info("🟡 GPT 닉네임 생성 요청 시작");

            Request request = new Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody.toString(), JSON))
                    .build();

            // ✅ 2) try-with-resources로 Response를 반드시 닫기
            try (Response response = CLIENT.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.warn("🔴 GPT 응답 실패 - status: {}", response.code());
                    throw new IOException("GPT API 응답 실패: " + response.code());
                }

                // body.string()을 호출하면 ResponseBody는 소모되며 자동 close 되지만,
                // try-with-resources로 Response 전체를 닫는 게 가장 안전.
                String body = response.body().string();
                log.debug("🟢 GPT 응답 원본: {}", body);

                JSONObject responseBody = new JSONObject(body);
                String result = responseBody.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");

                String cleaned = result.replaceAll("[^가-힣a-zA-Z0-9]", "").trim();
                log.info("🟢 GPT 닉네임 생성 성공: {}", cleaned);
                return cleaned;
            }

        } catch (IOException e) {
            log.error("🔻 GPT 호출 실패 - fallback 닉네임 생성으로 대체: {}", e.getMessage());
            return generateFallbackNickname();
        }
    }

    private String generateFallbackNickname() {
        String verb = fallbackVerbs[random.nextInt(fallbackVerbs.length)];
        String noun = fallbackNouns[random.nextInt(fallbackNouns.length)];
        String fallback = verb + noun + UUID.randomUUID().toString().substring(0, 3);
        log.info("🪃 fallback 닉네임 생성: {}", fallback);
        return fallback;
    }
}
