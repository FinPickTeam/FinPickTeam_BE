package org.scoula.finance.util;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class CsvUtils {
    // csv 파싱
    public List<Map<String, Object>> parseCsvResult(String filePath) {
        List<Map<String, Object>> resultList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {

            String headerLine = br.readLine();
            if (headerLine == null) {
                return resultList; // 빈 파일 처리
            }

            String[] headers = headerLine.replace("\uFEFF", "").split(",");

            System.out.println("헤더 라인: " + Arrays.toString(headers));

            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",", -1); // 빈 칸도 포함

                Map<String, Object> row = new HashMap<>();
                for (int i = 0; i < headers.length && i < values.length; i++) {
                    String key = headers[i].trim();
                    String value = values[i].trim();

                    try {
                        row.put(key, Double.parseDouble(value));
                    } catch (NumberFormatException e) {
                        row.put(key, value); // 숫자가 아니면 문자열로
                    }
                }

                resultList.add(row);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultList;
    }
}
