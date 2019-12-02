import javax.sound.sampled.Line;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Description: 回音壁功能
 */
public class Server {
    private final String LOCALHOST = "localhost";
    private final int DEFAULT_PORT = 8888;
    private AsynchronousServerSocketChannel serverSocketChannel;

    public void start(){
        try {
            serverSocketChannel = AsynchronousServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(LOCALHOST, DEFAULT_PORT));
            System.out.println(String.format("服务器端已启动，正在监听端口[%d]", DEFAULT_PORT));

            while (true) {

                /*
                有两种方式来解决异步调用的结果：
                    1. 使用Future，这个在客户端的实现中会演示；
                    2. 使用回调函数，这就是我们在服务器端演示的
                 */
                serverSocketChannel.accept(null, new AcceptHandler());

                //由于accept函数是异步的，回立即返回，所以为了避免过于频繁的调用accept函数加上了下面这个阻塞式操作
                System.in.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            close();
        }
    }

    private void close(){
        Optional.ofNullable(serverSocketChannel).ifPresent(channel -> {
            try {
                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel, Object> {

        @Override
        public void completed(AsynchronousSocketChannel result, Object attachment) {
            //前面明明已经while循环中去调用accept函数了，为啥还要在这里再一次调用accept函数?
            //答：其实前面用System.in.read()把循环阻塞住了，其实只调用了一次accept函数，所以还要在这里调；
            if (serverSocketChannel.isOpen()){
                serverSocketChannel.accept(null, this);
            }

            ByteBuffer buffer = ByteBuffer.allocate(1024);
            Map<String, Object> info = new HashMap<>();
            info.put("type", "read");
            info.put("buffer", buffer);
            result.read(buffer, info, new ClientHandler(result));
        }

        @Override
        public void failed(Throwable exc, Object attachment) {

        }
    }

    private class ClientHandler implements CompletionHandler<Integer, Map<String, Object>>{

        private AsynchronousSocketChannel clientChannel;

        ClientHandler(AsynchronousSocketChannel clientChannel) {
            this.clientChannel = clientChannel;
        }


        @Override
        public void completed(Integer result, Map<String, Object> attachment) {
            String type = (String) attachment.get("type");

            if ("read".equals(type)){
                ByteBuffer buffer = (ByteBuffer) attachment.get("buffer");
                buffer.flip();
                if (result > 0){
                    System.out.println(new String(buffer.array(), 0, result));
                    attachment.put("type", "write");

                    clientChannel.write(buffer, attachment, this);
                    //buffer.clear();
                }

            }else if ("write".equals(type)){
                //如果回传消息完毕就重新监听读事件

                //TODO 由于执行handler的线程不是和start()方法在同一个线程中，考虑到线程安全问题，重新定义一个新的？到底哪里会出现线程安全问题？
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                attachment.put("type", "read");
                attachment.put("buffer", buffer);
                clientChannel.read(buffer, attachment, this);
            }
        }

        @Override
        public void failed(Throwable exc, Map<String, Object> attachment) {

        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}
