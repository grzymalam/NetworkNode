package com.S22658;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//wezel systemu rozproszonego
public class Node implements Runnable {
    private String ID;
    private int port;
    private HashMap<String, Integer> resources = new HashMap<>();
    private Socket nodeNetworkSocket;
    private ServerSocket communicationSocket;
    private ArrayList<Socket> connectedNodes = new ArrayList<>();
    private HashMap<String, HashMap<String, Integer>> pendingChanges = new HashMap<>();
    private boolean communicationNode = false;

    //podlaczanie do istniejacej sieci
    public Node(String id, int port, String nodeNetworkIP, int nodeNetworkPort, HashMap<String, Integer> resources) {
        this.ID = id;
        this.port = port;
        try {
            nodeNetworkSocket = new Socket(InetAddress.getByName(nodeNetworkIP), nodeNetworkPort);
            communicationSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("Nie udalo sie podlaczyc do sieci.");
        }
    }

    //tworzenie wezla-matki
    public Node(String id, int port, HashMap<String, Integer> resources) {
        this.ID = id;
        this.port = port;
        try {
            communicationSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("Nie udalo sie podlaczyc do sieci.");
        }
    }

    public void addConnectedNode(Socket s) {
        connectedNodes.add(s);
    }

    public ArrayList<Socket> getConnectedNodes() {
        return connectedNodes;
    }

    public void allocateResources(HashMap<String, Integer> resourcesToAllocate, Socket notifierSocket, String ID, Socket source) {
        HashMap<String, Integer> resourceCopy = new HashMap<>(resources);
        for (String resource : resourcesToAllocate.keySet()) {
            PrintWriter output = null;
            int amountOfResourceToBeAllocated = resourcesToAllocate.get(resource);
            if (resourceCopy.containsKey(resource)) {
                int diff;
                if ((diff = resourceCopy.get(resource) - amountOfResourceToBeAllocated) >= 0) {
                    //node posiada wystarczajaca ilosc zasobu
                    try {
                        output = new PrintWriter(notifierSocket.getOutputStream(), true);
                    } catch (IOException e) {
                        System.out.println("Blad przy powiadamianiu wezla kontaktowego.");
                    }
                    resourceCopy.put(resource, resourceCopy.get(resource) - amountOfResourceToBeAllocated);
                    resourcesToAllocate.remove(resource);
                    output.println("-3");
                } else {
                    //jezeli node nie mial wystarczajacej ilosci zasobu
                    resourceCopy.put(resource, 0);
                    resourcesToAllocate.put(resource, diff * -1);
                    if (connectedNodes.size() == 1
                            && source.getLocalAddress() == connectedNodes.get(0).getLocalAddress()
                            && source.getPort() == connectedNodes.get(0).getPort()) {
                        try {
                            output = new PrintWriter(source.getOutputStream(), true);
                        } catch (IOException e) {
                            System.out.println("Blad przy powiadamianiu wezla kontaktowego.");
                        }
                        output.println("-2");
                    }
                }
            }
        }
        pendingChanges.put(ID, resourceCopy);
        for (Socket s : connectedNodes) {
            String msg = "-1 ";
            for (String resource : resourcesToAllocate.keySet()) {
                msg += resource + ":" + resourcesToAllocate.get(resource) + " ";
            }
            try {
                PrintWriter output = new PrintWriter(s.getOutputStream(), true);
                output.println(msg);
            } catch (IOException e) {
                System.out.println("Problem z przekazaniem zadania rezerwacji zasobow pozostalym wezlom.");
            }
        }
    }

    //jezeli cos edytuje zasoby pomiedzy proba, a potwierdzeniem to sie wysypie
    public void confirmResourceAllocation(String id, Socket source) {
        for(Socket s: connectedNodes){
            if(s.getPort()!=source.getPort() && s.getLocalAddress() != s.getLocalAddress()) {
                PrintWriter output = null;
                try {
                    output = new PrintWriter(source.getOutputStream(), true);
                } catch (IOException e) {
                    System.out.println("Blad przy powiadamianiu wezla kontaktowego.");
                }
                output.println("-4");
            }
        }
        resources = pendingChanges.get(id);
    }

    public void notifyCommunicationNode(Socket s) {
        try {
            PrintWriter output = new PrintWriter(s.getOutputStream(), true);
            output.println("Alokacja udana.");
        } catch (IOException e) {
            System.out.println("Blad powiadomienia klienta o alokacji zasobu.");
        }
    }

    @Override
    public void run() {
        Socket newConnection = null;
        while (true) {
            try {
                newConnection = communicationSocket.accept();
            } catch (IOException e) {
                System.out.println("Blad odbioru danych.");
            }
            new Thread(new Connection(newConnection, this)).start();
        }
    }
}
