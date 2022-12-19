package com.example.lab8;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ClientGUI implements Initializable {

    @FXML
    GridPane myPole;

    @FXML
    Label infoLabel;

    @FXML
    Button resetBtn;

    @FXML
    GridPane enemyPole;

    private boolean setPole = true;

    private boolean clickedNode = false;

    private Node ClickedNode;
    public Image shipImage = new Image("D:\\5sem\\RPKS\\RPKS-5sem\\Lab8\\ship.png");
    public Image breakShipImage = new Image("D:\\5sem\\RPKS\\RPKS-5sem\\Lab8\\breakship.png");
    public Image notShipImage = new Image("D:\\5sem\\RPKS\\RPKS-5sem\\Lab8\\notship.png");

    private ObservableList<Node> myChildrens;
    private ObservableList<Node> enemyChildrens;
    private List<ImageView> picList = new ArrayList<>();
    public Client my_client;

    public boolean turn = false;

    public HashMap<Integer, Integer> amountFree;



    public MyState[][] myPoleState = new MyState[10][10];
    public MyState[][] enemyPoleState = new MyState[10][10];

    public void drawNotShip(int row, int col, int preRow, int preCol){
        int rowBefore = row - 1;
        int colBefore = col - 1;
        for (int i = 0; i < 9; i++) {
            int rowNow = rowBefore + (i / 3);
            int colNow = colBefore + (i % 3);
            boolean isVisited = (rowNow == row && colNow == col) || (rowNow == preRow && colNow == preCol);
            if (rowNow >= 0 && rowNow <= 9 && colNow >= 0 && colNow <= 9 && (!isVisited)){
                if  (enemyPoleState[rowNow][colNow] == MyState.CRUSH) {
                    drawNotShip(rowNow, colNow, row, col);
                } else if (enemyPoleState[rowNow][colNow] == MyState.EMPTY) {
                    Platform.runLater(() ->{
                        enemyPole.add(new ImageView(notShipImage), colNow, rowNow);
                    });
                    enemyPoleState[rowNow][colNow] = MyState.BLOCKED;
                }
            }
        }
    }

    @FXML
    protected void resetPole(){
        if (setPole) {
            for (int i = 0; i <= 9; i++){
                for (int j = 0; j <=9; j++)
                    myPoleState[i][j] = MyState.EMPTY;
            }
            for (ImageView img : picList){
                myPole.getChildren().remove(img);
            }
            amountFree.put(4, 1);
            amountFree.put(3, 2);
            amountFree.put(2, 3);
            amountFree.put(1, 4);
        }
    }

    @FXML
    protected void startGame() {
        if (amountFree.get(1) == 0 && amountFree.get(2) == 0 && amountFree.get(3) == 0 && amountFree.get(4) == 0) {
            infoLabel.setText("Хорошо, будем отправлять на сервер");
            ClickedNode = null;
            Message msg = new Message(myPoleState, MsgState.POLE, 0, 0);
            System.out.println("Sending to server....");
            my_client.sendMessage(msg);
            System.out.println("End Sending to server....");
            setPole = false;
        }
        else {
            infoLabel.setText("Вы не закончили расстановку!");
        }

    }
    public void setClient(Client my){
        my_client = my;
        listen();
    }

    public void printPole(){
        for (int i = 0; i < 10; i++) {
            for(int j = 0; j < 10; j++)
            {
                System.out.print(myPoleState[i][j] + " ");
            }
            System.out.println();
        }
    }


    public void setBlockedVertical(Integer i_bottom, Integer i_up, Integer j) {
        System.out.println("i_up:" + i_up + " i_btm:" + i_bottom + "j: " + j);
        for (int i = i_up - 1; i <= i_up + (i_bottom - i_up) + 1; i++) {
            if ((i >= 0) && (j - 1 >= 0) && (i <= 9))
                myPoleState[i][j - 1] = MyState.BLOCKED;
            if ((i >= 0) && (j + 1 <= 9) && (i <= 9)) {
                myPoleState[i][j + 1] = MyState.BLOCKED;
            }
        }
        if ((i_up - 1) >= 0)
            myPoleState[i_up - 1][j] = MyState.BLOCKED;
        if ((i_bottom + 1) <= 9)
            myPoleState[i_bottom + 1][j] = MyState.BLOCKED;

    }

    public boolean checkHorizontal(int j_start, int j_finish, int i) {
        for(int j = j_start; j <= j_finish; j++) {
            if (myPoleState[i][j] != MyState.EMPTY) {
                return false;
            }
        }
        return true;
    }

    public boolean checkVertical(int i_min, int i_max, int j) {
        for (int i = i_min; i <= i_max; i++) {
            if (myPoleState[i][j] != MyState.EMPTY)
                return false;
        }
        return true;
    }
    public void setBlockedHorizontal(Integer j_left, Integer j_right, Integer i) {
        System.out.println("j_left:" + j_left + " j_right:" + j_right + "i: " + i);
        for (int j = j_left - 1; j <= j_left + (j_right - j_left) + 1; j++) {
            if ((j >= 0) && (i - 1 >= 0) && (j <= 9))
                myPoleState[i - 1][j] = MyState.BLOCKED;
            if ((j >= 0) && (i + 1 <= 9) && (j <= 9)) {
                myPoleState[i + 1][j] = MyState.BLOCKED;
            }
        }
        if ((j_left - 1) >= 0)
            myPoleState[i][j_left - 1] = MyState.BLOCKED;
        if ((j_right + 1) <= 9)
            myPoleState[i][j_right + 1] = MyState.BLOCKED;

    }

    public void goFight() {
        if (turn){
            Message msg = new Message(null, MsgState.FIGHT, GridPane.getRowIndex(ClickedNode), GridPane.getColumnIndex(ClickedNode));
            System.out.println("Я ОТПРАВЛЯЮ");
            my_client.sendMessage(msg);
            System.out.println("Я ОТПРАВИЛ");
            turn = false;
        }

    }


    @Override
    public void initialize(URL url, ResourceBundle rb){
        int numCols = 10;
        int numRows = 10;
        amountFree = new HashMap<>();
        amountFree.put(4, 1);
        amountFree.put(3, 2);
        amountFree.put(2, 3);
        amountFree.put(1, 4);
        for(int i = 0; i < 10; i++)
        {
            for (int j = 0; j < 10; j++)
            {
                myPoleState[i][j] = MyState.EMPTY;
            }
        }
        for(int i = 0; i < 10; i++)
        {
            for (int j = 0; j < 10; j++)
            {
                enemyPoleState[i][j] = MyState.EMPTY;
            }
        }

        for (int i = 0 ; i < numCols ; i++) {
            for (int j = 0; j < numRows; j++) {
                Pane newPane = new Pane();
                myPole.add(newPane, i, j);
            }
        }

        for (int i = 0 ; i < numCols ; i++) {
            for (int j = 0; j < numRows; j++) {
                Pane newPane = new Pane();
                enemyPole.add(newPane, i, j);
            }
        }
        myChildrens = myPole.getChildren();
        enemyChildrens = enemyPole.getChildren();

        for (Node node : myChildrens) {
            node.onMouseClickedProperty().set(new EventHandler<>() {
                @Override
                public void handle(MouseEvent e) {
                    if (setPole) {
                        if (!clickedNode) {
                            ClickedNode = node;
                            clickedNode = true;
                        } else if (clickedNode) {
                            clickedNode = false;
                            if ((GridPane.getRowIndex(ClickedNode) - GridPane.getRowIndex(node) == 0) && (Math.abs(GridPane.getColumnIndex(ClickedNode)- GridPane.getColumnIndex(node)) + 1) <= 4) {
                                if (amountFree.get(Math.abs(GridPane.getColumnIndex(ClickedNode)- GridPane.getColumnIndex(node)) + 1) != 0){
                                    if (checkHorizontal(Math.min(GridPane.getColumnIndex(ClickedNode), GridPane.getColumnIndex(node)),
                                            Math.max(GridPane.getColumnIndex(ClickedNode), GridPane.getColumnIndex(node)),
                                                    GridPane.getRowIndex(ClickedNode))) {
                                        amountFree.put(Math.abs(GridPane.getColumnIndex(ClickedNode)- GridPane.getColumnIndex(node)) + 1,
                                                amountFree.get(Math.abs(GridPane.getColumnIndex(ClickedNode)- GridPane.getColumnIndex(node)) + 1) -1);
                                        for (int j = Math.min(GridPane.getColumnIndex(ClickedNode), GridPane.getColumnIndex(node));
                                             j <= Math.max(GridPane.getColumnIndex(ClickedNode), GridPane.getColumnIndex(node)); j++) {
                                                myPoleState[GridPane.getRowIndex(ClickedNode)][j] = MyState.SHIP;
                                                ImageView img = new ImageView(shipImage);
                                                picList.add(img);
                                                myPole.add(img, j, GridPane.getRowIndex(ClickedNode));
                                        }
                                        setBlockedHorizontal(Math.min(GridPane.getColumnIndex(ClickedNode), GridPane.getColumnIndex(node)),
                                                Math.max(GridPane.getColumnIndex(ClickedNode), GridPane.getColumnIndex(node)),
                                                GridPane.getRowIndex(node));
                                    }
                                    else {
                                        clickedNode = false;
                                        ClickedNode = null;
                                    }

                                }
                                else {
                                    return;}
                            }
                            else if ((GridPane.getColumnIndex(ClickedNode) - GridPane.getColumnIndex(node) == 0) && (Math.abs(GridPane.getRowIndex(ClickedNode)- GridPane.getRowIndex(node)) + 1) <= 4) {
                                if (amountFree.get(Math.abs(GridPane.getRowIndex(ClickedNode)- GridPane.getRowIndex(node)) + 1) != 0) {
                                    if (checkVertical(Math.min(GridPane.getRowIndex(ClickedNode), GridPane.getRowIndex(node)),
                                            Math.max(GridPane.getRowIndex(ClickedNode), GridPane.getRowIndex(node)),
                                            GridPane.getColumnIndex(ClickedNode))) {
                                        amountFree.put(Math.abs(GridPane.getRowIndex(ClickedNode) - GridPane.getRowIndex(node)) + 1,
                                                amountFree.get(Math.abs(GridPane.getRowIndex(ClickedNode) - GridPane.getRowIndex(node)) + 1) - 1);
                                        for (int i = Math.min(GridPane.getRowIndex(ClickedNode), GridPane.getRowIndex(node));
                                             i <= Math.max(GridPane.getRowIndex(ClickedNode), GridPane.getRowIndex(node)); i++) {
                                                myPoleState[i][GridPane.getColumnIndex(ClickedNode)] = MyState.SHIP;
                                            ImageView img = new ImageView(shipImage);
                                            picList.add(img);
                                            myPole.add(img, GridPane.getColumnIndex(ClickedNode), i);
                                        }
                                        setBlockedVertical(Math.max(GridPane.getRowIndex(ClickedNode), GridPane.getRowIndex(node)),
                                                Math.min(GridPane.getRowIndex(ClickedNode), GridPane.getRowIndex(node)), GridPane.getColumnIndex(node));
                                    }
                                    else{
                                        clickedNode = false;
                                        ClickedNode = null;
                                    }
                                } else {
                                    return;
                                }
                            } else {
                                ClickedNode = null;
                                return;
                            }
                            System.out.println("POLE");
                        }
                    }
                }
            });
        }

        for (Node node : enemyChildrens) {
            node.onMouseClickedProperty().set(new EventHandler<>() {
                @Override
                public void handle(MouseEvent e) {
                    if (turn && !setPole && enemyPoleState[GridPane.getRowIndex(node)][GridPane.getColumnIndex(node)] == MyState.EMPTY) {
                        System.out.println("Hello you jump in ENEMY" + GridPane.getRowIndex(node) + " " + GridPane.getColumnIndex(node));
                        ClickedNode = node;
                    }
                }
            });
        }
        System.out.println("k");
    }

    public void receiveMsg() {
        while (true) {
            System.out.println("RECIEVING...");
            if (my_client == null)
                System.out.println("NULL");
            Message msg = new Message(null, null, 0, 0);
            msg = my_client.recieve();
            System.out.println("RECIEVE!");
            if (msg.state == MsgState.TURN) {
                System.out.println("MY TURN!");
                turn = true;
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        infoLabel.setText("ВАШ ХОД!");
                    }
                });
            }
            else if (msg.state == MsgState.WAIT) {
                turn = false;
                System.out.println("NOT MY TURN!");
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        infoLabel.setText("ХОД СОПЕРНИКА!");
                    }
                });

            }
            else if (msg.state == MsgState.REACH) {
                System.out.println("I AM REACH");
                int row = msg.row;
                int column = msg.column;
                Platform.runLater(() ->{
                    enemyPole.add(new ImageView(breakShipImage), column, row);
                });
                //enemyPole.add(new ImageView(breakShipImage), msg.column, msg.row);
                enemyPoleState[msg.row][msg.column] = MyState.CRUSH;
                //drawNotShip(msg.row, msg.column, -1, -1);
                turn = true;
            }
            else if (msg.state == MsgState.UREACH) {
                System.out.println("U ARE REACH");
                int row = msg.row;
                int column = msg.column;
                Platform.runLater(() ->{
                    myPole.add(new ImageView(breakShipImage), column, row);
                });
                //myPole.add(new ImageView(shipImage), msg.column, msg.row);
                myPoleState[msg.row][msg.column] = MyState.CRUSH;
            }
            else if (msg.state == MsgState.NOTREACH) {
                System.out.println("LOSER");
                enemyPoleState[msg.row][msg.column] = MyState.BLOCKED;
                int row = msg.row;
                int column = msg.column;
                Platform.runLater(() ->{
                    enemyPole.add(new ImageView(notShipImage), column, row);
                    infoLabel.setText("ХОД СОПЕРНИКА!");
                });
            } else if (msg.state == MsgState.KILLSHIP) {
                int row = msg.row;
                int column = msg.column;
                Platform.runLater(() ->{
                    enemyPole.add(new ImageView(breakShipImage), column, row);
                });
                enemyPoleState[msg.row][msg.column] = MyState.CRUSH;
                drawNotShip(msg.row, msg.column, -1, -1);
                turn = true;
            }
            else if (msg.state == MsgState.WIN) {
                Platform.runLater(() ->{
                    infoLabel.setText("ВЫ ВЫИГРАЛИ");
                });
            } else if (msg.state == MsgState.LOSE) {
                Platform.runLater(() ->{
                    infoLabel.setText("ВЫ ПРОИГРАЛИ");
                });
            }


        }
    }

    public void listen() {
        CompletableFuture.runAsync(this::receiveMsg);
    }


}