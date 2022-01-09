package com.S22658;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Connection implements Runnable {
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
            String line = input.readLine();
            System.out.print("Node " + node.getID());
            Message msg = new Message(line);
            String allocatedResourcesByNode = msg.getAllocatedResources();
            switch (msg.type) {
                case CLIENTRESOURCEREQUEST:
                    node.setClientSocket(connectionSocket);
                    node.allocateResources(msg.getResources(), msg.getID(), "");
                    break;
                //tu
                case NODESUCCESSNOTIFICATION:
                    node.fillNodesToCheck();
                    node.bouncebackSuccess(allocatedResourcesByNode);
                    break;
                //tu
                case NODEFAILNOTIFICATION:
                    node.removeFailedNode(msg.getSenderNodeID(), msg.getResources(), allocatedResourcesByNode);
                    break;
                //tu
                case NODEALLOCATIONREQUEST:
                    node.allocateResources(msg.getResources(), msg.getID(), allocatedResourcesByNode);
                    break;
                case SENDSELFID:
                    node.addConnectingNodeToConnectedNodes(msg.getSenderNodeID(), new AddressWrapper(msg.getIp(), msg.getPort()));
                    break;
                case NODECONNECTIONREQUEST:
                    node.addConnectedNode(msg.getSenderNodeID(), new AddressWrapper(msg.getIp(), msg.getPort()));
                    break;
                case NETWORKCONFIRMATION:
                    node.broadcast(msg.getID(), Node.BROADCAST_TYPE.SUCCESS);
                    break;
                case NETWORKFAILURE:
                    node.broadcast(msg.getID(), Node.BROADCAST_TYPE.FAILURE);
                    break;
                case TERMINATION:
                    node.broadcast("-1", Node.BROADCAST_TYPE.TERMINATION);
                    break;
            }
            if (msg.type != messageType.CLIENTRESOURCEREQUEST)
                connectionSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
