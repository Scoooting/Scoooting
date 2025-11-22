package org.scoooting.analytics.entities;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Table("rental_daily_stats")
@Data
@NoArgsConstructor
public class RentalDailyStats {

    @Id
    private LocalDate date;

    @Column("total_starts")
    private Integer totalStarts;

    @Column("total_starts")
    private Integer totalEnds;

    @Column("total_starts")
    private Integer totalCancels;

    @Column("average_duration")
    private Float averageDuration;

    @Column("average_distance")
    private Float averageDistance;

    @Column("min_duration")
    private Float minDuration;

    @Column("max_duration")
    private Float maxDuration;

    @Column("min_distance")
    private Float minDistance;

    @Column("max_distance")
    private Float maxDistance;

    public RentalDailyStats(LocalDate date) {
        this.date = date;
    }
}
