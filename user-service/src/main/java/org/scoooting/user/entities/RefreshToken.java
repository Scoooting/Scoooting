package org.scoooting.user.entities;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("refresh_tokens")
@Data
public class RefreshToken {

    @Id
    @Column("user_id")
    private Long userId;

    private String token;
}
