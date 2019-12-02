package edu.hdu;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Description:
 */
public class NIOTransferFileCopyRunnerTest {

    @Test
    public void copyFile(){
        File source = new File("/Users/apple/Develop/Projects/java/IOLesson/NIODemo/src/test/java/edu/hdu/test.txt");
        File target = new File("/Users/apple/Develop/Projects/java/IOLesson/NIODemo/src/test/java/edu/test.txt");

        NIOTransferFileCopyRunner runner = new NIOTransferFileCopyRunner();
        runner.copyFile(source, target);

    }

    @Test
    public void copyFrom(){
        File source = new File("/Users/apple/Develop/Projects/java/IOLesson/NIODemo/src/test/java/edu/hdu/test.txt");
        File target = new File("/Users/apple/Develop/Projects/java/IOLesson/NIODemo/src/test/java/edu/test2.txt");
    }
}