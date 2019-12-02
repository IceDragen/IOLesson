import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;

/**
 * Description:
 */
public class ChatServer {
    private final static int DEFAULT_PORT = 30000;
    private final static String QUIT = "quit";
    private final static int BUFFER_SIZE = 1024;

    private ServerSocketChannel server;
    private Selector selector;

    //用来获取客户端发来消息的buffer
    private ByteBuffer rBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    //用来转发给其他所有客户端的buffer
    private ByteBuffer wBuffer = ByteBuffer.allocate(BUFFER_SIZE);

    //统一消息编码的字符集，比BIO里改进了一点
    private Charset charset = StandardCharsets.UTF_8;
    //用户自定义的服务器监听端口
    private int port;

    public ChatServer() {
        this(DEFAULT_PORT);
    }

    public ChatServer(int port) {
        this.port = port;
    }

    public void start(){
        try{
            server = ServerSocketChannel.open();
            //ServerSocketChannel同时支持阻塞和非阻塞调用，默认是阻塞调用，所以要在这里设置下非阻塞调用
            server.configureBlocking(false);
            //拿到serverSocket,绑定监听端口
            server.socket().bind(new InetSocketAddress(port));

            //注册事件,服务器端感兴趣的事件是ACCEPT，也就是服务端接收了一个客户端连接
            selector = Selector.open();
            server.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("服务器端已启动，监听端口：" + port);

            //监听事件
            while (true){
                //这个select是阻塞函数，会监听所有注册在其中的channel的事件是否发生，一旦发生就返回
                selector.select();
                //先调用select()，然后调用selectedKeys()返回所有被触发事件的SelectionKey集合
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                for (SelectionKey key : selectionKeys){
                    handleEvent(key);
                }
                //手动清空该集合，否则下一次调用selectedKeys()返回的集合会包括上一次的结果
                selectionKeys.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            close();
        }
    }

    private void handleEvent(SelectionKey key) throws IOException {
        /*
        主要处理两种事件：
            Accept：和客户端之间建立了连接
            Read：客户端有消息发送过来
         */
        if (key.isAcceptable()){
            //首先获取注册在selector中的serverSocketChannel
            ServerSocketChannel server = (ServerSocketChannel) key.channel();
            //然后通过serverSocketChannel获取连接的客户端SocketChannel
            SocketChannel client = server.accept();
            //设置客户端不阻塞
            client.configureBlocking(false);
            //在selector中注册该客户端，感兴趣的事件是客户端的可读事件，即当客户端有消息发过来的时候
            client.register(selector, SelectionKey.OP_READ);
            System.out.println(String.format("客户端[%d]已连接", client.socket().getPort()));
        }else if (key.isReadable()){
            SocketChannel client = (SocketChannel) key.channel();
            String msg = receive(client);
            //如果客户端发过来的是空消息，那么我们认为这个客户端的连接出现了异常，需要将它关闭。
            if (msg.isEmpty()){
                quitChannel(key);
                System.out.println(String.format("客户端[%d]已断开", client.socket().getPort()));
            }else {
                forwardMessage(msg, client);
                if (readyToQuit(msg)){
                    quitChannel(key);
                    System.out.println(String.format("客户端[%d]已断开", client.socket().getPort()));
                }
            }
        }
    }

    private void forwardMessage(String msg, SocketChannel client){
        /*
        转发的对象不包括服务端和发送消息的客户端自身，同时要保证channel自身是有效的同时其注册的selector也是有效的(isValid())的作用
         */
        selector.keys().stream().filter(key -> !(key.channel() instanceof ServerSocketChannel)
                && !key.channel().equals(client) && key.isValid()).forEach(key -> {
                    String fwdMsg = "客户端[" + client.socket().getPort() + "]: " + msg;
                    wBuffer.clear();
                    wBuffer.put(charset.encode(fwdMsg));
                    wBuffer.flip();
                    SocketChannel channel = (SocketChannel) key.channel();
                    //一次写入不保证全部写完buffer里的内容，所以有没写完的内容就要接着写
                    while (wBuffer.hasRemaining()){
                        try {
                            channel.write(wBuffer);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
        });
    }

    private void quitChannel(SelectionKey key) {
        //解除绑定
        key.cancel();
        //由于selector中的channel注册信息已经发生改变，所以如果此时有其他线程由于调用select方法陷入了阻塞，那么让select()立即返回，详情看注释
        selector.wakeup();
    }

    private String receive(SocketChannel channel) throws IOException {
        //channel.read(rBuffer);
        //String msg = "";
        //if (rBuffer.hasArray()){
        //    rBuffer.flip();
        //    msg = new String(rBuffer.array(), charset);
        //    rBuffer.clear();
        //}

        rBuffer.clear();
        //如果从客户端读取的字节数大于0就继续读，直到没有字节可以读为止
        while (channel.read(rBuffer) > 0);
        rBuffer.flip();

        return String.valueOf(charset.decode(rBuffer));
    }

    private void close() {
        //关闭selector的同时会关闭注册在其中的所有channel
        Optional.ofNullable(selector).ifPresent(s -> {
            try {
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public boolean readyToQuit(String msg){
        return QUIT.equals(msg);
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.start();
    }
}
