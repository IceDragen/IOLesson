package edu.hdu;

import java.io.File;

/**
 * Description: 分别用流和channel的方式实现文件拷贝
 */

interface FileCopyRunner{
    void copyFile(File source, File target);
}

//public class FileCopyDemo {
//    public static void main(String[] args) {
//        FileCopyRunner noBufferedStreamCopy;
//        FileCopyRunner bufferedStreamCopy;
//        FileCopyRunner nioBufferCopy;
//        FileCopyRunner nioTransferCopy;
//    }
//}
