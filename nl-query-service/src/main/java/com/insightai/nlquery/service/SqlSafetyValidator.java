package com.insightai.nlquery.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lightweight SQL safety validator. Rejects any non-SELECT statement (DDL / DML)
 * and enforces an upper bound on LIMIT clause size to prevent runaway scans.
 *
 * <p>Designed to be intentionally strict — the consumer of generated SQL is a
 * read-only NL2SQL service, so any write/mutation verb must be refused.
 */
@Slf4j
@Component
public class SqlSafetyValidator {

    /** DDL / DML verbs that are never allowed in NL2SQL output. */
    public static final List<String> FORBIDDEN_KEYWORDS = Arrays.asList(
            "DROP", "DELETE", "UPDATE", "INSERT", "ALTER",
            "TRUNCATE", "GRANT", "REVOKE", "CREATE", "RENAME",
            "REPLACE", "MERGE", "CALL", "EXEC", "EXECUTE", "LOCK"
    );

    /** Upper bound on LIMIT rows to protect the data source. */
    public static final int MAX_LIMIT_ROWS = 10_000;

    private static final Pattern LIMIT_PATTERN =
            Pattern.compile("\\bLIMIT\\s+(\\d+)", Pattern.CASE_INSENSITIVE);

    private static final Pattern COMMENT_LINE = Pattern.compile("--.*$", Pattern.MULTILINE);
    private static final Pattern COMMENT_BLOCK = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);

    /** Outcome of a {@link #validate(String)} call. */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ValidationResult {
        private boolean valid;
        private String reason;
        private List<String> violations;

        public static ValidationResult ok() {
            return ValidationResult.builder().valid(true).violations(new ArrayList<>()).build();
        }

        public static ValidationResult reject(String reason, List<String> violations) {
            return ValidationResult.builder()
                    .valid(false)
                    .reason(reason)
                    .violations(violations)
                    .build();
        }
    }

    /**
     * Validate that the supplied SQL is safe to execute against the data source.
     *
     * <p>Rules enforced:
     * <ol>
     *     <li>Non-null / non-empty input.</li>
     *     <li>First significant token must be SELECT (after stripping comments).</li>
     *     <li>No forbidden DDL / DML keyword may appear anywhere (word-boundary match,
     *         but is intentionally tolerant of keywords appearing inside identifiers —
     *         see {@link #containsForbiddenKeyword}).</li>
     *     <li>If a LIMIT clause is present, its value must not exceed
     *         {@link #MAX_LIMIT_ROWS}.</li>
     *     <li>No semicolon-stacked statements (single statement only).</li>
     * </ol>
     */
    public ValidationResult validate(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return ValidationResult.reject("SQL is empty", List.of("EMPTY_SQL"));
        }

        String stripped = stripComments(sql).trim();
        if (stripped.isEmpty()) {
            return ValidationResult.reject("SQL is empty after stripping comments", List.of("EMPTY_SQL"));
        }

        List<String> violations = new ArrayList<>();

        // Single-statement enforcement.
        long semis = stripped.chars().filter(c -> c == ';').count();
        if (semis > 1 || (semis == 1 && !stripped.endsWith(";"))) {
            violations.add("MULTI_STATEMENT");
        }

        String firstToken = firstSignificantWord(stripped);
        if (firstToken == null || !"SELECT".equalsIgnoreCase(firstToken)) {
            violations.add("NOT_SELECT_PREFIX");
        }

        String matchedForbidden = containsForbiddenKeyword(stripped);
        if (matchedForbidden != null) {
            violations.add("FORBIDDEN_KEYWORD:" + matchedForbidden);
        }

        Integer limitValue = extractLimit(stripped);
        if (limitValue != null && limitValue > MAX_LIMIT_ROWS) {
            violations.add("LIMIT_TOO_LARGE:" + limitValue);
        }

        if (!violations.isEmpty()) {
            String reason = "SQL rejected: " + String.join(", ", violations);
            log.warn("SQL safety validation failed: {} | sql={}", reason, stripped);
            return ValidationResult.reject(reason, violations);
        }

        log.debug("SQL safety validation passed for: {}", stripped);
        return ValidationResult.ok();
    }

    /** Convenience: throws if invalid, otherwise returns the original SQL. */
    public String requireSafe(String sql) {
        ValidationResult vr = validate(sql);
        if (!vr.isValid()) {
            throw new UnsafeSqlException(vr.getReason());
        }
        return sql;
    }

    // ---- helpers ----

    private String stripComments(String sql) {
        String noBlock = COMMENT_BLOCK.matcher(sql).replaceAll(" ");
        return COMMENT_LINE.matcher(noBlock).replaceAll(" ");
    }

    private String firstSignificantWord(String sql) {
        Matcher m = Pattern.compile("^\\s*([A-Za-z_]+)").matcher(sql);
        return m.find() ? m.group(1) : null;
    }

    /**
     * Detect forbidden DDL/DML keywords using a word-boundary regex.
     * Returns the matched keyword (uppercase) or null if none.
     *
     * <p>Note: we compare against {@link #FORBIDDEN_KEYWORDS} so adding a new
     * forbidden verb automatically gets the same boundary-aware detection.
     */
    String containsForbiddenKeyword(String sql) {
        for (String kw : FORBIDDEN_KEYWORDS) {
            Pattern p = Pattern.compile("\\b" + kw + "\\b", Pattern.CASE_INSENSITIVE);
            if (p.matcher(sql).find()) {
                return kw.toUpperCase(Locale.ROOT);
            }
        }
        return null;
    }

    Integer extractLimit(String sql) {
        Matcher m = LIMIT_PATTERN.matcher(sql);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    /** Thrown when a SQL string fails {@link SqlSafetyValidator#requireSafe(String)}. */
    public static class UnsafeSqlException extends RuntimeException {
        public UnsafeSqlException(String message) {
            super(message);
        }
    }
}
