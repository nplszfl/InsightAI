package com.insightai.dashboard.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Dashboard entity. A dashboard is a container of widgets owned by a user,
 * with an access visibility scope (private, team, organization, public).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("dashboard")
public class Dashboard {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String description;
    private String ownerId;
    private String visibility;   // PRIVATE, TEAM, ORGANIZATION, PUBLIC
    private String category;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
