package edu.hdu;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Description: 聊天室服务器端，主要作用是接收客户端连接
 */
public class ChatServer {
    private final int DEFAULT_PORT = 30000;
    private final String QUIT = "quit";

    private ServerSocket ss;
    private ExecutorService executorService;
    // 用来保存已连接的客户端,暂时用客户端连接的端口作为key
    private Map<Integer, Writer> liveClients;

    public ChatServer() {
        this.liveClients = new HashMap<>();
        executorService = Executors.newFixedThreadPool(10);
    }

    //  由于调用了map的put方法，所以要注意同步
    public synchronized void addClient(Socket socket) throws IOException {
        if (socket != null){
            int port = socket.getPort();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            liveClients.put(port, writer);
        }
    }

    public synchronized void removeClient(Socket socket) throws IOException {
        if (socket != null){
            int port = socket.getPort();
            if (liveClients.containsKey(port)){
                liveClients.get(port).close();
                liveClients.remove(port);
            }
        }
    }

    public synchronized void forwardMessage(Socket from, String msg) throws IOException{
        liveClients.keySet().stream().filter(i -> i != from.getPort()).forEach(i -> {
            Writer writer = liveClients.get(i);
            try {
                writer.write(msg);
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void  start(){
        try {
            ss = new ServerSocket(DEFAULT_PORT);
            System.out.println("服务器已启动～");
            while (true){
                Socket socket = ss.accept();
                System.out.println(String.format("客户端[%d]已连接", socket.getPort()));
                //socket
                executorService.execute(new ChatHandler(this, socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            close();
        }
    }

    private void close() {
        Optional.ofNullable(ss).ifPresent(socket -> {
            try {
                socket.close();
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
