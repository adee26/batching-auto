package com.adedev.batchingauto.config.jpa;

import com.adedev.batchingauto.entity.sql.StudentMySql;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JpaSqlItemWriter {
    private final EntityManagerFactory entityManagerFactory;

    public JpaSqlItemWriter(@Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Bean
    public JpaItemWriter<StudentMySql> mySqlJpaItemWriter() {
        JpaItemWriter<StudentMySql> jpaSqlItemWriter = new JpaItemWriter();
        jpaSqlItemWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaSqlItemWriter;
    }
}
