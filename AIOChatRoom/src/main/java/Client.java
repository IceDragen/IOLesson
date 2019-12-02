import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Description:
 */
public class Client {
    private static final String HOST = "localhost";
    private static final int DEFAULT_PORT = 30000;
    private static final String QUIT = "quit";
    private static final int BUFFER_SIZE = 1024;

    private AsynchronousSocketChannel socketChannel;
    private Charset charset = StandardCharsets.UTF_8;
    private int port;

    public Client(){
        this(DEFAULT_PORT);
    }

    public Client(int port) {
        this.port = port;
    }

    public boolean readyToQuit(String msg){
        return QUIT.equalsIgnoreCase(msg);
    }

    public void close(Closeable c){
        Optional.ofNullable(c).ifPresent(closeable -> {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void start(){
        /*
        1. 建立连接
        2。开启处理用户输入的线程
        3。开启循环接收服务端数据
         */
        try {
            socketChannel = AsynchronousSocketChannel.open();
            Future<Void> connect = socketChannel.connect(new InetSocketAddress(HOST, port));
            connect.get();

            Thread inputHandler = new Thread(new UserInputHandler(this));
            inputHandler.start();


            while (true){
                ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                Future<Integer> readResult = socketChannel.read(buffer);
                int length = readResult.get();

                if (length <= 0){
                    System.out.println("服务器已断开连接，即将关闭客户端！");
                    //控制台输入无法响应中断
                    //inputHandler.interrupt();
                    System.exit(1);
                }
                buffer.flip();
                System.out.println(charset.decode(buffer));
                buffer.clear();
            }
        } catch (IOException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }finally {
            close(socketChannel);
        }
    }

    public void send(String msg) {
        if (msg.isEmpty()){
            return;
        }

        ByteBuffer buffer = charset.encode(msg);
        Future<Integer> writeResult = socketChannel.write(buffer);
        try {
            writeResult.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            System.out.println("消息发送失败！");
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.start();
    }

    public void exit() {
        close(socketChannel);
    }
}
