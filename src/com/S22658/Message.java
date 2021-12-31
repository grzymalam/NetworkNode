package com.S22658;

import java.net.Socket;
import java.util.HashMap;

public class Message {
    private String ID;
    private final HashMap<String, Integer> resources = new HashMap<>();
    public messageType type;
    //node z ktorym klient sie polaczyl
    private String ip;
    private int port;
    private String senderNodeID;
    /*
    * >=0 client
    * -1 nodeAllocationRequest
    * -2 nodeFail
    * -3 nodeSuccess
    * -4 nodeConnectionRequest
    * -5 sendselfid
    * -6 confirmChanges
    * */
    public Message(String msg) {
        System.out.println("NEW MESSAGE: " + msg);
        String[] messageSplit = msg.split(" ");
        ID = messageSplit[0];
        int integerID = Integer.valueOf(ID);
        if (integerID >= 0) {
            type = messageType.CLIENTRESOURCEREQUEST;
            for (int i = 1; i < messageSplit.length; i++) {
                resources.put(messageSplit[i].split(":")[0], Integer.valueOf(messageSplit[i].split(":")[1]));
            }
        } else if (integerID == -1) {
            type = messageType.NODEALLOCATIONREQUEST;
            ID = messageSplit[1];
            for (int i = 2; i < messageSplit.length; i++) {
                resources.put(messageSplit[i].split(":")[0], Integer.valueOf(messageSplit[i].split(":")[1]));
            }
            System.out.println("Alloc");
            System.out.println(ID);
            System.out.println(messageSplit[2]);
        } else if (integerID == -2) {
            type = messageType.NODEFAILNOTIFICATION;
            senderNodeID = messageSplit[1];
            for (int i = 2; i < messageSplit.length; i++) {
                resources.put(messageSplit[i].split(":")[0], Integer.valueOf(messageSplit[i].split(":")[1]));
            }
        } else if (integerID == -3) {
            type = messageType.NODESUCCESSNOTIFICATION;
        } else if (integerID == -4) {
            type = messageType.NODECONNECTIONREQUEST;
            senderNodeID = messageSplit[1];
            ip = messageSplit[2];
            port = Integer.valueOf(messageSplit[3]);
        }else if(integerID == -5){
            type = messageType.SENDSELFID;
            senderNodeID = messageSplit[1];
            ip = messageSplit[2];
            port = Integer.valueOf(messageSplit[3]);
            System.out.println("RECEIVED: " + senderNodeID + " " + ip + " " + port);
        }else if(integerID == -6){
            type = messageType.NETWORKCONFIRMATION;
            ID = messageSplit[1];
        }
    }
    public String getID(){
        return ID;
    }
    public String getSenderNodeID() {
        return senderNodeID;
    }
    public HashMap<String, Integer> getResources() {
        return resources;
    }
    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }
}
enum messageType{
    CLIENTRESOURCEREQUEST, NODESUCCESSNOTIFICATION, NODEFAILNOTIFICATION, NODEALLOCATIONREQUEST, NETWORKCONFIRMATION, NODECONNECTIONREQUEST, SENDSELFID
}
