package com.adedev.batchingauto.config;

import com.adedev.batchingauto.model.StudentCSV;
import com.adedev.batchingauto.model.StudentJSON;
import com.adedev.batchingauto.model.StudentXML;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.File;

@Configuration
public class SampleJob {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final FirstJobReader jobReader;
    private final FirstJobProcessor jobProcessor;
    private final FirstJobWriter jobWriter;

    public SampleJob(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                     FirstJobReader jobReader, FirstJobProcessor jobProcessor, FirstJobWriter jobWriter) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.jobReader = jobReader;
        this.jobProcessor = jobProcessor;
        this.jobWriter = jobWriter;
    }

    @Bean
    public Job firstChunkJob() {
        return new JobBuilder("Chunk Job", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(firstChunkStep())
                .build();
    }

    public Step firstChunkStep() {
        return new StepBuilder("First Chunk Step", jobRepository)
                .<StudentXML, StudentXML> chunk(3, transactionManager)
//                .reader(flatFileItemReader(null))
//                .reader(jsonItemReader(null))
                .reader(staxEventItemReader(null))
//                .processor(jobProcessor)
                .writer(jobWriter)
                .build();

    }

    @Bean
    @StepScope
    public FlatFileItemReader<StudentCSV> flatFileItemReader(
            @Value("#{jobParameters['inputFile']}") FileSystemResource fileSystemResource) {

        FlatFileItemReader<StudentCSV> flatFileItemReader = new FlatFileItemReader<>();
        flatFileItemReader.setResource(fileSystemResource);

        DefaultLineMapper mapper = new DefaultLineMapper();

        DelimitedLineTokenizer delimitedLineTokenizer = new DelimitedLineTokenizer();
        delimitedLineTokenizer.setNames("ID", "First Name", "Last Name", "Email");
        mapper.setLineTokenizer(delimitedLineTokenizer);

        BeanWrapperFieldSetMapper<StudentCSV> wrapperFieldSetMapper = new BeanWrapperFieldSetMapper<>();
        wrapperFieldSetMapper.setTargetType(StudentCSV.class);
        mapper.setFieldSetMapper(wrapperFieldSetMapper);

        flatFileItemReader.setLineMapper(mapper);
        flatFileItemReader.setLinesToSkip(1);

        return flatFileItemReader;
    }

    @Bean
    @StepScope
    public JsonItemReader<StudentJSON> jsonItemReader(
            @Value("#{jobParameters['inputFile']}") FileSystemResource fileSystemResource) {
        JsonItemReader<StudentJSON> jsonItemReader = new JsonItemReader<>();
        jsonItemReader.setResource(fileSystemResource);
        jsonItemReader.setJsonObjectReader(new JacksonJsonObjectReader<>(StudentJSON.class));
        jsonItemReader.setCurrentItemCount(2);
        jsonItemReader.setMaxItemCount(8);
        return jsonItemReader;
    }

    @Bean
    @StepScope
    public StaxEventItemReader<StudentXML> staxEventItemReader(
            @Value("#{jobParameters['inputFile']}") FileSystemResource fileSystemResource) {
        StaxEventItemReader<StudentXML> staxEventItemReader = new StaxEventItemReader<>();
        staxEventItemReader.setResource(fileSystemResource);
        staxEventItemReader.setFragmentRootElementName("student");

        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(StudentXML.class);
        staxEventItemReader.setUnmarshaller(marshaller);

        return staxEventItemReader;
    }

}
