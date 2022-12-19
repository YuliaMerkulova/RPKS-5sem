package com.example.lab8;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class Server {

    private String host = "localhost"; //127.0.0.1
    private Integer port = 8843;

    public List<ClientHandler> clients = new ArrayList<>();

    public Server() {
    }

    public AtomicInteger whoTurn;
    public AtomicInteger startGame;

    public ArrayList<MyState[][]> poles;
    public int[] ships;

    public Server(String host, Integer port) {
        this.host = host;
        this.port = port;
    }



    public void start() {
        whoTurn = new AtomicInteger(1);
        startGame = new AtomicInteger(0);
        poles = new ArrayList<>(2);
        ships = new int[2];
        ships[0] = 20;
        ships[1] = 20;
        System.out.println("Инициализация сервера");
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Сервер стартовал и ожидает подключение клиента");
            for (int i = 0; i < 2; i++)
            {
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

