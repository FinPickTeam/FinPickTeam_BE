package org.scoula.monthreport.ext.kosis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class KosisFileBenchmarkProvider {
    private static final ObjectMapper OM = new ObjectMapper();
    private static Map<String, BigDecimal> CACHE = null;

    @SneakyThrows
    public static Map<String, BigDecimal> load(String dataPath, String mappingPath) {
        if (CACHE != null) return CACHE;

        List<Map<String,Object>> rows;
        Map<String,Object> mapping;

        try (InputStream data = res(dataPath);
             InputStream map  = res(mappingPath)) {
            rows = OM.readValue(data, new TypeReference<>() {});
            mapping = OM.readValue(map, new TypeReference<>() {});
        }

        String periodKey = (String) mapping.getOrDefault("periodKey", "PRD_DE");
        String valueKey  = (String) mapping.getOrDefault("valueKey", "DT");
        String selector  = (String) mapping.getOrDefault("categorySelector", "C2_NM");
        Map<String,String> filters = (Map<String,String>) mapping.getOrDefault("filters", Map.of());
        Map<String,String> catMap  = (Map<String,String>) mapping.getOrDefault("categoryMap", Map.of());

        // 1) 필터 적용
        List<Map<String,Object>> filtered = rows.stream()
                .filter(r -> passFilters(r, filters))
                .collect(Collectors.toList());

        // 2) 최신 시점 선택
        String latest = filtered.stream()
                .map(r -> str(r.get(periodKey)))
                .filter(s -> s != null && !s.isBlank())
                .max(String::compareTo)
                .orElse(null);
        if (latest == null) return Map.of();

        List<Map<String,Object>> latestRows = filtered.stream()
                .filter(r -> latest.equals(str(r.get(periodKey))))
                .collect(Collectors.toList());

        // 3) 카테고리 매핑 & 합계
        Map<String, BigDecimal> out = new LinkedHashMap<>();
        for (Map<String,Object> r : latestRows) {
            String kosCat = str(r.get(selector));     // e.g., "03.의류 · 신발"
            String myCat  = catMap.get(kosCat);       // e.g., "shopping"
            if (myCat == null) continue;

            BigDecimal v = parse(str(r.get(valueKey)));
            out.merge(myCat, v, BigDecimal::add);
        }
        // total 자동 계산(없으면)
        out.putIfAbsent("total", out.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add));
        return CACHE = out;
    }

    private static InputStream res(String p){
        InputStream is = KosisFileBenchmarkProvider.class.getResourceAsStream(p.startsWith("/")? p : "/" + p);
        if (is == null) throw new IllegalArgumentException("resource not found: " + p);
        return is;
    }
    private static boolean passFilters(Map<String,Object> r, Map<String,String> f){
        if (f == null || f.isEmpty()) return true;
        for (var e : f.entrySet()){
            if (!Objects.equals(str(r.get(e.getKey())), e.getValue())) return false;
        }
        return true;
    }
    private static String str(Object o){ return o == null ? null : String.valueOf(o); }
    private static BigDecimal parse(String s){
        if (s == null || s.isBlank() || "-".equals(s)) return BigDecimal.ZERO;
        return new BigDecimal(s.replace(",", "").trim());
    }
}
