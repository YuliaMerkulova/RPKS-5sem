package com.example.lab8;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

import static com.example.lab8.MsgState.*;


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
        this.outputStream = new ObjectOutputStream(client.getOutputStream());
    }

    public boolean isCrushed(int row, int col, int preRow, int preCol, int numEnemy) {
        boolean res = true;
        int rowBefore = row - 1;
        int colBefore = col - 1;
        for (int i = 0; i < 9; i++) {
            int rowNow = rowBefore + (i / 3);
            int colNow = colBefore + (i % 3);
            boolean isVisited = (rowNow == row && colNow == col) || (rowNow == preRow && colNow == preCol);
            if (rowNow >= 0 && rowNow <= 9 && colNow >= 0 && colNow <= 9 && (!isVisited)){
                if  (server.poles.get(numEnemy)[rowNow][colNow] == MyState.SHIP) {
                    return false;
                } else if (server.poles.get(numEnemy)[rowNow][colNow] == MyState.CRUSH) {
                    res = res && isCrushed(rowNow, colNow, row, col, numEnemy);
                }
            }
        }
        return res;
    }

    ObjectInputStream inputStream;
    ObjectOutputStream outputStream;
    @Override
    public void run() {
        try {
            while (!client.isClosed()) {
                if (server.whoTurn.get() == 3) {
                    break;
                }
                    if (server.whoTurn.get() == this.turn) {
                        Message text = null;
                        System.out.println("Я жду сообщение от клиента" + server.whoTurn.get());
                        text = (Message) inputStream.readObject();
                        System.out.println("Я получил сообщение от клиента" + this.turn);
                        if (text != null) {
                            if (text.state == POLE) {
                                server.poles.add(this.turn - 1, text.myStates);
                                if (this.turn == 1) {
                                    if (server.whoTurn.get() != 3)
                                        server.whoTurn.set(2);
                                }
                                else if (this.turn == 2){
                                    server.clients.get(0).sendMessage(new Message(null, TURN, 0, 0));
                                    this.sendMessage(new Message(null, WAIT, 0, 0));
                                    if (server.whoTurn.get() != 3)
                                        server.whoTurn.set(this.turn == 1? 2 : 1);
                                }
                            }else if (text.state == FIGHT) {
                                System.out.println("Я получил файт");
                                if (server.poles.get((turn == 1) ? 1: 0)[text.row][text.column] == MyState.SHIP) {
                                    server.poles.get((turn == 1) ? 1: 0)[text.row][text.column] = MyState.CRUSH;
                                    System.out.println("Я проверяю на краш");
                                    boolean res = isCrushed(text.row, text.column, -1, -1, ((turn == 1) ? 1: 0));
                                    System.out.println("Я проверил" + res);
                                    server.ships[(turn == 1) ? 1: 0]--;
                                    if (server.ships[(turn == 1) ? 1: 0] == 0) {
                                        this.sendMessage(new Message(null, WIN, text.row, text.column));
                                        server.clients.get((turn == 1) ? 1: 0).sendMessage(new Message(null, LOSE, text.row, text.column));
                                        if (server.whoTurn.get() != 3) {
                                            server.whoTurn.set(3);
                                        }
                                        continue;
                                    }
                                    if (res) {
                                        this.sendMessage(new Message(null, KILLSHIP, text.row, text.column));
                                        //server.clients.get((turn == 1) ? 1: 0).sendMessage(new Message(null, UREACH, text.row, text.column));
                                    } else {
                                        this.sendMessage(new Message(null, REACH, text.row, text.column));
                                        }
                                    server.clients.get((turn == 1) ? 1: 0).sendMessage(new Message(null, UREACH, text.row, text.column));
                                } else {
                                    this.sendMessage(new Message(null, NOTREACH, text.row, text.column));
                                    server.clients.get((turn == 1) ? 1: 0).sendMessage(new Message(null, TURN, text.row, text.column));
                                    if (server.whoTurn.get() != 3)
                                        server.whoTurn.set(this.turn == 1? 2 : 1);
                                }
                            }
                        }
                    }
            }
        } catch (IOException e) {
//            e.printStackTrace();
            System.out.println("Ошибка при работе с клиентом");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        finally {
            server.whoTurn.set(3);
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
        System.out.println("TRY TO SEND");
        try {
            //outputStream = new ObjectOutputStream(client.getOutputStream());
            System.out.println("STATE IS" + message.state);
            outputStream.writeObject(message);
            //outputStream.flush();
            System.out.println("SEND");
        } catch (IOException e) {
            System.out.println("Проблема при записи сообщения в поток клиента: " + client.toString());
        }
    }

    public String getName() {
        return name;
    }
}
