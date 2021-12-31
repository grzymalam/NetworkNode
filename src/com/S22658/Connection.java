package com.S22658;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Connection implements Runnable{
    private final Socket connectionSocket;
    private final Node node;
    public Connection(Socket socket, Node node) {
        this.connectionSocket = socket;
        this.node = node;
    }

    @Override
    public void run() {
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

            String line;
            while ((line = input.readLine()) != null) {
                System.out.println(line);
                Message msg = new Message(line);
                switch (msg.type){
                    case CLIENTRESOURCEREQUEST: node.setClientSocket(connectionSocket); node.allocateResources(msg.getResources(), msg.getID()); node.isCommunicationNode = true; break;
                    case NODESUCCESSNOTIFICATION: node.fillNodesToCheck(); node.bouncebackSuccess(); node.confirmResourceAllocation(node.getID()); break;
                    case NODEFAILNOTIFICATION: node.removeFailedNode(msg.getSenderNodeID(), msg.getResources()); break;
                    case NODEALLOCATIONREQUEST: node.allocateResources(msg.getResources(), msg.getID()); break;
                    case NETWORKCONFIRMATION: node.confirmResourceAllocation(msg.getID()); break;
                    case SENDSELFID: node.addConnectingNodeToConnectedNodes(msg.getSenderNodeID(), new AddressWrapper(msg.getIp(), msg.getPort())); break;
                    case NODECONNECTIONREQUEST: node.addConnectedNode(msg.getSenderNodeID(), new AddressWrapper(msg.getIp(), msg.getPort()));
                }
                break;
            }
            System.out.println("connection closed");
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
