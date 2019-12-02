import sun.tools.tree.ByteExpression;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Description:
 */
public class Client {
    private final String LOCALHOST = "localhost";
    private final int DEFAULT_PORT = 8888;
    private AsynchronousSocketChannel socketChannel;

    public void start(){
        try {
            socketChannel = AsynchronousSocketChannel.open();
            //Future中的范型类型是你后面调用函数的返回类型
            Future<Void> connect = socketChannel.connect(new InetSocketAddress(LOCALHOST, DEFAULT_PORT));
            //阻塞式调用等待连接完成
            connect.get();

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while (true){
                String msg = reader.readLine();
                ByteBuffer wBuffer = ByteBuffer.wrap(msg.getBytes());

                Future<Integer> writeResult = socketChannel.write(wBuffer);
                writeResult.get();


                ByteBuffer rBuffer = ByteBuffer.allocate(1024);
                Future<Integer> readResult = socketChannel.read(rBuffer);
                readResult.get();
                rBuffer.flip();
                int length = readResult.get();

                System.out.println(new String(rBuffer.array(), 0, length));
            }

        } catch (IOException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }finally {
            close();
        }

    }

    private void close() {
        try {
            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.start();
    }

}
