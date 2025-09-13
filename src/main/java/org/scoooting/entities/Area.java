package org.scoooting.entities;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("area")
@Data
public class Area {

    @Id
    public Long id;
    public String city;
    public String district;

}
