package org.scoooting.entities;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("user_favorite_transports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFavoriteTransport {

    @Id
    private Long id;

    @NotNull
    private Long userId; // FK to users

    @NotNull
    private Long transportId; // FK to transports

    @NotNull
    @PastOrPresent
    private LocalDateTime addedAt;

    @Size(max = 255, message = "Note cannot exceed 255 characters")
    private String note;
}
