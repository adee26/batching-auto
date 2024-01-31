package com.adedev.batchingauto.config.jpa;

import com.adedev.batchingauto.entity.postgres.StudentPostgres;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

@Configuration
public class JpaItemReader {
    private final EntityManagerFactory postgreSqlEntityManagerFactory;

    public JpaItemReader(@Qualifier("postgresqlEntityManagerFactory") EntityManagerFactory postgreSqlEntityManagerFactory) {
        this.postgreSqlEntityManagerFactory = postgreSqlEntityManagerFactory;
    }

    @Bean
    @StepScope
    public JpaCursorItemReader<StudentPostgres> jpaPostgresCursorItemReader(
            @Value("#{jobParameters['currentItemCount']}") Integer currentItemCount,
            @Value("#{jobParameters['maxItemCount']}") Integer maxItemCount) {
        JpaCursorItemReader<StudentPostgres> jpaCursorItemReader = new JpaCursorItemReader<>();

        jpaCursorItemReader.setEntityManagerFactory(postgreSqlEntityManagerFactory);
        jpaCursorItemReader.setQueryString("FROM StudentPostgres");

        jpaCursorItemReader.setCurrentItemCount(currentItemCount);
        jpaCursorItemReader.setMaxItemCount(maxItemCount);

        return jpaCursorItemReader;
    }
}
