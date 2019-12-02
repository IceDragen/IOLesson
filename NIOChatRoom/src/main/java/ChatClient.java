import com.sun.deploy.security.TrustRecorder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * Description:
 */
public class ChatClient {
    private final static String DEFAULT_HOST = "127.0.0.1";
    private final static int DEFAULT_SERVER_PORT = 30000;
    private final static String QUIT = "quit";
    private final static int BUFFER_SIZE = 1024;
    //是否要停止客户端的标志
    private boolean isQuit = false;

    private SocketChannel client;
    private ByteBuffer rBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    private ByteBuffer wBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    private Selector selector;
    private Charset charset = StandardCharsets.UTF_8;

    public void start(){
        /*
        1. 初始化socket，和服务端之间建立连接
        2. selector中注册socket，感兴趣事件是connected；
        3. 处理对应的事件
         */

        try {
            client = SocketChannel.open();
            //设置客户端使用非阻塞方式，这样客户端的所有方法都不会陷入阻塞
            client.configureBlocking(false);
            //调用connect方法后会马上返回，返回时连接还没有建立，所以要在后面CONNECTED事件中进一步判断以确保连接建立
            client.connect(new InetSocketAddress(DEFAULT_HOST, DEFAULT_SERVER_PORT));

            selector = Selector.open();
            client.register(selector, SelectionKey.OP_CONNECT);

            while (!isQuit){
                //开始监听IO
                selector.select();
                //加这个判断是为了防止输入完quit停止客户端后继续执行if代码块里的逻辑，具体代码可以和老师的对照着看，主要视频讲解在NIO聊天室最后一集
                if (selector.isOpen()) {
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    handle(selectionKeys);
                    selectionKeys.clear();
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            close();
        }

    }

    private void close() {
        try {
            selector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void exit(){
        isQuit = true;
        selector.wakeup();
        close();
    }

    private void handle(Set<SelectionKey> selectionKeys) {
        selectionKeys.forEach(key -> {
            try {
                handleKey(key);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void handleKey(SelectionKey key) throws IOException {
        if (key.isConnectable()){
            SocketChannel channel = (SocketChannel) key.channel();
            //判断下是不是连接是不是还在建立的过程中
            if (channel.isConnectionPending()){
                channel.finishConnect();
            }
            channel.register(selector, SelectionKey.OP_READ);
            //由于用户输入是阻塞式的，所以单独开线程处理
            new Thread(new UserInputHandler(this)).start();
        }else if (key.isReadable()){
            String msg = receive(key);
            System.out.println(msg);
        }
    }

    private String receive(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        rBuffer.clear();
        while (channel.read(rBuffer) > 0);
        rBuffer.flip();

        return String.valueOf(charset.decode(rBuffer));
    }


    public void send(String msg) throws IOException {
        if (msg.isEmpty()){
            return;
        }
        wBuffer.clear();
        wBuffer.put(charset.encode(msg));
        wBuffer.flip();
        while (wBuffer.hasRemaining()){
            client.write(wBuffer);
        }
    }

    public boolean readyToQuit(String msg) {
        return QUIT.equals(msg);
    }

    public static void main(String[] args) {
        ChatClient client = new ChatClient();
        client.start();
    }
}
