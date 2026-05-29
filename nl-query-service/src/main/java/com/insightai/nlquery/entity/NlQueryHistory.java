package com.insightai.nlquery.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("nl_query_history")
public class NlQueryHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String originalQuery;
    private String generatedSql;
    private String intent;
    private String dataSource;
    private String tableName;
    private String queryStatus;
    private String errorMessage;
    private String createdBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}