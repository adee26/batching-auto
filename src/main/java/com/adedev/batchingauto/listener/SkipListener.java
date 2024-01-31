package com.adedev.batchingauto.listener;

import com.adedev.batchingauto.model.StudentCSV;
import org.springframework.batch.core.annotation.OnSkipInProcess;
import org.springframework.batch.core.annotation.OnSkipInRead;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

@Component
public class SkipListener {
    @OnSkipInRead
    public void skipInRead(Throwable th) {
        if (th instanceof FlatFileParseException) {
            createFile("firstChunkStep/reader/SkipInRead.txt", ((FlatFileParseException) th).getInput());
        }
    }

    @OnSkipInProcess
    public void skipInProcess(StudentCSV studentCSV, Throwable th) {
        createFile("firstChunkStep/processor/SkipInProcess.txt", studentCSV.toString());
    }

    public void createFile(String filePath, String data) {
        try(FileWriter fileWriter = new FileWriter(filePath, true)) {
            fileWriter.write(data + new Date() + "\n");
        } catch (IOException e){

        }
    }
}
