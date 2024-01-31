package com.adedev.batchingauto.config.jpa;

import com.adedev.batchingauto.entity.postgres.StudentPostgres;
import com.adedev.batchingauto.entity.sql.StudentMySql;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class JpaItemProcessor implements ItemProcessor<StudentPostgres, StudentMySql> {
    @Override
    public StudentMySql process(StudentPostgres item) {
        System.out.println(item.getId());

        StudentMySql studentMySql = new StudentMySql();
        studentMySql.setId(item.getId());
        studentMySql.setFirstName(item.getFirstName());
        studentMySql.setLastName(item.getLastName());
        studentMySql.setEmail(item.getEmail());
        studentMySql.setDeptId(item.getDeptId());
        studentMySql.setIsActive(item.getIsActive() != null && Boolean.parseBoolean(item.getIsActive()));

        return studentMySql;
    }
}
