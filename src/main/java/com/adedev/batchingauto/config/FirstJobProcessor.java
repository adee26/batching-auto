package com.adedev.batchingauto.config;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class FirstJobProcessor implements ItemProcessor<Integer, Long> {
    @Override
    public Long process(Integer item) throws Exception {
        System.out.println("Inside Item Processor");
        return (long) (item + 20);
    }
}
