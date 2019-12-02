package edu.hdu;

import java.io.*;
import java.nio.channels.FileChannel;

/**
 * Description:
 */
public class NIOTransferFileCopyRunner implements FileCopyRunner {
    @Override
    public void copyFile(File source, File target) {

        try (FileChannel fin = new FileInputStream(source).getChannel();
            FileChannel fout = new FileOutputStream(target).getChannel()){

            long transferred = 0L;
            long size = fin.size();
            while (transferred != size){
                transferred += fin.transferTo(0, size, fout);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
