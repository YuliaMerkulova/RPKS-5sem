package com.example.lab8;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;


public class ClientHandler implements Runnable {

    private static final String ANCHOR_NAME = "###";

    private Socket client;
    private Server server;
    private String name;
    private int turn;


    public ClientHandler(Socket client, Server server, int turn_) throws IOException {
        this.client = client;
        this.server = server;
        this.turn = turn_;
        this.inputStream =  new ObjectInputStream(client.getInputStream());
        outputStream = new ObjectOutputStream(client.getOutputStream());
    }

    ObjectInputStream inputStream;
    ObjectOutputStream outputStream;
    @Override
    public void run() {
        try {
            while (client.isConnected()) {
                if (!client.isInputShutdown()) {
                    Message text = null;
                    try{
                        text = (Message) inputStream.readObject();
                    }
                    catch (EOFException er) {
                        er.printStackTrace();
                    }
                    if (text != null) {
                        for (int i = 0; i < 10; i++) {
                            for (int j = 0; j < 10; j++) {
                                System.out.print(text.myStates[i][j]);
                            }
                            System.out.println();
                        }
                    }
                    outputStream.writeObject(text);
                    //server.sendMessageToChat(text);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Ошибка при работе с клиентом");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            if (client != null) {
                try {
                    client.close();
                } catch (IOException ex) {
                    System.out.println("Ошибка при закрытии клиента!");
                }
            }
        }
    }

    public void sendMessage(Message message) {
        ObjectOutputStream outputStream = null;
        try {
            outputStream = new ObjectOutputStream(client.getOutputStream());
            outputStream.writeObject(message);
            outputStream.flush();
        } catch (IOException e) {
            System.out.println("Проблема при записи сообщения в поток клиента: " + client.toString());
        }
    }

    public String getName() {
        return name;
    }
}

//public class ClientHandler implements Runnable {
//
//    private static final String ANCHOR_NAME = "###";
//
//    private Socket client;
//    private Server server;
//    private String name;
//
//    private ObjectInputStream in; // поток чтения из сокета
//    private ObjectOutputStream out; // поток записи в сокет
//    public ClientHandler(Socket client, Server server) throws IOException {
//        this.client = client;
//        this.server = server;
//        in = new ObjectInputStream(client.getInputStream());
//        out = new ObjectOutputStream(client.getOutputStream());
//    }
//
//    @Override
//    public void run() {
//        try {
//
//            while (!client.isClosed()) {
//                Message msg = (Message) in.readObject();
//                System.out.println("Сообщение от клиента: " + msg.column);
//                server.sendMessageToChat(msg);
//            }
//        } catch (IOException e) {
//            if (client.isClosed()) {
//                System.out.println("Closed");
//            }
//            else {
//                System.out.println("open");
//            }
//            System.err.println("Ошибка ubuhb при работе с клиентом" +  e);
//        } catch (ClassNotFoundException e) {
//            throw new RuntimeException(e);
//        }
//        finally {
//            if (in != null) {
//                try {
//                    in.close();
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//            if (client != null) {
//                try {
//                    client.close();
//                } catch (IOException ex) {
//                    System.err.println("Ошибка при закрытии клиента!" + ex);
//                }
//            }
//        }
//}
//
//    public void sendMessage(Message msg) {
//        try {
//            //out = new ObjectOutputStream(client.getOutputStream());
//            out.writeObject(msg);
//            out.flush();
//        } catch (IOException e) {
//            System.err.println("Проблема при записи сообщения в поток клиента: " + client.toString() + e);
//        }
//    }
//
//    public String getName() {
//        return name;
//    }
//}
