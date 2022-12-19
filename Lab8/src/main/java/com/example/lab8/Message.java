package com.example.lab8;

import java.io.Serializable;

public class Message implements Serializable {
    public MyState[][] myStates;

    public MsgState state;

    public int row;
    public int column;


    Message(MyState[][] st, MsgState state_, int row_, int column_) {
        state = state_;
        row = row_;
        column = column_;
        if (st != null) {
            myStates = new MyState[10][10];
            for (int i = 0; i < 10; i++) {
                System.arraycopy(st[i], 0, myStates[i], 0, 10);
            }
        }
    }
}
