package com.S22658;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

//wezel systemu rozproszonego
public class Node implements Runnable {
    private final String ID;
    private String address;
    private final int port;
    private HashMap<String, Integer> resources;
    private HashMap<String, Integer> pendingResources = new HashMap<>();
    private Socket nodeNetworkSocket;
    private Socket clientSocket;
    private ServerSocket communicationSocket;
    private HashMap<String, AddressWrapper> connectedNodes = new HashMap<>();
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
            communicationSocket.setReuseAddress(true);
            address = communicationSocket.getInetAddress().getHostAddress();
            System.out.println("ADDRESS ASSIGNMENT: " + address);
            PrintWriter writer = new PrintWriter(nodeNetworkSocket.getOutputStream(), true);
            writer.println("-4 " + id + " " + address + " " + port);
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
            address = communicationSocket.getInetAddress().getHostAddress();
        } catch (IOException e) {
            System.out.println("Nie udalo sie podlaczyc do sieci.");
        }
    }
    public void reset(){
        isCommunicationNode = false;
        fillNodesToCheck();
        clientSocket = null;
        pendingResources.clear();
    }
    public String getID() {
        return ID;
    }
    public HashMap<String, AddressWrapper> getConnectedNodes(){
        return connectedNodes;
    }
    public HashMap<String, Integer> getResources(){
        return resources;
    }
    public void setClientSocket(Socket socket){
        isCommunicationNode = true;
        clientSocket = socket;
    }
    //nody dostepne do sprawdzenia sa resetowane po sukcesie
    public void addConnectedNode(String id, AddressWrapper wrapper) {
        log("Adding new node and sending own id to it");
        connectedNodes.put(id, wrapper);
        nodesLeftToCheck.add(id);
        sendSelfId(wrapper);
    }
    public void addConnectingNodeToConnectedNodes(String id, AddressWrapper wrapper){
        log("Adding new node that we asked to be connected to.");
        connectedNodes.put(id, wrapper);
        nodesLeftToCheck.add(id);
    }

    public void fillNodesToCheck(){
        nodesLeftToCheck = new ArrayList<>(connectedNodes.keySet());
    }
    public void removeFailedNode(String id, HashMap<String, Integer> resourcesToAllocate) {
        nodesLeftToCheck.remove(id);
        checkNextChildNode(resourcesToAllocate);
    }
    public void send(String id, String message){
        PrintWriter writer;
        Socket socket;
        try{
            socket = new Socket(connectedNodes.get(id).getAddress(), connectedNodes.get(id).getPort());
            writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println(message);
        }catch (Exception e){
            log("Error sending message. STRING/STRING");
        }
    }
    public void send(AddressWrapper wrapper, String message){
        PrintWriter writer;
        Socket socket;
        try{
            socket = new Socket(wrapper.getAddress(), wrapper.getPort());
            writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println(message);
        }catch (Exception e){
            log("Error sending message. ADDRESSWRAPPER/STRING");
        }
    }
    public void send(Socket socket, String message){
        PrintWriter writer;
        try{
            writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println(message);
        }catch (Exception e){
            log("Error sending message. SOCKET/STRING");
        }
    }
    public void allocateResources(HashMap<String, Integer> resourcesToAllocate, String ID) {
        allocationInvokerID = ID;
        nodesLeftToCheck.remove(ID);
        System.out.println("NODE [" + this.ID + "] allocating resources for: " + ID);
        HashMap<String, Integer> resourceCopy = new HashMap<>(resources);
        HashMap<String, Integer> resourcesToAllocateAfterAllocation = new HashMap<>();
        log("AVAL resources: " + resourceCopy.keySet());
        log("AVAL resources: " + resourceCopy.values());
        log("resources to be allocated: " + resourcesToAllocate.keySet());
        log("resources to be allocated: " + resourcesToAllocate.values());
        //proba alokacji w current wezle
        for (String resource : resourcesToAllocate.keySet()) {
            int amountOfResourceToBeAllocated = resourcesToAllocate.get(resource);
            if (resourceCopy.containsKey(resource)) {
                int diff = resourceCopy.get(resource) - amountOfResourceToBeAllocated;
                System.out.println("NODE [" + this.ID + "]: diff: " + diff);
                System.out.println("NODE [" + this.ID + "]: node's " + resource + " resource amount: " + resourceCopy.get(resource));
                System.out.println("NODE [" + this.ID + "]: node's requested " + resource + "resource amount: " + amountOfResourceToBeAllocated);
                if (diff >= 0) {
                    //node posiada wystarczajaca ilosc zasobu
                    System.out.println("NODE [" + this.ID + "]: " + resource + " allocation successful.");
                    resourceCopy.replace(resource, diff);
                    System.out.println("NODE [" + this.ID + "]: " + resource + " resource left in node: " + diff);
                } else {
                    //jezeli node nie mial wystarczajacej ilosci zasobu
                    System.out.println("NODE [" + this.ID + "]: node doesnt have enough resources to allocate.");
                    resourceCopy.replace(resource, 0);
                    resourcesToAllocateAfterAllocation.put(resource, diff*-1);
                    System.out.println("NODE [" + this.ID + "]: resource left to alloc: " + resourcesToAllocateAfterAllocation.get(resource));
                }
            }else
                resourcesToAllocateAfterAllocation.put(resource, resourcesToAllocate.get(resource));
        }
        if(resourcesToAllocateAfterAllocation.size() == 0)
            bouncebackSuccess();
        else{
            if(nodesLeftToCheck.size() == 0)
                bouncebackFailed(resourcesToAllocateAfterAllocation);
            else
                checkNextChildNode(resourcesToAllocateAfterAllocation);
        }
        log("Putting pending changes: " + resourceCopy.keySet() + " " + resourceCopy.values());
        pendingResources = resourceCopy;
    }
    public void sendSelfId(AddressWrapper wrapper){
        String msg = "-5 " + ID + " " + address + " " + port;
        System.out.println("WRAPPER: " + wrapper);
        System.out.println("MESSAGE: " + msg);
        send(wrapper, msg);
        log("Sent own id.");
    }
    public void checkNextChildNode(HashMap<String, Integer> resourcesToAllocate){
        log("NODES LEFT TO CHECK: " + nodesLeftToCheck);
        log("resources to be allocated: " + resourcesToAllocate.keySet().toString());
        log("resources to be allocated: " + resourcesToAllocate.values().toString());
        if(nodesLeftToCheck.size()>0) {
            String nodeToCheckID = nodesLeftToCheck.get(0);
            log("CHECKING NODE " + nodeToCheckID);
            String msg = "-1 " + this.ID + " ";
            for (String resource : resourcesToAllocate.keySet()) {
                msg += resource + ":" + resourcesToAllocate.get(resource) + " ";
            }
            System.out.println("Msg = " + msg);
            send(nodeToCheckID, msg);
            System.out.println("Message sent");
            nodesLeftToCheck.remove(nodeToCheckID);
        }else{
            bouncebackFailed(resourcesToAllocate);
            log("Allocation failed. Not enough resources. Changes have not been saved");
        }
    }
    public void bouncebackFailed(HashMap<String,Integer> resourcesToAllocate){
        String msg = "-2 " + ID + " ";
        if(!isCommunicationNode) {
            for (String resource : resourcesToAllocate.keySet()) {
                msg += resource + ":" + resourcesToAllocate.get(resource) + " ";
            }
            send(allocationInvokerID, msg);
        }else {
            msg = "FAILED";
            send(clientSocket, msg);
        }
    }
    public void bouncebackSuccess(){
        String msg;
        Socket source;
        if(!isCommunicationNode) {
            msg = "-3";
        }else
            msg = "ALLOCATED";
        try {
            if(!isCommunicationNode) {
                log("INVOKER DATA: " + connectedNodes.get(allocationInvokerID));
                log("INVOKER ID: " + allocationInvokerID);
                send(allocationInvokerID, msg);
            }
            else {
                send(clientSocket, msg);
                log("Alloc succefull, client has been notified.");
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
    public void confirmResourceAllocation(String id) {
        log(id);
        log("IDs");
        log(connectedNodes.keySet().toString());
        ArrayList<String> nodesToBeNotified = new ArrayList<>(connectedNodes.keySet());
        Socket socket;
        for (String checkedID: nodesToBeNotified) {
            if (!id.equals(checkedID)) {
                log("NOTIFYING NODE: " + checkedID);
                send(checkedID, "-6 " + this.ID);
            }
        }
        //todo pending resources
        log("PENDING RESOURCES:" + pendingResources.toString());
        log("RESOURCES:" + resources.toString());
        if(!pendingResources.isEmpty())
            resources = new HashMap<>(pendingResources);
        log("RESOURCES AFTER ALLOC:" + resources.toString());
        reset();
    }

    public void log(String msg){
        System.out.println("[NODE " + ID + "]: " + msg);
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
