package edu.hdu;

import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * Description:
 */
public class NoBufferedStreamFileCopyRunnerTest {

    @Test
    public void copyFile() {
        File source = new File("/Users/apple/Develop/Projects/java/IOLesson/NIODemo/src/test/java/edu/hdu/test.txt");
        File target = new File("/Users/apple/Develop/Projects/java/IOLesson/NIODemo/src/test/java/edu/test.txt");

        NoBufferedStreamFileCopyRunner runner = new NoBufferedStreamFileCopyRunner();
        runner.copyFile(source, target);
    }
}