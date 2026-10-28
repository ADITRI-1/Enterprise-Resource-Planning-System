package com.iiitd.erp.io.controller.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.io.dto.request.ListParams;
import com.iiitd.erp.io.dto.response.PageResponse;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Applies search, sort and pagination to an in-memory list of response rows. Fields are
 * addressed by their JSON names, so whatever a client sees in a row it can sort or search on.
 * Lists in this application are small (hundreds to low thousands of rows), so doing this after
 * loading is simpler than pushing every combination down into SQL.
 */
@Component
public class Paging {

    private static final TypeReference<Map<String, Object>> ROW = new TypeReference<>() {};

    private final ObjectMapper objectMapper;

    public Paging(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> PageResponse<T> page(List<T> rows, ListParams params) {
        ListParams p = params == null ? ListParams.all() : params;
        int page = p.page() == null ? 0 : p.page();
        int size = p.size() == null ? ListParams.DEFAULT_SIZE : p.size();
        if (page < 0 || size < 1 || size > ListParams.MAX_SIZE) {
            throw ErpException.of(ErrorCode.REQUEST_VALIDATION_FAILED,
                    Map.of("reason", "page must be >= 0 and size between 1 and " + ListParams.MAX_SIZE));
        }

        List<Entry<T>> entries = rows.stream()
                .map(row -> new Entry<>(row, objectMapper.convertValue(row, ROW)))
                .filter(e -> matches(e.fields(), p.q()))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        if (p.sort() != null && !p.sort().isBlank()) {
            entries.sort(comparator(p.sort().trim(), entries));
        }

        int total = entries.size();
        int from = Math.min(page * size, total);
        int to = Math.min(from + size, total);
        List<T> items = entries.subList(from, to).stream().map(Entry::row).toList();
        return new PageResponse<>(items, page, size, total, (total + size - 1) / size);
    }

    private static boolean matches(Map<String, Object> fields, String q) {
        if (q == null || q.isBlank()) {
            return true;
        }
        String needle = q.trim().toLowerCase(Locale.ROOT);
        return fields.values().stream()
                .filter(v -> v instanceof String || v instanceof Number || v instanceof Boolean)
                .anyMatch(v -> v.toString().toLowerCase(Locale.ROOT).contains(needle));
    }

    private static <T> Comparator<Entry<T>> comparator(String sort, List<Entry<T>> entries) {
        boolean desc = sort.startsWith("-") || sort.toLowerCase(Locale.ROOT).endsWith(",desc");
        String field = sort.replaceFirst("^-", "").replaceFirst("(?i),(asc|desc)$", "").trim();
        if (!entries.isEmpty() && !entries.get(0).fields().containsKey(field)) {
            throw ErpException.of(ErrorCode.REQUEST_VALIDATION_FAILED,
                    Map.of("reason", "unknown sort field", "sort", field,
                            "allowed", new TreeSet<>(entries.get(0).fields().keySet())));
        }
        Comparator<Entry<T>> byField = (a, b) -> compareValues(a.fields().get(field), b.fields().get(field));
        Comparator<Entry<T>> ordered = desc ? byField.reversed() : byField;
        // Rows without a value always go last, whichever direction is chosen.
        return Comparator.<Entry<T>, Boolean>comparing(e -> e.fields().get(field) == null).thenComparing(ordered);
    }

    private static int compareValues(Object a, Object b) {
        if (a == null || b == null) {
            return a == null ? (b == null ? 0 : 1) : -1;
        }
        if (a instanceof Number na && b instanceof Number nb) {
            return Double.compare(na.doubleValue(), nb.doubleValue());
        }
        if (a instanceof Boolean ba && b instanceof Boolean bb) {
            return Boolean.compare(ba, bb);
        }
        return a.toString().compareToIgnoreCase(b.toString());
    }

    private record Entry<T>(T row, Map<String, Object> fields) {}
}
