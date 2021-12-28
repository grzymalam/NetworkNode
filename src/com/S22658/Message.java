package com.S22658;

import java.util.HashMap;

public class Message {
    private String ID;
    private HashMap<String, Integer> resources = new HashMap<String, Integer>();
    private messageType type;
    public Message(String msg){
        String[] messageSplit = msg.split(" ");
        if((ID = messageSplit[0]) != "0") {
            type = messageType.CLIENT;
            for(int i = 1; i < messageSplit.length; i++){
                resources.put(messageSplit[i].split(":")[0], Integer.valueOf(messageSplit[i].split(":")[1]));
            }
        }else{
            type = messageType.NODE;
        }
    }

    public HashMap<String, Integer> getResources() {
        return resources;
    }


}
enum messageType{
    NODE, CLIENT
}
