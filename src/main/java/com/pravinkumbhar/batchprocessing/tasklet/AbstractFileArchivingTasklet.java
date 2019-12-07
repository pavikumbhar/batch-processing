package com.pravinkumbhar.batchprocessing.tasklet;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.batch.core.step.tasklet.Tasklet;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractFileArchivingTasklet implements Tasklet {
    
    protected abstract String archiveDirectoryPath();
    
    private File archiveDirectory() {
        return new File(archiveDirectoryPath());
    }
    
    protected void archiveFile(String fileName) throws IOException {
        log.info("Archiving file:  {} ", fileName);
        File file = new File(fileName);
        File targetFile = new File(archiveDirectory(), file.getName() + getSuffix());
        FileUtils.moveFile(file, targetFile);
    }

    private String getSuffix() {
        return "_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new DateTime(DateTimeZone.UTC).toDate());
    }
}
