package edu.hdu;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Description:
 */
public class NIOBufferFileCopyRunnerTest {

    @Test
    public void copyFile() {
        File source = new File("/Users/apple/Develop/Projects/java/IOLesson/NIODemo/src/test/java/edu/hdu/test.txt");
        File target = new File("/Users/apple/Develop/Projects/java/IOLesson/NIODemo/src/test/java/edu/test.txt");

        NIOBufferFileCopyRunner runner = new NIOBufferFileCopyRunner();
        runner.copyFile(source, target);
    }
}