package com.adedev.batchingauto.config;

import com.adedev.batchingauto.model.StudentCSV;
import com.adedev.batchingauto.model.StudentJDBC;
import com.adedev.batchingauto.model.StudentJSON;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class FirstJobProcessor implements ItemProcessor<StudentCSV, StudentJSON> {
    @Override
    public StudentJSON process(StudentCSV item) {
        System.out.println("Inside Item Processor");

        if(item.getId() == 6) {
            System.out.println("Inside JsonFileItemProcessor");
            throw new NullPointerException();
        }

        StudentJSON studentJSON = new StudentJSON();
        studentJSON.setId(item.getId());
        studentJSON.setFirstName(item.getFirstName());
        studentJSON.setLastName(item.getLastName());
        studentJSON.setEmail(item.getLastName());

        return studentJSON;
    }
}
