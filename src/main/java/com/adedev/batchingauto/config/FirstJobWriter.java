package com.adedev.batchingauto.config;

import com.adedev.batchingauto.model.StudentJDBC;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
public class FirstJobWriter implements ItemWriter<StudentJDBC> {
    @Override
    public void write(Chunk<? extends StudentJDBC> chunk) throws Exception {
        System.out.println("Inside Item Writer");
        chunk.getItems().forEach(System.out::println);
    }
}
