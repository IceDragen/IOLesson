package edu.hdu;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Optional;

/**
 * Description: 服务端Socket
 */
public class Server {
    public static void main(String[] args) {
        final int DEFAULT_PORT = 30000;
        ServerSocket serverSocket = null;
        final String QUIT = "quit";

        try {
            serverSocket = new ServerSocket(DEFAULT_PORT);
            System.out.println("服务端已启动～");

            while (true){
                Socket socket = serverSocket.accept();

                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                String msg;
                while ((msg = reader.readLine()) != null) {
                    System.out.printf("收到来自客户端%s:%d的消息\t", socket.getInetAddress().getHostAddress(), socket.getPort());
                    System.out.println("消息内容为: " + msg);

                    writer.write("消息已收到，请放心!\n");
                    writer.flush();

                    if (QUIT.equals(msg)){
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
