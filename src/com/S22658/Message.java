package com.S22658;

import java.util.HashMap;

public class Message {
    private String ID;
    private final HashMap<String, Integer> resources = new HashMap<>();
    public messageType type;
    private String allocatedResources;
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
        System.out.println(msg);
        String[] messageSplit = msg.split(" ");
        int integerID;
        ID = messageSplit[0];
        if(ID.equals("TERMINATE"))
            integerID = -8;
        else
            integerID = Integer.parseInt(ID);
        switch (integerID){
            case -1:
                type = messageType.NODEALLOCATIONREQUEST;
                ID = messageSplit[1];
                for (int i = 2; i < messageSplit.length-1; i++) {
                    resources.put(messageSplit[i].split(":")[0], Integer.valueOf(messageSplit[i].split(":")[1]));
                }
                allocatedResources = messageSplit[messageSplit.length-1];
                break;
            case -2:
                type = messageType.NODEFAILNOTIFICATION;
                senderNodeID = messageSplit[1];
                for (int i = 2; i < messageSplit.length - 1; i++) {
                    resources.put(messageSplit[i].split(":")[0], Integer.valueOf(messageSplit[i].split(":")[1]));
                }
                allocatedResources = messageSplit[messageSplit.length-1];
                break;
            case -3:
                type = messageType.NODESUCCESSNOTIFICATION;
                allocatedResources = messageSplit[messageSplit.length-1];
                break;
            case -4:
                type = messageType.NODECONNECTIONREQUEST;
                senderNodeID = messageSplit[1];
                ip = messageSplit[2];
                port = Integer.parseInt(messageSplit[3]);
                break;
            case -5:
                type = messageType.SENDSELFID;
                senderNodeID = messageSplit[1];
                ip = messageSplit[2];
                port = Integer.parseInt(messageSplit[3]);
                break;
            case -6:
                type = messageType.NETWORKCONFIRMATION;
                ID = messageSplit[1];
                break;
            case -7:
                type = messageType.NETWORKFAILURE;
                ID = messageSplit[1];
                break;
            case -8:
                type = messageType.TERMINATION;
                break;
            default:
                type = messageType.CLIENTRESOURCEREQUEST;
                for (int i = 1; i < messageSplit.length; i++) {
                    resources.put(messageSplit[i].split(":")[0], Integer.valueOf(messageSplit[i].split(":")[1]));
                }
        }
    }
    public HashMap<String, Integer> getResources() {
        return resources;
    }
    public String getID(){
        return ID;
    }
    public String getSenderNodeID() {
        return senderNodeID;
    }
    public String getAllocatedResources() {
        return allocatedResources;
    }
    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }
}
enum messageType{
    CLIENTRESOURCEREQUEST, NODESUCCESSNOTIFICATION, NODEFAILNOTIFICATION, NODEALLOCATIONREQUEST,
    NETWORKCONFIRMATION, NETWORKFAILURE, NODECONNECTIONREQUEST, SENDSELFID,
    TERMINATION
}
