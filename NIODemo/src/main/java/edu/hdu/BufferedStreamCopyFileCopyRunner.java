package edu.hdu;


import java.io.*;

/**
 * Description:
 */
public class BufferedStreamCopyFileCopyRunner implements FileCopyRunner {
    @Override
    public void copyFile(File source, File target) {
        try (BufferedInputStream bfin = new BufferedInputStream(new FileInputStream(source));
             BufferedOutputStream bfout = new BufferedOutputStream(new FileOutputStream(target))){

            byte[] buffer = new byte[1024];
            int num;
            while ((num = bfin.read(buffer)) != -1){
                bfout.write(buffer, 0, num);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
