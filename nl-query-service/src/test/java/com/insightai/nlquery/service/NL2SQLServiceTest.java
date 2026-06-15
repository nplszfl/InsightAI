package com.insightai.nlquery.service;

import com.insightai.nlquery.dto.ConversationContextDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the rule-based NL2SQL engine.
 *
 * <p>Coverage matrix:
 * <ul>
 *     <li>6 core NL→SQL patterns (sales / orders / users / count / top-N / time-range)</li>
 *     <li>JOIN detection</li>
 *     <li>Empty / null guard</li>
 * </ul>
 */
class NL2SQLServiceTest {

    private NL2SQLService nl2sql;

    @BeforeEach
    void setUp() {
        nl2sql = new NL2SQLService();
    }

    // ---------- 6 core NL2SQL patterns ----------

    @Test
    @DisplayName("sales keyword → FROM sales")
    void testParseQuery_SalesMapsToSalesTable() {
        String sql = nl2sql.parseQuery("查一下所有销售", List.of());
        assertTrue(sql.toUpperCase().contains("FROM SALES"),
                "expected FROM SALES, got: " + sql);
    }

    @Test
    @DisplayName("orders keyword → FROM orders")
    void testParseQuery_OrdersMapsToOrdersTable() {
        String sql = nl2sql.parseQuery("show all orders today", List.of());
        assertTrue(sql.toUpperCase().contains("FROM ORDERS"),
                "expected FROM ORDERS, got: " + sql);
    }

    @Test
    @DisplayName("users/客户 keyword → FROM users")
    void testParseQuery_UsersMapsToUsersTable() {
        String sql = nl2sql.parseQuery("查询最近的用户", List.of());
        assertTrue(sql.toUpperCase().contains("FROM USERS"),
                "expected FROM USERS, got: " + sql);
    }

    @Test
    @DisplayName("总数 → SELECT COUNT(*)")
    void testParseQuery_CountKeywordProducesCountStar() {
        String sql = nl2sql.parseQuery("订单总数是多少", List.of());
        assertTrue(sql.toUpperCase().contains("SELECT COUNT(*)"),
                "expected SELECT COUNT(*), got: " + sql);
    }

    @Test
    @DisplayName("Top N → ORDER BY ... LIMIT N")
    void testParseQuery_TopNProducesLimit() {
        String sql = nl2sql.parseQuery("前 5 销售", List.of());
        assertTrue(sql.toUpperCase().contains("LIMIT 5"),
                "expected LIMIT 5, got: " + sql);
        assertTrue(sql.toUpperCase().contains("ORDER BY"),
                "expected ORDER BY for top-N, got: " + sql);
    }

    @Test
    @DisplayName("最近 N 天 → WHERE DATE_SUB clause")
    void testParseQuery_RecentNDaysProducesWhereClause() {
        String sql = nl2sql.parseQuery("最近 7 天的销售", List.of());
        assertTrue(sql.toUpperCase().contains("WHERE"),
                "expected WHERE, got: " + sql);
        assertTrue(sql.toUpperCase().contains("DATE_SUB"),
                "expected DATE_SUB for relative date, got: " + sql);
        assertTrue(sql.toUpperCase().contains("INTERVAL 7 DAY"),
                "expected INTERVAL 7 DAY, got: " + sql);
    }

    // ---------- additional rule coverage ----------

    @Test
    @DisplayName("和/与 conjunction → JOIN")
    void testParseQuery_AndProducesJoin() {
        String sql = nl2sql.parseQuery("销售和用户", List.of());
        assertTrue(sql.toUpperCase().contains(" JOIN "),
                "expected JOIN, got: " + sql);
    }

    @Test
    @DisplayName("product keyword → FROM products")
    void testParseQuery_ProductsMapsToProductsTable() {
        String sql = nl2sql.parseQuery("top products", List.of());
        assertTrue(sql.toUpperCase().contains("FROM PRODUCTS"),
                "expected FROM PRODUCTS, got: " + sql);
    }

    @Test
    @DisplayName("平均 keyword → SELECT AVG")
    void testParseQuery_AverageProducesAvg() {
        String sql = nl2sql.parseQuery("最近 30 天的平均销售", List.of());
        assertTrue(sql.toUpperCase().contains("AVG("),
                "expected AVG(..., got: " + sql);
    }

    @Test
    @DisplayName("空查询 → IllegalArgumentException")
    void testParseQuery_EmptyThrows() {
        assertThrows(IllegalArgumentException.class, () -> nl2sql.parseQuery("", List.of()));
        assertThrows(IllegalArgumentException.class, () -> nl2sql.parseQuery(null, List.of()));
    }

    @Test
    @DisplayName("未知关键字回退到默认 data_table")
    void testParseQuery_UnknownKeywordFallsBack() {
        String sql = nl2sql.parseQuery("随机的东西", List.of());
        assertTrue(sql.toUpperCase().contains("FROM DATA_TABLE"),
                "expected fallback FROM DATA_TABLE, got: " + sql);
    }
}
