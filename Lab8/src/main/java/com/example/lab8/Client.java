package com.example.lab8;
//

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;


public class Client {

    private String host = "localhost";
    private Integer port = 8843;
    private String name = "Bot";

    public void sendMessage(Message msg) {
        try {
            outputStream.writeObject(msg);
            outputStream.flush();
        }catch (IOException e)
        {
            System.out.println("Не отправили упс");
        }
    }

    public Message recieve() {
        Message msg = null;
        try{
            msg = (Message) inputStream.readObject();
        }catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return msg;
    }


    public void endWork()
    {
        try {
            outputStream.close();
            inputStream.close();
        }catch (IOException e)
        {
            System.out.println("Не закрыли упс");
        }

    }

    public Client(String host, Integer port, String name) {
        this.host = host;
        this.port = port;
        this.name = name;
    }
    public ObjectOutputStream outputStream;
    public ObjectInputStream inputStream;
    public void start() {
        try {
            System.out.println("Клиент инициализирован");
            Socket server = new Socket(host, port);
            outputStream = new ObjectOutputStream(server.getOutputStream());
            inputStream = new ObjectInputStream(server.getInputStream());

            //outputStream = new ObjectOutputStream(server.getOutputStream());
//            new Thread(() -> { //читаем соо с сервера
//                try {
//                    inputStream = new ObjectInputStream(server.getInputStream());
//                    while (inputStream.hasNext()) {
//                        String text = inputStream.nextLine();
//                        System.out.println(text);
//                    }
//                } catch (IOException e) {
//                    System.out.println("Проблема при чтении сервера клиентом");
//                }
//            }).start();

            //outputStream.close();
        } catch (IOException e) {
            System.out.println("Ошибка при подключении к серверу");
        }

    }
}




//import javafx.application.Application;
//import javafx.fxml.FXML;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.Scene;
//import javafx.stage.Stage;
//
//import java.io.IOException;
//import java.io.ObjectInputStream;
//import java.io.ObjectOutputStream;
//import java.io.PrintWriter;
//import java.net.Socket;
//import java.util.Scanner;
//
//public class Client implements Runnable {
//    private String host;
//    private Integer port;
//    private String name;
//    public Socket server;
//
//    public ObjectOutputStream outputStream;
//    public Client(String host, Integer port, String name) {
//        this.host = host;
//        this.port = port;
//        this.name = name;
//        outputStream
//        //try(server = new Socket(host, port); )
//        //server = client_sock;
//
//    }
//
//
//    //public Socket server;
//    public void sendMessage(Message message) throws IOException {
//        outputStream.writeObject(message);
//        outputStream.flush();
//    }
//
//
//    public void start(){
//
//        try(Socket server = new Socket(host, port)) {
//            try (ObjectOutputStream outputStream = new ObjectOutputStream(server.getOutputStream())) {
//                System.out.println("Клиент инициализирован");
//                new Thread(() -> {
//                    //ObjectInputStream inputStream = null;
//                    try (ObjectInputStream inputStream = new ObjectInputStream(server.getInputStream())) {
//                        //inputStream = new ObjectInputStream(server.getInputStream());
//                        while (!server.isClosed()) {
//                            System.out.println("waiting");
//                            if (!server.isClosed()) {
//                                System.out.println("WAAAIT");
//                                Message msg = (Message) inputStream.readObject();
//                                System.out.println("Сервер говорит: " + msg.column);
//                            } else {
//                                System.out.println("try serverclose");
//                                inputStream.close();
//                                server.close();
//                            }
//                            System.out.println("END WAAAIT");
//                        }
//                    } catch (IOException e) {
//                        System.out.println("Проблема при чтении сервера клиентом");
//                    } catch (ClassNotFoundException e) {
//                        throw new RuntimeException(e);
//                    }}).start();
//                Scanner inputMessage = new Scanner(System.in);
//                while (inputMessage.hasNext()) {
//                    outputStream.writeChars(inputMessage.nextLine());
//                    outputStream.flush();
//                }
//            } catch (IOException e) {
//                System.out.println("Ошибка при подключении к серверу");
//            }
//        } catch (IOException e)
//        {
//            e.printStackTrace();
//        }
//
//
//    }
//
//    @Override
//    public void run() {
//        start();
//    }
//
//}
