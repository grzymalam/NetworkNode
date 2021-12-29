package com.S22658;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class Connection implements Runnable{
    private final Socket connectionSocket;
    private BufferedReader input = null;
    private PrintWriter output = null;
    private Node node;
    private Message msg;
    private boolean isClientConnection = false;
    public Connection(Socket socket, Node node) {
        this.connectionSocket = socket;
        this.node = node;
    }

    @Override
    public void run() {
        try {

            output = new PrintWriter(connectionSocket.getOutputStream(), true);
            input = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

            String line;
            while ((line = input.readLine()) != null) {
                System.out.println(line);
                msg = new Message(line, connectionSocket);
                switch (msg.type){
                    case CLIENTRESOURCEREQUEST: node.allocateResources(msg.getResources(), connectionSocket, msg.getID(), msg.getSource()); node.isCommunicationNode = true; break;
                    case NODESUCCESSNOTIFICATION: node.clearFailedNodes(); output.println("ALLOCATED"); node.confirmResourceAllocation(node.getID(), connectionSocket); break;
                    case NODEFAILNOTIFICATION: node.addFailedNode(msg.getSenderNodeID()); break;
                    case NODEALLOCATIONREQUEST: node.allocateResources(msg.getResources(), connectionSocket, msg.getID(), msg.getSource()); break;
                    case NETWORKCONFIRMATION: node.confirmResourceAllocation(msg.getID(), msg.getSource()); break;
                    case NODECONNECTIONREQUEST: node.addConnectedNode(msg.getSenderNodeID(), msg.getSource());
                }
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
