package edu.hdu;

import java.io.*;

/**
 * Description:
 */
public class NoBufferedStreamFileCopyRunner implements FileCopyRunner {
    @Override
    public void copyFile(File source, File target) {
        try(FileInputStream fin = new FileInputStream(source);
            FileOutputStream fout = new FileOutputStream(target)) {

            int content;
            //这段看不懂的话去看FileInputStream这个类的read()返回值的含义
            while ((content = fin.read()) != -1){
                fout.write(content);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
