package com.adedev.batchingauto.config;

import com.adedev.batchingauto.model.StudentJDBC;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.JsonFileItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import java.util.Date;

@Configuration
public class ItemWriterConfig {

    @Bean
    @StepScope
    public static FlatFileItemWriter<StudentJDBC> flatFileItemWriter(
            @Value("#{jobParameters['outputFile']}") FileSystemResource fileSystemResource) {
        FlatFileItemWriter<StudentJDBC> flatFileItemWriter = new FlatFileItemWriter<>();
        flatFileItemWriter.setResource(fileSystemResource);

        flatFileItemWriter.setHeaderCallback(writer -> writer.write("Id,First Name,Last Name, Email"));

        DelimitedLineAggregator<StudentJDBC> delimitedLineAggregator = new DelimitedLineAggregator();
        BeanWrapperFieldExtractor<StudentJDBC> beanWrapperFieldExtractor = new BeanWrapperFieldExtractor<>();
        beanWrapperFieldExtractor.setNames(new String[]{"id", "firstName", "lastName", "email"});
        delimitedLineAggregator.setFieldExtractor(beanWrapperFieldExtractor);
        flatFileItemWriter.setLineAggregator(delimitedLineAggregator);

        flatFileItemWriter.setFooterCallback(footer -> footer.write("Created at " + new Date()));

        return flatFileItemWriter;
    }

    @Bean
    @StepScope
    public JsonFileItemWriter<StudentJDBC> jsonFileItemWriter(
            @Value("#{jobParameters['outputFile']}") FileSystemResource fileSystemResource) {
        JsonFileItemWriter<StudentJDBC> jsonFileItemWriter = new JsonFileItemWriter<>(
                fileSystemResource, new JacksonJsonObjectMarshaller<>());
        return jsonFileItemWriter;
    }
}
