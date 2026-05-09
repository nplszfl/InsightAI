package com.insightai.common.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("data_source")
public class DataSource {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String type; // MYSQL, POSTGRESQL, MONGODB, API
    private String host;
    private Integer port;
    private String database;
    private String username;
    private String password;
    private String config; // JSON config for additional settings
    private Integer status; // 1=active, 0=inactive
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
