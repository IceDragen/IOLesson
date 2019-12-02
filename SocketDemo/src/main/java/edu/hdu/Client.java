package edu.hdu;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Description:
 */
public class Client {
    public static void main(String[] args) {
        final String DEFAULT_HOST = "127.0.0.1";
        final int DEFAULT_HOST_PORT = 30000;
        final String QUIT = "quit";

        try(Socket socket = new Socket(DEFAULT_HOST, DEFAULT_HOST_PORT)) {
            System.out.println("客户端准备就绪");
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            while (true){
                String msg = consoleReader.readLine();

                writer.write(msg + "\n");
                writer.flush();

                String response = reader.readLine();
                System.out.println("收到来自服务器的消息：" + response);

                if (QUIT.equals(msg)){
                    System.out.println("客户端已断开连接");
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
