import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Description:
 */
public class Server {
    private static final String HOST = "localhost";
    private static final int DEFAULT_PORT = 30000;
    private static final String QUIT = "quit";
    private static final int BUFFER_SIZE = 1024;
    private static final int THREAD_POOL_SIZE = 8;

    //自定义AsynchronousChannelGroup，从而比较精确地控制所有连接的资源使用情况
    private AsynchronousChannelGroup channelGroup;
    private AsynchronousServerSocketChannel serverSocket;
    /*
    为什么要保存的是ClientHandler对象而不是socketChannel对象？
    因为我希望一个socketChannel对象由一个ClientHandler去处理，我们可以通过ClientHandler拿到其对应的SocketChannel，但是反过来就不行，
    所以为了始终能由同一个ClientHandler去处理同一个SocketChannel，我们选择在列表中保存SocketChannel对应的ClientHandler对象。
     */
    private List<ClientHandler> connectedClients;
    private Charset charset = StandardCharsets.UTF_8;
    private int port;

    public Server() {
        this(DEFAULT_PORT);
    }

    public Server(int port) {
        this.port = port;
        connectedClients = new ArrayList<>();
    }

    public void start(){
        try {
            ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
            channelGroup = AsynchronousChannelGroup.withThreadPool(executorService);
            serverSocket = AsynchronousServerSocketChannel.open(channelGroup);
            serverSocket.bind(new InetSocketAddress(HOST, port));
            System.out.println("服务器已启动，监听端口：" + port);

            while (true){
                serverSocket.accept(null, new AcceptHandler());
                //阻塞调用，防止accept调用过于频繁
                System.in.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            close(serverSocket);
        }
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

    private String getClientName(AsynchronousSocketChannel client) {
        int remotePort = 0;
        try {
            InetSocketAddress address = (InetSocketAddress) client.getRemoteAddress();
            remotePort = address.getPort();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return String.format("客户端[%d]", remotePort);
    }

    private synchronized void addClient(ClientHandler handler){
        connectedClients.add(handler);
        System.out.println(getClientName(handler.client) + "已连接");
    }

    private synchronized void removeClient(ClientHandler handler){
        connectedClients.remove(handler);
        System.out.println(getClientName(handler.client) + "已断开");
        close(handler.client);
    }

    private class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel, Object> {
        @Override
        public void completed(AsynchronousSocketChannel client, Object attachment) {
            /*
            1. 开启下一次accept；
            2。将新连接加入连接列表；
            3。从连接中读取数据
             */
            if (serverSocket.isOpen()){
                serverSocket.accept(null, this);
            }
            if (client.isOpen()){
                ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                ClientHandler handler = new ClientHandler(client);
                client.read(buffer, buffer, handler);
                //将新连接加入连接列表
                addClient(handler);
            }
        }

        @Override
        public void failed(Throwable exc, Object attachment) {
            System.out.println("连接失败：" + exc);
        }
    }

    private class ClientHandler implements CompletionHandler<Integer, ByteBuffer>{

        private AsynchronousSocketChannel client;

        public ClientHandler(AsynchronousSocketChannel channel) {
            this.client = channel;
        }

        @Override
        public void completed(Integer result, ByteBuffer buffer) {
            /*
            判断是读还是写的回调：
                读事件：
                    1. 获取消息内容；
                    2。将消息内容转发给其他存活的客户端
                    判断是否是quit消息：
                        是：
                            将该客户端移出客户端列表
                        否：
                          开启下一次读取事件
                 写事件：
                    不做处理
             */
            //通过attachment是否为空来表示是读事件还是写事件
            if (buffer != null){
                if (result <= 0){
                    //移除该客户端
                    removeClient(this);
                }else {
                    String fwdMsg = receive(buffer, charset);
                    System.out.println(getClientName(client) + ": " + fwdMsg);
                    forwardMessage(fwdMsg);

                    if (readyToQuit(fwdMsg)){
                        removeClient(this);
                    }else {
                        client.read(buffer, buffer, new ClientHandler(client));
                    }
                }
            }
        }

        private synchronized void forwardMessage(String fwdMsg) {
            connectedClients.stream().filter(handler -> !handler.equals(this)).forEach(handler -> {
                //防止一个客户端出错而影响到其他客户端
                try {
                    handler.client.write(charset.encode("收到来自" + getClientName(client) + fwdMsg), null, handler);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        private String receive(ByteBuffer buffer, Charset charset) {
            buffer.flip();
            String result =  String.valueOf(charset.decode(buffer));
            buffer.clear();
            return result;
        }

        @Override
        public void failed(Throwable exc, ByteBuffer attachment) {

        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}
