package org.scoula.user.util;

import okhttp3.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Random;
import java.util.UUID;

@Component
public class NicknameGenerator {

    private static final Logger log = LoggerFactory.getLogger(NicknameGenerator.class);

    @Value("${gpt.api.key}")
    private String apiKey;

    private final String[] fallbackVerbs = {"코딩하는", "주식하는", "청약하는", "절약하는", "분석하는"};
    private final String[] fallbackNouns = {"토끼", "부엉이", "호랑이", "모몽가", "너구리", "펭귄", "도치", "햄스터"};
    private final Random random = new Random();

    public String generateNickname() {
        try {
            OkHttpClient client = new OkHttpClient();

            String prompt = "랜덤한 {동사}하는{동물 또는 캐릭터} 형태의 한국어 닉네임을 한 개만 출력해줘. 예: '코딩하는토끼'";
            JSONObject requestBody = new JSONObject()
                    .put("model", "gpt-4o")  // 또는 gpt-4o-mini
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
                    .post(RequestBody.create(requestBody.toString(), MediaType.parse("application/json")))
                    .build();

            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                log.warn("🔴 GPT 응답 실패 - status: {}", response.code());
                throw new IOException("GPT API 응답 실패: " + response.code());
            }

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
