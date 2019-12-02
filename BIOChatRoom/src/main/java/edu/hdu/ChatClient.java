package edu.hdu;

import com.sun.corba.se.impl.ior.WireObjectKeyTemplate;
import org.omg.CORBA.PRIVATE_MEMBER;

import java.io.*;
import java.net.Socket;
import java.util.Optional;

/**
 * Description:
 */
public class ChatClient {
    private final String DEFAULT_HOST = "127.0.0.1";
    private final int DEFAULT_SERVER_PORT = 30000;
    private final String QUIT = "quit";

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    public void send(String msg) throws IOException {
        if (!socket.isOutputShutdown()){
            writer.write(msg + "\n");
            writer.flush();
        }
    }

    private String receive() throws IOException {
        if (!socket.isInputShutdown()){
            return reader.readLine();
        }
        return null;
    }

    public boolean readyToQuit(String msg){
        return QUIT.equals(msg);
    }

    public void start(){
        try {
            socket = new Socket(DEFAULT_HOST, DEFAULT_SERVER_PORT);

            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            //由于从控制台读取读取是一个阻塞操作，所以要新开线程去执行控制台输入相关处理逻辑
            new Thread(new UserInputHandler(this)).start();

            String msg;
            while ((msg = receive()) != null){
                System.out.println(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            close();
        }
    }

    public void close() {
        Optional.ofNullable(writer).ifPresent(w -> {
            try {
                w.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
        ChatClient client = new ChatClient();
        client.start();
    }
}
