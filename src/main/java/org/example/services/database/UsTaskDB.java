package org.example.services.database;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "ustask")
@Data
public class UsTaskDB {

    @Id
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "num", nullable = false)
    private int num;

    @Column(name = "controlnum", nullable = false)
    private int controlNum;
}
