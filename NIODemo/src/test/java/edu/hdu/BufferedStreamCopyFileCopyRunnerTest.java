package edu.hdu;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Description:
 */
public class BufferedStreamCopyFileCopyRunnerTest {

    @Test
    public void copyFile() {
        File source = new File("/Users/apple/Develop/Projects/java/IOLesson/NIODemo/src/test/java/edu/hdu/test.txt");
        File target = new File("/Users/apple/Develop/Projects/java/IOLesson/NIODemo/src/test/java/edu/test.txt");

        BufferedStreamCopyFileCopyRunner runner = new BufferedStreamCopyFileCopyRunner();
        runner.copyFile(source, target);

    }
}