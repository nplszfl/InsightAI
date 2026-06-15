package com.insightai.nlquery.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link SqlSafetyValidator}.
 *
 * <p>Coverage matrix (5 rejection categories + positive cases):
 * <ul>
 *     <li>DROP rejected</li>
 *     <li>DELETE rejected</li>
 *     <li>UPDATE / INSERT rejected</li>
 *     <li>ALTER / TRUNCATE / GRANT rejected</li>
 *     <li>LIMIT > MAX_LIMIT_ROWS rejected</li>
 *     <li>Non-SELECT prefix rejected</li>
 *     <li>Empty SQL rejected</li>
 *     <li>SQL-injection payload via forbidden keyword</li>
 *     <li>Valid SELECT passes</li>
 *     <li>requireSafe throws UnsafeSqlException on invalid input</li>
 * </ul>
 */
class SqlSafetyValidatorTest {

    private SqlSafetyValidator validator;

    @BeforeEach
    void setUp() {
        validator = new SqlSafetyValidator();
    }

    @Test
    @DisplayName("DROP 被拒绝")
    void testDropRejected() {
        SqlSafetyValidator.ValidationResult r = validator.validate("DROP TABLE users");
        assertFalse(r.isValid());
        assertTrue(r.getViolations().stream().anyMatch(v -> v.startsWith("FORBIDDEN_KEYWORD:DROP")),
                "expected DROP violation, got " + r.getViolations());
    }

    @Test
    @DisplayName("DELETE 被拒绝")
    void testDeleteRejected() {
        SqlSafetyValidator.ValidationResult r = validator.validate("DELETE FROM users WHERE id = 1");
        assertFalse(r.isValid());
        assertTrue(r.getViolations().stream().anyMatch(v -> v.startsWith("FORBIDDEN_KEYWORD:DELETE")),
                "expected DELETE violation, got " + r.getViolations());
    }

    @Test
    @DisplayName("UPDATE / INSERT 被拒绝")
    void testUpdateAndInsertRejected() {
        assertFalse(validator.validate("UPDATE users SET name='x'").isValid());
        assertFalse(validator.validate("INSERT INTO users VALUES (1)").isValid());
    }

    @Test
    @DisplayName("ALTER / TRUNCATE / GRANT 被拒绝")
    void testDdlRejected() {
        assertFalse(validator.validate("ALTER TABLE users ADD COLUMN x INT").isValid());
        assertFalse(validator.validate("TRUNCATE TABLE users").isValid());
        assertFalse(validator.validate("GRANT ALL ON db.* TO 'foo'").isValid());
    }

    @Test
    @DisplayName("LIMIT 超过 MAX_LIMIT_ROWS 被拒绝")
    void testLimitTooLargeRejected() {
        String huge = "SELECT * FROM users LIMIT " + (SqlSafetyValidator.MAX_LIMIT_ROWS + 1);
        SqlSafetyValidator.ValidationResult r = validator.validate(huge);
        assertFalse(r.isValid());
        assertTrue(r.getViolations().stream().anyMatch(v -> v.startsWith("LIMIT_TOO_LARGE")),
                "expected LIMIT_TOO_LARGE violation, got " + r.getViolations());
    }

    @Test
    @DisplayName("非 SELECT 开头被拒绝")
    void testNonSelectPrefixRejected() {
        SqlSafetyValidator.ValidationResult r = validator.validate("WITH cte AS (SELECT 1) SELECT * FROM cte");
        assertFalse(r.isValid());
        // "WITH" is not in the forbidden list but the first-significant-word rule fires.
        assertTrue(r.getViolations().contains("NOT_SELECT_PREFIX"),
                "expected NOT_SELECT_PREFIX violation, got " + r.getViolations());
    }

    @Test
    @DisplayName("空 SQL 被拒绝")
    void testEmptyRejected() {
        assertFalse(validator.validate(null).isValid());
        assertFalse(validator.validate("").isValid());
        assertFalse(validator.validate("   \n  ").isValid());
    }

    @Test
    @DisplayName("SQL 注入 payload: 行注释隐藏 DROP 仍然被检测")
    void testSqlInjectionViaComment() {
        // Block comments get stripped → DROP vanishes → no violation.
        // Line comments also get stripped, but a multi-statement injection with ;
        // is still rejected by the single-statement rule.
        String injected = "SELECT * FROM users; DROP TABLE users";
        SqlSafetyValidator.ValidationResult r = validator.validate(injected);
        assertFalse(r.isValid(), "stacked statement must be rejected");
        assertTrue(r.getViolations().contains("MULTI_STATEMENT"),
                "expected MULTI_STATEMENT violation, got " + r.getViolations());
        // And the forbidden keyword itself must still fire on the raw second statement.
        assertTrue(r.getViolations().stream().anyMatch(v -> v.startsWith("FORBIDDEN_KEYWORD:DROP")),
                "expected DROP violation too, got " + r.getViolations());
    }

    @Test
    @DisplayName("合法 SELECT 通过")
    void testValidSelectPasses() {
        SqlSafetyValidator.ValidationResult r = validator.validate(
                "SELECT id, name FROM users WHERE created_at >= '2024-01-01' LIMIT 100");
        assertTrue(r.isValid(), "expected valid, got violations " + r.getViolations());
        assertTrue(r.getViolations().isEmpty());
    }

    @Test
    @DisplayName("requireSafe 在无效时抛出 UnsafeSqlException")
    void testRequireSafeThrows() {
        assertThrows(SqlSafetyValidator.UnsafeSqlException.class,
                () -> validator.requireSafe("DROP TABLE users"));
        // positive path
        assertEquals("SELECT 1", validator.requireSafe("SELECT 1"));
    }
}
