-- V1: Multi-turn conversation context for NL2SQL.
-- Stores prior turns so subsequent queries can resolve pronouns like
-- "上次的表" / "它" / "那个" to concrete identifiers.

CREATE TABLE IF NOT EXISTS conversation_context (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    session_id      VARCHAR(128) NOT NULL                COMMENT 'Session grouping identifier',
    user_id         VARCHAR(128)                         COMMENT 'User that owns the turn',
    query_text      TEXT         NOT NULL                COMMENT 'Original NL query as supplied by the user',
    generated_sql   TEXT                                COMMENT 'SQL produced by NL2SQL engine (nullable on failure)',
    intent          VARCHAR(64)                          COMMENT 'Detected intent (AGGREGATION, TREND_ANALYSIS, ...)',
    table_name      VARCHAR(128)                         COMMENT 'Primary table referenced, for downstream coreference',
    executed        TINYINT(1)   NOT NULL DEFAULT 0      COMMENT 'Whether the generated SQL was executed',
    row_count       INT                                 COMMENT 'Rows returned by execution',
    error_message   TEXT                                COMMENT 'Error message when generation/execution failed',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Insert timestamp',
    PRIMARY KEY (id),
    KEY idx_conv_session_created (session_id, created_at),
    KEY idx_conv_session_user    (session_id, user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'Multi-turn conversation context for NL2SQL coreference resolution';
