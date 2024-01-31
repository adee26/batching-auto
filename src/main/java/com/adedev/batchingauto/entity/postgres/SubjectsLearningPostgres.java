package com.adedev.batchingauto.entity.postgres;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "subjects_learning")
@Data
public class SubjectsLearningPostgres {
    @Id
    private Long id;
    @Column(name = "sub_name")
    private String subName;
    private Long studentId;
    @Column(name = "marks_obtained")
    private Long marksObtained;
}
