package com.adedev.batchingauto.listener;

import com.adedev.batchingauto.model.StudentCSV;
import com.adedev.batchingauto.model.StudentJSON;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

@Component
public class SkipListenerImpl implements SkipListener<StudentCSV, StudentJSON> {
    @Override
    public void onSkipInRead(Throwable th) {
        if (th instanceof FlatFileParseException) {
            createFile("firstChunkStep/reader/SkipInRead.txt", ((FlatFileParseException) th).getInput());
        }
    }

    @Override
    public void onSkipInWrite(StudentJSON studentCSV, Throwable th) {
        createFile("firstChunkStep/processor/SkipInProcess.txt", studentCSV.toString());
    }

    @Override
    public void onSkipInProcess(StudentCSV studentJSON, Throwable th) {
        createFile("firstChunkStep/writer/SkipInWrite.txt", studentJSON.toString());
    }

    public void createFile(String filePath, String data) {
        try(FileWriter fileWriter = new FileWriter(filePath, true)) {
            fileWriter.write(data + new Date() + "\n");
        } catch (IOException e){

        }
    }
}
