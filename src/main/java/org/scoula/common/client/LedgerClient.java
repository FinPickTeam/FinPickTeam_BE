package org.scoula.common.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
public class LedgerClient {

    private final RestTemplate restTemplate;

    @Value("${internal.ledger.url}")
    private String ledgerBaseUrl;

    public LedgerClient() {
        this.restTemplate = new RestTemplate();
    }

    public int getTotalExpense(Long userId, String categoryName, LocalDate from, LocalDate to) {
        String url = String.format("%s/api/users/%d/ledger?category=%s&from=%s&to=%s",
                ledgerBaseUrl, userId, categoryName, from.toString(), to.toString());

        try {
            ResponseEntity<LedgerResponse> response = restTemplate.getForEntity(url, LedgerResponse.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return 0;
            }

            List<LedgerEntry> entries = response.getBody().getData();
            return entries.stream().mapToInt(entry -> (int) entry.getAmount()).sum();

        } catch (Exception e) {
            log.warn("🔥 거래내역 호출 실패 (userId: {}) - {}", userId, e.getMessage());
            return 0;
        }
    }

    // 내부 응답 DTO 클래스
    private static class LedgerResponse {
        private List<LedgerEntry> data;
        public List<LedgerEntry> getData() { return data; }
        public void setData(List<LedgerEntry> data) { this.data = data; }
    }

    private static class LedgerEntry {
        private double amount;
        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }
    }
}
