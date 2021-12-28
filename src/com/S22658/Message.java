package com.S22658;

import java.net.Socket;
import java.util.HashMap;

public class Message {
    private String ID;
    private HashMap<String, Integer> resources = new HashMap<String, Integer>();
    public messageType type;
    //node z ktorym klient sie polaczyl
    private String ip;
    private int port;
    private Socket source;
    /*
    * >=0 client
    * -1 nodeAllocationRequest
    * -2 nodeFail
    * -3 nodeSuccess
    * -4 confirmChanges
    * */
    public Message(String msg, Socket source){
        String[] messageSplit = msg.split(" ");
        if(Integer.valueOf(ID = messageSplit[0]) >= 0) {
            type = messageType.CLIENTRESOURCEREQUEST;
            ip = source.getInetAddress().getHostAddress();
            port = source.getPort();
            for(int i = 1; i < messageSplit.length; i++){
                resources.put(messageSplit[i].split(":")[0], Integer.valueOf(messageSplit[i].split(":")[1]));
            }
        }else if(Integer.valueOf(ID) == -1){
            type = messageType.NODEALLOCATIONREQUEST;
        }else if (Integer.valueOf(ID) == -2){
            type = messageType.NODEFAILNOTIFICATION;
        }else if (Integer.valueOf(ID) == -3) {
            type = messageType.NODESUCCESSNOTIFICATION;
        }else
            type = messageType.NETWORKCONFIRMATION;
    }
    public String getID(){
        return ID;
    }
    public Socket getSource() {
        return source;
    }
    public HashMap<String, Integer> getResources() {
        return resources;
    }
}
enum messageType{
    CLIENTRESOURCEREQUEST, NODESUCCESSNOTIFICATION, NODEFAILNOTIFICATION, NODEALLOCATIONREQUEST, NETWORKCONFIRMATION
}
