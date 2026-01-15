package org.scoooting.user.adapters.persistence.entities;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("refresh_tokens")
@Data
public class RefreshTokenEntity {

    @Id
    @Column("user_id")
    private Long userId;

    private String token;
}
