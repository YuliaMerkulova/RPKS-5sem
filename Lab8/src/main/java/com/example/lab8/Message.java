package com.example.lab8;

import java.io.Serializable;

public class Message implements Serializable {
    MyState[][] myStates;
    Message(MyState[][] st) {
        myStates = new MyState[10][10];
        for(int i = 0; i < 10; i++)
        {
            System.arraycopy(st[i], 0, myStates[i], 0, 10);
        }
    }
}
