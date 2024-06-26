package com.adedev.batchingauto.config;

import com.adedev.batchingauto.model.StudentCSV;
import com.adedev.batchingauto.model.StudentJDBC;
import com.adedev.batchingauto.model.StudentJSON;
import com.adedev.batchingauto.model.StudentResponse;
import com.adedev.batchingauto.service.StudentService;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.adapter.ItemWriterAdapter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.JsonFileItemWriter;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.sql.DataSource;
import java.util.Date;

@Configuration
public class ItemWriterConfig {
    @Qualifier("universityDataSource")
    private final DataSource universityDataSource;
    private final StudentService studentService;

    public ItemWriterConfig(DataSource universityDataSource, StudentService studentService) {
        this.universityDataSource = universityDataSource;
        this.studentService = studentService;
    }

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
    public JsonFileItemWriter<StudentJSON> jsonFileItemWriter(
            @Value("#{jobParameters['outputFile']}") FileSystemResource fileSystemResource) {
        JsonFileItemWriter<StudentJSON> jsonFileItemWriter = new JsonFileItemWriter<>(
                fileSystemResource, new JacksonJsonObjectMarshaller<>());
        return jsonFileItemWriter;
    }

    @Bean
    @StepScope
    public StaxEventItemWriter<StudentJDBC> staxEventItemWriter(
            @Value("#{jobParameters['outputFile']}") FileSystemResource fileSystemResource){
        StaxEventItemWriter<StudentJDBC> staxEventItemWriter = new StaxEventItemWriter<>();
        staxEventItemWriter.setResource(fileSystemResource);

        staxEventItemWriter.setRootTagName("students");

        Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
        jaxb2Marshaller.setClassesToBeBound(StudentJDBC.class);
        staxEventItemWriter.setMarshaller(jaxb2Marshaller);

        return staxEventItemWriter;
    }

    @Bean
    public JdbcBatchItemWriter<StudentCSV> jdbcBatchItemWriter() {
        JdbcBatchItemWriter<StudentCSV> jdbcBatchItemWriter = new JdbcBatchItemWriter<>();
        jdbcBatchItemWriter.setDataSource(universityDataSource);
        jdbcBatchItemWriter.setSql("INSERT INTO university.student(id, first_name, last_name, email) " +
                "VALUES (?,?,?,?)");
        jdbcBatchItemWriter.setItemPreparedStatementSetter(
                ((item, ps) -> { ps.setLong(1, item.getId());
                ps.setString(2, item.getFirstName());
                ps.setString(3, item.getLastName());
                ps.setString(4, item.getEmail());})
        );
        return jdbcBatchItemWriter;
    }
    @Bean

    public ItemWriterAdapter<StudentCSV> itemWriterAdapter() {
        ItemWriterAdapter<StudentCSV> itemWriterAdapter = new ItemWriterAdapter<>();

        itemWriterAdapter.setTargetObject(studentService);
        itemWriterAdapter.setTargetMethod("restCallToCreateStudent");

        return itemWriterAdapter;
    }
}
