package edu.hdu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Description: 处理每个连接客户端的读写事件
 */
public class ChatHandler implements Runnable{

    private ChatServer server;
    private Socket socket;

    public ChatHandler(ChatServer server, Socket socket) {
        this.server = server;
        this.socket = socket;
    }


    @Override
    public void run() {
        try {
            server.addClient(socket);

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String msg;

            //readLine是个阻塞函数，如果没有内容它会一直停在那里知道有内容
            while ((msg = reader.readLine()) != null){
                String fwdMsg = String.format("收到来自客户端[%d]的消息：%s\n", socket.getPort(), msg);
                server.forwardMessage(socket, fwdMsg);

                if (server.readyToQuit(msg)){
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                server.removeClient(socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
