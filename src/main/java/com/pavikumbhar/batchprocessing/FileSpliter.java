package com.pavikumbhar.batchprocessing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class FileSpliter {
    
    /**
     *
     * @param fileName
     * @param maxRows
     * @param header
     * @param footer
     * @param targetDir
     * @throws IOException
     */
    public static void splitTextFiles(String fileName, int maxRows, String header, String footer, String targetDir) throws IOException {
        File bigFile = new File(fileName);
        int i = 1;
        String ext = fileName.substring(fileName.lastIndexOf('.'));
        String fileNoExt = bigFile.getName().replace(ext, "");
        File newDir = null;
        if (targetDir != null) {
            newDir = new File(targetDir);
        } else {
            newDir = new File(bigFile.getParent() + File.separator + fileNoExt + "_split");
        }
        newDir.mkdirs();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(fileName))) {
            String line = null;
            int lineNum = 1;
            String splitedFileName = "";
            Path splitFile = Paths.get(newDir.getPath() + File.separator + fileNoExt + "_" + String.format("%02d", i) + ext);
            BufferedWriter writer = Files.newBufferedWriter(splitFile, StandardOpenOption.CREATE);
            while ((line = reader.readLine()) != null) {
                if (lineNum == 1) {
                    splitedFileName = splitFile.toString();
                    if (isFooterNotEmpty(header)) {
                        writer.append(header);
                        writer.newLine();
                    }
                }
                writer.append(line);

                if (lineNum >= maxRows) {
                    if (isFooterNotEmpty(footer)) {
                        writer.newLine();
                        writer.append(footer);
                    }
                    writer.close();
                    log.debug("new file created : {} lines written to file : {} ", splitedFileName, lineNum);
                    lineNum = 1;
                    i++;
                    splitFile = Paths.get(newDir.getPath() + File.separator + fileNoExt + "_" + String.format("%02d", i) + ext);
                    writer = Files.newBufferedWriter(splitFile, StandardOpenOption.CREATE);
                } else {
                    writer.newLine();
                    lineNum++;
                }
            }
            if (lineNum <= maxRows && isFooterNotEmpty(footer)) {// early exit
                writer.newLine();
                lineNum++;
                writer.append(footer);
            }
            writer.close();
            log.debug(" new file created : {} lines written to file : {} ", splitedFileName, lineNum);
        }

        log.debug("file '" + bigFile.getName() + "' split into " + i + " files");
    }
    
    /**
     *
     * @param footer
     * @return
     */
    private static boolean isFooterNotEmpty(String footer) {
        return footer != null && footer.length() > 0;
    }
}