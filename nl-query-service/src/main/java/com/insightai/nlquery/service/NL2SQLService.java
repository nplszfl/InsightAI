package com.insightai.nlquery.service;

import com.insightai.nlquery.dto.ConversationContextDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rule-based NL2SQL engine.
 *
 * <p>Maps natural-language patterns to SQL fragments. Designed to cover ~80% of
 * common BI/analytics queries without depending on an external LLM.
 *
 * <p>Supports coreference resolution via {@link #resolveReferences(String, List)}:
 * Chinese pronouns like "上次的表" / "那个" / "它" / "上述" can be substituted
 * with concrete identifiers (table name, intent, time range) drawn from the
 * recent conversation history of the same session.
 */
@Slf4j
@Service
public class NL2SQLService {

    // -------- public API --------

    /** Parse a NL query in the context of a session (uses previous context if non-empty). */
    public String parseQuery(String query, List<ConversationContextDto> previousContext) {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Query must not be empty");
        }
        log.debug("parseQuery: '{}' with {} prior turns", query,
                previousContext == null ? 0 : previousContext.size());

        String resolved = resolveReferences(query, previousContext == null ? List.of() : previousContext);
        return generateSql(resolved);
    }

    /** Backward-compatible overload for stateless parsing (no session context). */
    public String parseQuery(String query) {
        return parseQuery(query, List.of());
    }

    /**
     * Coreference/anaphora resolution.
     *
     * <p>Recognised Chinese pronouns / demonstratives:
     * <ul>
     *     <li>"上次" / "上一次" / "刚才" → previous turn's table name + time range</li>
     *     <li>"那个" / "这" / "该" / "上述" → previous turn's table name</li>
     *     <li>"它" / "其" → previous turn's table name</li>
     *     <li>"和" / "与" → JOIN (handled in {@link #generateSql})</li>
     * </ul>
     *
     * <p>If no prior context is available the query is returned unchanged so
     * downstream generation still operates on a well-formed string.
     */
    public String resolveReferences(String query, List<ConversationContextDto> previousContext) {
        if (query == null || previousContext == null || previousContext.isEmpty()) {
            return query == null ? "" : query;
        }

        ConversationContextDto last = lastSuccessful(previousContext);
        if (last == null) {
            return query;
        }

        String resolved = query;

        // 1) "上次的表" / "上一次" / "刚才" — replace with previous table name.
        String previousTable = last.getTableName();
        if (previousTable != null && !previousTable.isBlank()) {
            resolved = RE_PREVIOUS_TABLE.matcher(resolved)
                    .replaceAll(Matcher.quoteReplacement(previousTable));
            resolved = RE_THAT_ONE.matcher(resolved)
                    .replaceAll(Matcher.quoteReplacement(previousTable));
            resolved = RE_IT_PRONOUN.matcher(resolved)
                    .replaceAll(Matcher.quoteReplacement(previousTable));
        }

        // 2) "上次的 7 天" / "刚才的时间范围" — append the prior time range if present.
        String priorRange = extractTimeRangeHint(last.getGeneratedSql());
        if (priorRange != null && RE_PREVIOUS_TIME.matcher(resolved).find()) {
            resolved = RE_PREVIOUS_TIME.matcher(resolved)
                    .replaceAll(Matcher.quoteReplacement(priorRange));
        }

        log.debug("Resolved references: '{}' -> '{}'", query, resolved);
        return resolved;
    }

    /**
     * Generate SQL from a fully-resolved natural-language query using the rule engine.
     * Supports 10 core patterns covering the bulk of typical BI questions.
     */
    public String generateSql(String query) {
        String lower = query.toLowerCase(Locale.ROOT);
        StringBuilder sql = new StringBuilder();

        // SELECT projection
        if (lower.contains("总数") || lower.contains("count") || lower.contains("多少")) {
            sql.append("SELECT COUNT(*) AS total");
        } else if (lower.contains("平均") || lower.contains("均值") || lower.contains("average") || lower.contains("avg")) {
            sql.append("SELECT AVG(value) AS average_value");
        } else if (lower.contains("总和") || lower.contains("sum") || lower.contains("求和")) {
            sql.append("SELECT SUM(value) AS sum_value");
        } else if (lower.startsWith("top ") || lower.contains(" top ") || lower.contains("前 ") || lower.contains("最高的")) {
            sql.append("SELECT *");
        } else {
            sql.append("SELECT *");
        }

        // FROM clause — table keyword mapping
        String table = detectTable(lower);
        sql.append(" FROM ").append(table);

        // JOIN detection — "和" / "与" introduce a second table
        String secondaryTable = detectJoinTable(lower, table);
        if (secondaryTable != null) {
            sql.append(" JOIN ").append(secondaryTable)
                    .append(" ON ").append(table).append(".id = ").append(secondaryTable).append(".id");
        }

        // WHERE clause — time range
        String timeFilter = detectTimeFilter(query);
        if (timeFilter != null) {
            sql.append(" WHERE ").append(timeFilter);
        }

        // ORDER BY + LIMIT for ranking
        if (lower.contains("top ") || lower.contains("前 ") || lower.contains("最高的")) {
            Integer topN = extractTopN(query);
            sql.append(" ORDER BY value DESC LIMIT ").append(topN != null ? topN : 10);
        } else if (lower.contains("总数") || lower.contains("count") || lower.contains("平均") || lower.contains("总和")) {
            // aggregations don't need LIMIT
        } else {
            // safe default — cap result set
            sql.append(" LIMIT 100");
        }

        return sql.toString();
    }

    // ---- helpers ----

    /** Pick the most recent context row that produced a SQL we can reuse. */
    private ConversationContextDto lastSuccessful(List<ConversationContextDto> ctx) {
        for (int i = ctx.size() - 1; i >= 0; i--) {
            ConversationContextDto c = ctx.get(i);
            if (c != null && c.getTableName() != null && !c.getTableName().isBlank()) {
                return c;
            }
        }
        return null;
    }

    /** Map NL keywords → physical table. Order matters: more specific first. */
    String detectTable(String lower) {
        if (lower.contains("销售") || lower.contains("sales")) return "sales";
        if (lower.contains("订单") || lower.contains("order")) return "orders";
        if (lower.contains("用户") || lower.contains("user") || lower.contains("客户")) return "users";
        if (lower.contains("产品") || lower.contains("product") || lower.contains("商品")) return "products";
        if (lower.contains("支付") || lower.contains("payment")) return "payments";
        if (lower.contains("库存") || lower.contains("stock") || lower.contains("inventory")) return "inventory";
        if (lower.contains("访问") || lower.contains("visit") || lower.contains("浏览") || lower.contains("pv")) return "page_views";
        return "data_table";
    }

    String detectJoinTable(String lower, String primaryTable) {
        String secondary = null;
        if ((lower.contains("和") || lower.contains("与")) && !lower.contains("和".repeat(2))) {
            // only treat as JOIN when followed by another noun, not conjunction in a list
            if (lower.contains("用户") || lower.contains("user")) secondary = "users";
            else if (lower.contains("产品") || lower.contains("product") || lower.contains("商品")) secondary = "products";
            else if (lower.contains("订单") || lower.contains("order")) secondary = "orders";
            else if (lower.contains("销售") || lower.contains("sales")) secondary = "sales";
        }
        if (secondary == null || secondary.equals(primaryTable)) return null;
        return secondary;
    }

    /** Detect a "最近 N 天/周/月" pattern and return the SQL WHERE fragment. */
    String detectTimeFilter(String query) {
        Pattern p = Pattern.compile("最近\\s*(\\d+)\\s*(天|日|周|月|年|hour|day|week|month|year)s?", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(query);
        if (m.find()) {
            int n = Integer.parseInt(m.group(1));
            String unit = m.group(2).toLowerCase(Locale.ROOT);
            return switch (unit) {
                case "天", "日", "day" -> "created_at >= DATE_SUB(NOW(), INTERVAL " + n + " DAY)";
                case "周", "week" -> "created_at >= DATE_SUB(NOW(), INTERVAL " + n + " WEEK)";
                case "月", "month" -> "created_at >= DATE_SUB(NOW(), INTERVAL " + n + " MONTH)";
                case "年", "year" -> "created_at >= DATE_SUB(NOW(), INTERVAL " + n + " YEAR)";
                case "hour" -> "created_at >= DATE_SUB(NOW(), INTERVAL " + n + " HOUR)";
                default -> "created_at >= DATE_SUB(NOW(), INTERVAL " + n + " DAY)";
            };
        }
        return null;
    }

    Integer extractTopN(String query) {
        Matcher m = Pattern.compile("(?:top|前)\\s*(\\d+)", Pattern.CASE_INSENSITIVE).matcher(query);
        if (m.find()) return Integer.parseInt(m.group(1));
        return null;
    }

    /** Try to extract a time-range predicate from a previous SQL, e.g. "created_at >= DATE_SUB(...)". */
    String extractTimeRangeHint(String priorSql) {
        if (priorSql == null) return null;
        Matcher m = Pattern.compile("(created_at\\s*>=\\s*DATE_SUB\\(NOW\\([^)]*\\),\\s*INTERVAL\\s*\\d+\\s*(?:DAY|WEEK|MONTH|YEAR|HOUR)\\))",
                Pattern.CASE_INSENSITIVE).matcher(priorSql);
        return m.find() ? m.group(1) : null;
    }

    // ---- regex constants for coreference resolution ----
    private static final Pattern RE_PREVIOUS_TABLE = Pattern.compile("(上[一]?次|刚才|先前)(的)?\\s*(表|查询|那张表|那张数据)");
    private static final Pattern RE_THAT_ONE = Pattern.compile("(那个|这|该|上述|那张)\\s*(表|数据|查询)?");
    private static final Pattern RE_IT_PRONOUN = Pattern.compile("(它|其)(的)?\\s*(数据|表|记录)?");
    private static final Pattern RE_PREVIOUS_TIME = Pattern.compile("(上[一]?次|刚才|先前)\\s*(的)?\\s*(\\d+\\s*(?:天|周|月|年|日))?(时间|范围|条件|时段)?");
}
