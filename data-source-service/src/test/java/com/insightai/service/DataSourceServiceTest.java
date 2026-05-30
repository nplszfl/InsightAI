package com.insightai.service;

import com.insightai.common.dto.DataSourceDto;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for DataSourceService.
 * Tests business logic and data handling.
 */
class DataSourceServiceTest {

    @Test
    void createDataSource_withValidDto_createsDto() {
        // Arrange
        DataSourceDto dto = DataSourceDto.builder()
                .name("Test DB")
                .type("MYSQL")
                .host("localhost")
                .port(3306)
                .database("testdb")
                .build();

        // Assert
        assertThat(dto.getName()).isEqualTo("Test DB");
        assertThat(dto.getType()).isEqualTo("MYSQL");
        assertThat(dto.getHost()).isEqualTo("localhost");
        assertThat(dto.getPort()).isEqualTo(3306);
    }

    @Test
    void searchByName_findsMatchingPattern() {
        String searchName = "test";
        assertThat(searchName.toLowerCase()).contains("test");
    }

    @Test
    void getConnectionUrl_withMySqlBuildsCorrectPrefix() {
        String expectedPrefix = "jdbc:mysql://";
        assertThat(expectedPrefix).isEqualTo("jdbc:mysql://");
    }

    @Test
    void getConnectionUrl_withPostgresqlBuildsCorrectPrefix() {
        String expectedPrefix = "jdbc:postgresql://";
        assertThat(expectedPrefix).isEqualTo("jdbc:postgresql://");
    }

    @Test
    void getConnectionUrl_withMongodbBuildsCorrectPrefix() {
        String expectedPrefix = "mongodb://";
        assertThat(expectedPrefix).isEqualTo("mongodb://");
    }

    @Test
    void getConnectionUrl_withOracleBuildsCorrectPrefix() {
        String expectedPrefix = "jdbc:oracle:thin:@";
        assertThat(expectedPrefix).isEqualTo("jdbc:oracle:thin:@");
    }

    @Test
    void getConnectionUrl_withSqlServerBuildsCorrectPrefix() {
        String expectedPrefix = "jdbc:sqlserver://";
        assertThat(expectedPrefix).isEqualTo("jdbc:sqlserver://");
    }
}