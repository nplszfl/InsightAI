package com.insightai.nlquery.service;

import com.insightai.nlquery.dto.ConversationContextDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for coreference / anaphora resolution in {@link NL2SQLService#resolveReferences(String, List)}.
 *
 * <p>Coverage matrix:
 * <ul>
 *     <li>"上次的表" → previous table name</li>
 *     <li>"那个表" / "该" → previous table name</li>
 *     <li>"它" / "其" → previous table name</li>
 *     <li>"上次的 7 天" → previous time range preserved</li>
 *     <li>empty context → query unchanged</li>
 *     <li>null inputs → graceful no-op</li>
 * </ul>
 */
class ResolveReferencesTest {

    private NL2SQLService nl2sql;

    @BeforeEach
    void setUp() {
        nl2sql = new NL2SQLService();
    }

    private ConversationContextDto prior(String tableName, String generatedSql) {
        return ConversationContextDto.builder()
                .sessionId("sess-1")
                .userId("u1")
                .tableName(tableName)
                .generatedSql(generatedSql)
                .build();
    }

    @Test
    @DisplayName("\"上次的表\" 替换为上一个表名")
    void testResolve_PreviousTableToken() {
        List<ConversationContextDto> ctx = List.of(prior("sales", "SELECT * FROM sales"));
        String resolved = nl2sql.resolveReferences("上次那张表的总和", ctx);
        assertTrue(resolved.contains("sales"),
                "expected 'sales' in resolved, got: " + resolved);
        assertFalse(resolved.contains("上次"),
                "previous-table phrase should be replaced, got: " + resolved);
    }

    @Test
    @DisplayName("\"那个\" 替换为上一个表名")
    void testResolve_ThatOneDemonstrative() {
        List<ConversationContextDto> ctx = List.of(prior("orders", "SELECT * FROM orders"));
        String resolved = nl2sql.resolveReferences("那个的总数", ctx);
        assertTrue(resolved.contains("orders"),
                "expected 'orders' in resolved, got: " + resolved);
    }

    @Test
    @DisplayName("\"它\" 替换为上一个表名")
    void testResolve_ItPronoun() {
        List<ConversationContextDto> ctx = List.of(prior("users", "SELECT * FROM users"));
        String resolved = nl2sql.resolveReferences("它的记录", ctx);
        assertTrue(resolved.contains("users"),
                "expected 'users' in resolved, got: " + resolved);
    }

    @Test
    @DisplayName("\"上次的时间\" 保留上一次的时间范围")
    void testResolve_PreviousTimeRange() {
        String priorSql = "SELECT * FROM sales WHERE created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)";
        List<ConversationContextDto> ctx = List.of(prior("sales", priorSql));
        String resolved = nl2sql.resolveReferences("上次的时间范围内的总和", ctx);
        assertTrue(resolved.toUpperCase().contains("DATE_SUB"),
                "expected prior DATE_SUB fragment preserved, got: " + resolved);
        assertTrue(resolved.toUpperCase().contains("INTERVAL 7 DAY"),
                "expected prior INTERVAL 7 DAY preserved, got: " + resolved);
    }

    @Test
    @DisplayName("空上下文 → 原样返回")
    void testResolve_EmptyContextReturnsQueryUnchanged() {
        String q = "它的数据";
        String resolved = nl2sql.resolveReferences(q, List.of());
        assertEquals(q, resolved);
    }

    @Test
    @DisplayName("null 输入 → 空字符串, 不抛异常")
    void testResolve_NullInputsAreSafe() {
        assertEquals("", nl2sql.resolveReferences(null, List.of()));
        assertEquals("hello", nl2sql.resolveReferences("hello", null));
    }

    @Test
    @DisplayName("没有有效 tableName 的上下文 → 原样返回")
    void testResolve_PriorWithoutTableIsIgnored() {
        ConversationContextDto empty = ConversationContextDto.builder()
                .tableName(null)
                .generatedSql(null)
                .build();
        String q = "上次的表的总和";
        String resolved = nl2sql.resolveReferences(q, List.of(empty));
        assertEquals(q, resolved, "without a usable prior table the query must not be mutated");
    }
}
