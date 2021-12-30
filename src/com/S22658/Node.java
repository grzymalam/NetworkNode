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
    private Socket clientSocket;
    private ServerSocket communicationSocket;
    private HashMap<String, Socket> connectedNodes = new HashMap<>();
    private HashMap<String, HashMap<String, Integer>> pendingChanges = new HashMap<>();
    private ArrayList<String> nodesLeftToCheck = new ArrayList<>();
    private String allocationInvokerID;
    public boolean isCommunicationNode = false;

    //podlaczanie do istniejacej sieci
    public Node(String id, int port, String nodeNetworkIP, int nodeNetworkPort, HashMap<String, Integer> resources) {
        this.ID = id;
        this.port = port;
        this.resources = resources;
        try {
            nodeNetworkSocket = new Socket(InetAddress.getByName(nodeNetworkIP), nodeNetworkPort);
            communicationSocket = new ServerSocket(port);
            PrintWriter writer = new PrintWriter(nodeNetworkSocket.getOutputStream(), true);
            writer.println("-4 " + id);
            System.out.println("Wezel " + id + " podlaczyl sie do wezla o ip: " + nodeNetworkIP + " i porcie: " + nodeNetworkPort);
        } catch (IOException e) {
            System.out.println("Nie udalo sie podlaczyc do sieci.");
        }
    }

    //tworzenie wezla-matki
    public Node(String id, int port, HashMap<String, Integer> resources) {
        this.ID = id;
        this.port = port;
        this.resources = resources;
        try {
            communicationSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("Nie udalo sie podlaczyc do sieci.");
        }
    }

    public String getID() {
        return ID;
    }

    public void removeFailedNode(String id, HashMap<String, Integer> resourcesToAllocate) {
        nodesLeftToCheck.remove(id);
       //TODO sprawdzenie kolejnego node w kolei/bounceback jak nie ma
    }
    //nody dostepne do sprawdzenia sa resetowane po sukcesie
    public void fillNodesToCheck(){
        nodesLeftToCheck = new ArrayList<>(connectedNodes.keySet());
    }

    public void addConnectedNode(String id, Socket s) {
        connectedNodes.put(id, s);
        nodesLeftToCheck.add(id);
    }

    public HashMap<String, Socket> getConnectedNodes() {
        return connectedNodes;
    }
    public void allocateResources(HashMap<String, Integer> resourcesToAllocate , String ID, Socket source) {
        allocationInvokerID = ID;
        if(isCommunicationNode)
            clientSocket = source;
        System.out.println("NODE [" + this.ID + "] allocating resources for: " + source.getLocalAddress().getHostAddress());
        HashMap<String, Integer> resourceCopy = new HashMap<>(resources);
        System.out.println(resources.keySet());
        //proba alokacji w current wezle
        for (String resource : resourcesToAllocate.keySet()) {
            PrintWriter output = null;
            int amountOfResourceToBeAllocated = resourcesToAllocate.get(resource);
            if (resourceCopy.containsKey(resource)) {
                int diff = resourceCopy.get(resource) - amountOfResourceToBeAllocated;
                System.out.println("NODE [" + this.ID + "]: diff: " + diff);
                System.out.println("NODE [" + this.ID + "]: node's A resource amount: " + resourceCopy.get(resource));
                System.out.println("NODE [" + this.ID + "]: node's requested A resource amount: " + amountOfResourceToBeAllocated);
                if (diff >= 0) {
                    System.out.println("NODE [" + this.ID + "]: allocation successful.");
                    //node posiada wystarczajaca ilosc zasobu
                    if(!isCommunicationNode) {
                        try {
                            output = new PrintWriter(source.getOutputStream(), true);
                        } catch (IOException e) {
                            System.out.println("Blad przy powiadamianiu wezla kontaktowego.");
                        }
                        resourceCopy.put(resource, resourceCopy.get(resource) - amountOfResourceToBeAllocated);
                        resourcesToAllocate.remove(resource);
                        output.println("-3");
                    }
                } else {
                    System.out.println("NODE [" + this.ID + "]: node doesnt have enough resources to allocate.");
                    //jezeli node nie mial wystarczajacej ilosci zasobu
                    resourceCopy.put(resource, 0);
                    resourcesToAllocate.put(resource, diff * -1);
                    System.out.println("NODE [" + this.ID + "]: resource left to alloc: " + resourcesToAllocate.get(resource));
                    if (connectedNodes.size() == 1 && !isCommunicationNode) {
                        try {
                            output = new PrintWriter(source.getOutputStream(), true);
                        } catch (IOException e) {
                            System.out.println("Blad przy powiadamianiu wezla kontaktowego.");
                        }
                        String msg = "-2 ";
                        for (String s : resourcesToAllocate.keySet()) {
                            msg += s + ":" + resourcesToAllocate.get(s) + " ";
                        }
                        try {
                            output.println(msg);
                        } catch (Exception e) {
                            System.out.println("Blad powiadamiania");
                        }
                    }
                }
            }
        }
        pendingChanges.put(ID, resourceCopy);
        System.out.println("CONNECTED NODES: ");
        connectedNodes.keySet().forEach(System.out::println);
        System.out.println("CONNECTED NODES SOCKETS: ");
        connectedNodes.values().forEach(System.out::println);
        System.out.println("[NODE " + this.ID + "]NODES LEFT TO CHECK: " + nodesLeftToCheck);
        if(nodesLeftToCheck.size()>0) {
            String nodeToCheckID = nodesLeftToCheck.get(0);
            Socket socket = connectedNodes.get(nodeToCheckID);
            String msg = "-1 ";
            for (String resource : resourcesToAllocate.keySet()) {
                msg += resource + ":" + resourcesToAllocate.get(resource) + " ";
            }
            try {
                PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                output.println(msg);
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("Problem z przekazaniem zadania rezerwacji zasobow pozostalym wezlom.");
            }
        }else{
            String msg = "-2 ";
            for (String resource : resourcesToAllocate.keySet()) {
                msg += resource + ":" + resourcesToAllocate.get(resource) + " ";
            }
            try {
                PrintWriter output = new PrintWriter(source.getOutputStream(), true);
                output.println(msg);
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("Problem z przekazaniem zadania rezerwacji zasobow pozostalym wezlom.");
            }
        }
    }
    //TODO sprawdzenie kolejnego node zamiast robienie wszystkiego w jednej metodzie (poco?)
    public void checkNextChildNode(){

    }
    //jezeli cos edytuje zasoby pomiedzy proba, a potwierdzeniem to sie wysypie
    public void confirmResourceAllocation(String id, Socket source) {
        for (Socket s : connectedNodes.values()) {
            if (s.getPort() != source.getPort() && s.getLocalAddress() != s.getLocalAddress()) {
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
