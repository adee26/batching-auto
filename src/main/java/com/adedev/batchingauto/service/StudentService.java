package com.adedev.batchingauto.service;

import com.adedev.batchingauto.model.StudentResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class StudentService {

    List<StudentResponse> list;

    public List<StudentResponse> restCallToGetStudents() {
        RestTemplate restTemplate = new RestTemplate();
        StudentResponse[] studentResponses =
                restTemplate.getForObject("http://localhost:8081/api/v1/students", StudentResponse[].class);

        list = new ArrayList<>();

        if(studentResponses != null) {
            Collections.addAll(list, studentResponses);
        }

        return list;
    }

    public StudentResponse getStudent(long id, String name) {
        System.out.println("id = " + id + " and name = " + name);
        if(list == null) {
            restCallToGetStudents();
        }

        if(list != null && !list.isEmpty()) {
            return list.remove(0);
        }

        return null;
    }
}
