package com.S22658;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
//wezel systemu rozproszonego
public class Node implements Runnable{
    private String ID;
    private int port;
    private HashMap<String, Integer> resources = new HashMap<>();
    private Socket nodeNetworkSocket;
    private ServerSocket communicationSocket;
    private ArrayList<Socket> connectedNodes = new ArrayList<>();
    //podlaczanie do istniejacej sieci
    public Node(String id, int port, String nodeNetworkIP, int nodeNetworkPort, Map<String, Integer> resources){
        this.ID = id;
        this.port = port;
        try {
            nodeNetworkSocket = new Socket(InetAddress.getByName(nodeNetworkIP), nodeNetworkPort);
            communicationSocket = new ServerSocket(port);
        } catch (IOException e ) {
            System.out.println("Nie udalo sie podlaczyc do sieci.");
        }
    }
    //tworzenie wezla-matki
    public Node(String id, int port, Map<Character, Integer> resources){
        this.ID = id;
        this.port = port;
        try {
            communicationSocket = new ServerSocket(port);
        } catch (IOException e ) {
            System.out.println("Nie udalo sie podlaczyc do sieci.");
        }
    }

    @Override
    public void run() {
        Socket newConnection = null;
        while(true){
            try {
                Socket newConnection = communicationSocket.accept();
            } catch (IOException e) {
                System.out.println("Blad odbioru danych.");
            }
            String id = newConnection.get
        }
    }
}
