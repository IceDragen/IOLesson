package edu.hdu;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Description:
 */
public class NIOBufferFileCopyRunner implements FileCopyRunner {

    @Override
    public void copyFile(File source, File target) {
        try(FileChannel fin = new FileInputStream(source).getChannel();
            FileChannel fout = new FileOutputStream(target).getChannel()) {

            ByteBuffer buffer = ByteBuffer.allocate(1024);

            while (fin.read(buffer) != -1){
                //将buffer改成读模式
                buffer.flip();

                //确保将buffer里的数据都读完，仅仅调用一次fout.write(buffer)不够保险
                while (buffer.hasRemaining()){
                    fout.write(buffer);
                }

                //恢复成写模式
                buffer.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
