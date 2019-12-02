package edu.hdu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Description:
 */
public class UserInputHandler implements Runnable{
    private  ChatClient client;

    public UserInputHandler(ChatClient client) {
        this.client = client;
    }

    @Override
    public void run() {
        //等待用户输入消息
        try(BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in))){
            String msg;
            while ((msg = consoleReader.readLine()) != null){
                client.send(msg);
                if (client.readyToQuit(msg)){
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
