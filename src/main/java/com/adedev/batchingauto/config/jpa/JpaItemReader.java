package com.adedev.batchingauto.config.jpa;

import com.adedev.batchingauto.entity.postgres.StudentPostgres;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JpaItemReader {
    private final EntityManagerFactory postgreSqlEntityManagerFactory;

    public JpaItemReader(@Qualifier("postgresqlEntityManagerFactory") EntityManagerFactory postgreSqlEntityManagerFactory) {
        this.postgreSqlEntityManagerFactory = postgreSqlEntityManagerFactory;
    }

    @Bean
    public JpaCursorItemReader<StudentPostgres> jpaPostgresCursorItemReader() {
        JpaCursorItemReader<StudentPostgres> jpaCursorItemReader = new JpaCursorItemReader<>();

        jpaCursorItemReader.setEntityManagerFactory(postgreSqlEntityManagerFactory);
        jpaCursorItemReader.setQueryString("FROM StudentPostgres");

        return jpaCursorItemReader;
    }
}
