package com.adedev.batchingauto.config;

import com.adedev.batchingauto.config.jpa.JpaItemProcessor;
import com.adedev.batchingauto.entity.postgres.StudentPostgres;
import com.adedev.batchingauto.entity.sql.StudentMySql;
import com.adedev.batchingauto.listener.SkipListenerImpl;
import com.adedev.batchingauto.model.StudentCSV;
import com.adedev.batchingauto.model.StudentJDBC;
import com.adedev.batchingauto.model.StudentJSON;
import com.adedev.batchingauto.model.StudentResponse;
import com.adedev.batchingauto.model.StudentXML;
import com.adedev.batchingauto.service.StudentService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.adapter.ItemReaderAdapter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class SampleJob {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final StudentService studentService;
    @Qualifier("universityDataSource")
    private final DataSource universityDataSource;
    private final SkipListenerImpl skipListener;
    private final JpaCursorItemReader<StudentPostgres> jpaPostgresCursorItemReader;
    private final JpaItemProcessor jpaItemProcessor;
    private final JpaItemWriter<StudentMySql> mySqlJpaItemWriter;
    private final JpaTransactionManager jpaTransactionManager;

    public SampleJob(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                     StudentService studentService, DataSource universityDataSource,
                     SkipListenerImpl skipListener,
                     @Qualifier("jpaPostgresCursorItemReader") JpaCursorItemReader<StudentPostgres> jpaPostgresCursorItemReader,
                     JpaItemProcessor jpaItemProcessor,
                     JpaItemWriter<StudentMySql> mySqlJpaItemWriter,
                     JpaTransactionManager jpaTransactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.studentService = studentService;
        this.universityDataSource = universityDataSource;
        this.skipListener = skipListener;
        this.jpaPostgresCursorItemReader = jpaPostgresCursorItemReader;
        this.jpaItemProcessor = jpaItemProcessor;
        this.mySqlJpaItemWriter = mySqlJpaItemWriter;
        this.jpaTransactionManager = jpaTransactionManager;
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
                .<StudentPostgres, StudentMySql> chunk(3, transactionManager)
                .reader(jpaPostgresCursorItemReader)
                .processor(jpaItemProcessor)
                .writer(mySqlJpaItemWriter)
                .transactionManager(jpaTransactionManager)
                .faultTolerant()
                .skip(FlatFileParseException.class)
//                .skipLimit(Integer.MAX_VALUE)
//                .skipPolicy(new AlwaysSkipItemSkipPolicy())
                .listener(skipListener)
                .retryLimit(2)
                .retry(Throwable.class)
//                .writer(jobWriter)
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

    public JdbcCursorItemReader<StudentJDBC> jdbcJdbcCursorItemReader() {
        JdbcCursorItemReader<StudentJDBC> jdbcJdbcCursorItemReader = new JdbcCursorItemReader<>();
        jdbcJdbcCursorItemReader.setDataSource(universityDataSource);
        jdbcJdbcCursorItemReader.setSql(
                "SELECT id, first_name, last_name, email FROM university.student");

        BeanPropertyRowMapper<StudentJDBC> beanPropertyRowMapper = new BeanPropertyRowMapper<>();
        beanPropertyRowMapper.setMappedClass(StudentJDBC.class);
        jdbcJdbcCursorItemReader.setRowMapper(beanPropertyRowMapper);

//        jdbcJdbcCursorItemReader.setCurrentItemCount(2);
//        jdbcJdbcCursorItemReader.setMaxItemCount(8);

        return jdbcJdbcCursorItemReader;
    }

    public ItemReaderAdapter<StudentResponse> itemReaderAdapter() {
        ItemReaderAdapter<StudentResponse> itemReaderAdapter = new ItemReaderAdapter<>();
        itemReaderAdapter.setTargetObject(studentService);
        itemReaderAdapter.setTargetMethod("getStudent");
        itemReaderAdapter.setArguments(new Object[] {1L, "Test"});

        return itemReaderAdapter;
    }

}
