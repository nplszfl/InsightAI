package com.insightai.common.dto;

import lombok.*;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceDto {
    private Long id;
    private String name;
    private String type;
    private String host;
    private Integer port;
    private String database;
    private String username;
    private String password;
    private String config;
    private Integer status;
}
