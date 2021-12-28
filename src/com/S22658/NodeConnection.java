package com.S22658;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

public class NodeConnection implements Runnable{
    private final Socket nodeSocket;
    private BufferedReader input = null;
    private PrintWriter output = null;

    public NodeConnection(Socket socket) {
        this.nodeSocket = socket;
    }
}
