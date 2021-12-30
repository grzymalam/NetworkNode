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
                    case CLIENTRESOURCEREQUEST: node.allocateResources(msg.getResources(), msg.getID(), connectionSocket); node.isCommunicationNode = true; break;
                    case NODESUCCESSNOTIFICATION: node.fillNodesToCheck(); node.bouncebackSuccess(); node.confirmResourceAllocation(node.getID(), connectionSocket); break;
                    case NODEFAILNOTIFICATION: node.removeFailedNode(msg.getSenderNodeID(), msg.getResources()); break;
                    case NODEALLOCATIONREQUEST: node.allocateResources(msg.getResources(), msg.getID(), connectionSocket); break;
                    case NETWORKCONFIRMATION: node.confirmResourceAllocation(msg.getID(), connectionSocket); break;
                    case SENDSELFID: node.addConnectingNodeToConnectedNodes(msg.getSenderNodeID(), connectionSocket); break;
                    case NODECONNECTIONREQUEST: node.addConnectedNode(msg.getSenderNodeID(), connectionSocket); node.sendSelfId(connectionSocket);
                }
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
