package com.example.lab8;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

import java.net.URL;
import java.util.*;

public class ClientGUI implements Initializable {

    @FXML
    GridPane myPole;

    @FXML
    Label infoLabel;

    @FXML
    GridPane enemyPole;

    private boolean setPole = true;

    private boolean clickedNode = false;

    private Node ClickedNode;
    public Image shipImage = new Image("D:\\5sem\\RPKS\\RPKS-5sem\\Lab8\\ship.png");

    private ObservableList<Node> myChildrens;
    private ObservableList<Node> enemyChildrens;
    public Client my_client;

    public HashMap<Integer, Integer> amountFree;

    public int fourthMy = 1;
    public int thirdMy = 2;
    public int secondMy = 3;
    public int firstMy = 4;


    public MyState[][] myPoleState = new MyState[10][10];


    @FXML
    protected void startGame() {
        if (amountFree.get(1) == 0 && amountFree.get(2) == 0 && amountFree.get(3) == 0 && amountFree.get(4) == 0) {
            infoLabel.setText("Хорошо, будем отправлять на сервер");

        }
        else {
            infoLabel.setText("Вы не закончили расстановку!");
        }

    }
    public void setClient(Client my){
        my_client = my;
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
                                        //System.out.println("SET HORIZONTAL");
                                        for (int j = Math.min(GridPane.getColumnIndex(ClickedNode), GridPane.getColumnIndex(node));
                                             j <= Math.max(GridPane.getColumnIndex(ClickedNode), GridPane.getColumnIndex(node)); j++) {
                                                myPoleState[GridPane.getRowIndex(ClickedNode)][j] = MyState.SHIP;
                                                myPole.add(new ImageView(shipImage), j, GridPane.getRowIndex(ClickedNode));
                                        }
                                        setBlockedHorizontal(Math.min(GridPane.getColumnIndex(ClickedNode), GridPane.getColumnIndex(node)),
                                                Math.max(GridPane.getColumnIndex(ClickedNode), GridPane.getColumnIndex(node)),
                                                GridPane.getRowIndex(node));
                                    }
                                    else {
                                        //System.out.println("NOT HOR HERE");
                                        clickedNode = false;
                                        ClickedNode = null;
                                    }

                                }
                                else {
                                    //System.out.println("NOT AMOUNT");
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
                                                myPole.add(new ImageView(shipImage), GridPane.getColumnIndex(ClickedNode), i);
                                        }
                                        setBlockedVertical(Math.max(GridPane.getRowIndex(ClickedNode), GridPane.getRowIndex(node)),
                                                Math.min(GridPane.getRowIndex(ClickedNode), GridPane.getRowIndex(node)), GridPane.getColumnIndex(node));
                                    }
                                    else{
                                        //System.out.println("NOT VER HERE");
                                        clickedNode = false;
                                        ClickedNode = null;
                                    }
                                } else {
                                    //System.out.println("NOT AMOUNT 2");
                                    return;
                                }
                            } else {
                                //System.out.println("IN TH EL");
                                ClickedNode = null;
                                return;
                            }
                            System.out.println("POLE");
                            printPole();
//                            Message msg = new Message(myPoleState);
//                            System.out.println("Sending to server....");
//                            my_client.sendMessage(msg);
//                            System.out.println("End Sending to server....");
//                            System.out.println("Receive msg....");
//                            msg = my_client.recieve();
//                            for (int i = 0; i < 10; i++)
//                            {
//                                for (int j = 0; j < 10; j++)
//                                {
//                                    System.out.print(msg.myStates[i][j]);
//                                }
//                                System.out.println();
//                            }
                        }
                    }
                }
            });
        }

        for (Node node : enemyChildrens) {
            node.onMouseClickedProperty().set(new EventHandler<>() {
                @Override
                public void handle(MouseEvent e) {
                    if (!setPole) {
                        System.out.println("Hello you jump in ENEMY" + GridPane.getRowIndex(node) + " " + GridPane.getColumnIndex(node));
                        //my_client.sendMessage("Cli you jump in ENEMY" + GridPane.getRowIndex(node) + " " + GridPane.getColumnIndex(node));
                    }
                }
            });
        }
    }

}