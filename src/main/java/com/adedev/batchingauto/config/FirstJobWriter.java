package com.adedev.batchingauto.config;

import com.adedev.batchingauto.model.StudentCSV;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
public class FirstJobWriter implements ItemWriter<StudentCSV> {
    @Override
    public void write(Chunk<? extends StudentCSV> chunk) throws Exception {
        System.out.println("Inside Item Writer");
        chunk.getItems().forEach(System.out::println);
    }
}
