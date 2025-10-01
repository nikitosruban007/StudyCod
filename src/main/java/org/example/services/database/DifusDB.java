package org.example.services.database;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "difus")
@Data
public class DifusDB {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "difficult", nullable = false)
    private double difficult;
}
