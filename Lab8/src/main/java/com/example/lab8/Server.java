package com.example.lab8;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class Server {

    private String host = "localhost"; //127.0.0.1
    private Integer port = 8843;

    private List<ClientHandler> clients = new ArrayList<>();

    public Server() {
    }

    public AtomicInteger whoTurn;
    public AtomicBoolean startGame;

    public ArrayList<MyState[][]> poles;

    public Server(String host, Integer port) {
        this.host = host;
        this.port = port;
    }



    public void start() {
        whoTurn = new AtomicInteger(1);
        startGame = new AtomicBoolean(false);
        System.out.println("Инициализация сервера");
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Сервер стартовал и ожидает подключение клиента");
            while (true) {
                Socket client = serverSocket.accept();
                System.out.println("Подключился новый клиент: " + client.toString());
                ClientHandler clientHandler = null;
                if (clients.isEmpty()) {
                    clientHandler = new ClientHandler(client, this, 1);
                }
                else{
                    clientHandler = new ClientHandler(client, this, 2);
                }
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.out.println("Проблема с сервером");
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException ex) {
                    System.out.println("Проблема при закрытии сервера");
                }
            }
        }
    }
}

