package com.S22658;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//wezel systemu rozproszonego
public class Node implements Runnable {
    private final String ID;
    private final int port;
    private String address;
    private String allocationInvokerID;
    private ServerSocket communicationSocket;
    private Socket clientSocket;
    private final Sender sender = new Sender();
    private HashMap<String, Integer> resources;
    private HashMap<String, Integer> pendingResources = new HashMap<>();
    private HashMap<String, AddressWrapper> connectedNodes = new HashMap<>();
    private ArrayList<String> nodesLeftToCheck = new ArrayList<>();
    private boolean isCommunicationNode = false;
    private boolean isSilent = false;

    //podlaczanie do istniejacej sieci
    public Node(String id, int port, String nodeNetworkIP, int nodeNetworkPort, HashMap<String, Integer> resources) {
        this.ID = id;
        this.port = port;
        this.resources = resources;
        try {
            Socket nodeNetworkSocket = new Socket(InetAddress.getByName(nodeNetworkIP), nodeNetworkPort);
            communicationSocket = new ServerSocket(port);
            communicationSocket.setReuseAddress(true);
            address = communicationSocket.getInetAddress().getHostAddress();
            PrintWriter writer = new PrintWriter(nodeNetworkSocket.getOutputStream(), true);
            writer.println("-4 " + id + " " + address + " " + port);
            log("podlaczyl sie do wezla o ip: " + nodeNetworkIP + " i porcie: " + nodeNetworkPort);
        } catch (IOException e) {
            log("Nie udalo sie podlaczyc do sieci");
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
            log("Nie udalo sie podlaczyc do sieci");
        }
    }

    public void reset() {
        log("Resetting");
        if (isCommunicationNode) {
            isCommunicationNode = false;
            closeClientsocket();
        }
        fillNodesToCheck();
        pendingResources = new HashMap<>();
    }

    public String getID() {
        return ID;
    }

    public HashMap<String, Integer> getResources() {
        return resources;
    }

    public void setClientSocket(Socket socket) {
        isCommunicationNode = true;
        clientSocket = socket;
    }
    public void setSilent(boolean b){
        isSilent = b;
    }
    //nody dostepne do sprawdzenia sa resetowane po sukcesie
    public void addConnectedNode(String id, AddressWrapper wrapper) {
        log("Adding new node and sending own id to it");
        connectedNodes.put(id, wrapper);
        nodesLeftToCheck.add(id);
        sendSelfId(wrapper);
    }

    public void addConnectingNodeToConnectedNodes(String id, AddressWrapper wrapper) {
        log("Adding new node that we asked to be connected to.");
        connectedNodes.put(id, wrapper);
        nodesLeftToCheck.add(id);
    }

    public void fillNodesToCheck() {
        nodesLeftToCheck = new ArrayList<>(connectedNodes.keySet());
    }

    public void removeFailedNode(String id, HashMap<String, Integer> resourcesToAllocate, String allocatedResourcesByNode) {
        nodesLeftToCheck.remove(id);
        checkNextChildNode(resourcesToAllocate, allocatedResourcesByNode);
    }

    public void allocateResources(HashMap<String, Integer> resourcesToAllocate, String ID, String allocatedResourcesByNode) {
        fillNodesToCheck();
        nodesLeftToCheck.remove(ID);
        allocationInvokerID = ID;
        HashMap<String, Integer> resourceCopy = new HashMap<>(resources);
        HashMap<String, Integer> resourcesToAllocateAfterAllocation = new HashMap<>();
        log("NODE [" + this.ID + "] allocating resources for: " + ID);
        log("AVAL resources: " + resourceCopy.keySet());
        log("AVAL resources: " + resourceCopy.values());
        log("resources to be allocated: " + resourcesToAllocate.keySet());
        log("resources to be allocated: " + resourcesToAllocate.values());
        //proba alokacji w current wezle
        for (String resource : resourcesToAllocate.keySet()) {
            int amountOfResourceToBeAllocated = resourcesToAllocate.get(resource);
            if (resourceCopy.containsKey(resource)) {
                int diff = resourceCopy.get(resource) - amountOfResourceToBeAllocated;
                log("NODE [" + this.ID + "]: diff: " + diff);
                log("NODE [" + this.ID + "]: node's " + resource + " resource amount: " + resourceCopy.get(resource));
                log("NODE [" + this.ID + "]: node's requested " + resource + "resource amount: " + amountOfResourceToBeAllocated);
                if (diff >= 0) {
                    //node posiada wystarczajaca ilosc zasobu
                    resourceCopy.replace(resource, diff);
                    log(resource + " allocation successful.");
                    log(resource + " resource left in node: " + diff);
                } else {
                    //jezeli node nie mial wystarczajacej ilosci zasobu
                    resourceCopy.replace(resource, 0);
                    resourcesToAllocateAfterAllocation.put(resource, diff * -1);
                    log("node doesnt have enough resources to allocate.");
                    log("resource left to alloc: " + resourcesToAllocateAfterAllocation.get(resource));
                }
            } else {
                log("NODE DOESNT CONTAIN THE RESOURCE: " + resource);
                resourcesToAllocateAfterAllocation.put(resource, resourcesToAllocate.get(resource));
            }
        }

        log("Putting pending changes: " + resourceCopy.keySet() + " " + resourceCopy.values());
        pendingResources = resourceCopy;
        allocatedResourcesByNode += getAllocatedResources(pendingResources);
        log("Allocated: " + allocatedResourcesByNode);

        if (resourcesToAllocateAfterAllocation.size() == 0) {
            log("success");
            bouncebackSuccess(allocatedResourcesByNode);
        } else if(nodesLeftToCheck.size() == 0) {
            log("Nodes left to check : " + nodesLeftToCheck);
            bouncebackFailed(resourcesToAllocateAfterAllocation, allocatedResourcesByNode);
        } else
            checkNextChildNode(resourcesToAllocateAfterAllocation, allocatedResourcesByNode);
    }
    private String getAllocatedResources(HashMap<String, Integer> resourcesAfterAlloc){
        String toReturn = "";
        int val;
        for(Map.Entry<String, Integer> entry: resources.entrySet()){
            if((val = entry.getValue()-pendingResources.get(entry.getKey())) > 0 && pendingResources.containsKey(entry.getKey())){
                toReturn+=entry.getKey() + ":" + val + "&";
            }
        }

        if(!toReturn.equals("")) {
            toReturn = ID + "&" + toReturn;
            StringBuilder builder = new StringBuilder(toReturn);
            builder.setCharAt(builder.length()-1, '%');
            toReturn = builder.toString();
        }
        return toReturn;
    }
    public void sendSelfId(AddressWrapper wrapper) {
        String msg = "-5 " + ID + " " + address + " " + port;
        log("WRAPPER: " + wrapper);
        log("MESSAGE: " + msg);
        sender.send(wrapper, msg);
        log("Sent own id.");
    }

    public void checkNextChildNode(HashMap<String, Integer> resourcesToAllocate, String allocatedResourcesByNode) {
        AddressWrapper wrapper;
        log("NODES LEFT TO CHECK: " + nodesLeftToCheck);
        log("resources to be allocated: " + resourcesToAllocate.keySet().toString());
        log("resources to be allocated: " + resourcesToAllocate.values().toString());
        if (nodesLeftToCheck.size() > 0) {
            String nodeToCheckID = nodesLeftToCheck.get(0);
            log("CHECKING NODE " + nodeToCheckID);
            String msg = "-1 " + this.ID + " ";
            for (String resource : resourcesToAllocate.keySet()) {
                msg += resource + ":" + resourcesToAllocate.get(resource) + " ";
            }
            msg += allocatedResourcesByNode;
            wrapper = connectedNodes.get(nodeToCheckID);
            sender.send(wrapper.getAddress(), wrapper.getPort(), msg);
            log("Message sent");
            nodesLeftToCheck.remove(nodeToCheckID);
        } else {
            bouncebackFailed(resourcesToAllocate, allocatedResourcesByNode);
            log("Allocation failed. Not enough resources. Changes have not been saved");
        }
    }

    public void bouncebackFailed(HashMap<String, Integer> resourcesToAllocate, String allocatedResourcesByNode) {
        String msg = "-2 " + ID + " ";
        AddressWrapper wrapper;
        if (!isCommunicationNode) {
            for (String resource : resourcesToAllocate.keySet()) {
                msg += resource + ":" + resourcesToAllocate.get(resource) + " ";
            }
            msg += allocatedResourcesByNode;
            log("bouncebackFailed " + msg);
            wrapper = connectedNodes.get(allocationInvokerID);
            sender.send(wrapper.getAddress(), wrapper.getPort(), msg);
        } else {
            log("SENDING FAILED");
            sender.send(clientSocket, "FAILED");
            broadcast(ID, BROADCAST_TYPE.FAILURE);
        }
    }

    public void bouncebackSuccess(String allocatedResourcesByNode) {
        String msg;
        AddressWrapper wrapper;
        log("bouncebackSuccess " + allocatedResourcesByNode);
        try {
            if (isCommunicationNode) {
                msg = "ALLOCATED ";
                sender.send(clientSocket, msg);
                for(String toSend: allocatedResourcesByNode.split("%")){
                    toSend = toSend.replaceAll("&", " ");
                    log("SENDING: " + toSend);
                    sender.send(clientSocket, toSend);
                }
                closeClientsocket();
                log("Alloc successful, client has been notified.");
                broadcast(ID, BROADCAST_TYPE.SUCCESS);
            } else {
                msg = "-3 ";
                wrapper = connectedNodes.get(allocationInvokerID);
                sender.send(wrapper.getAddress(), wrapper.getPort(), msg + allocatedResourcesByNode);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
    public void broadcast(String id, BROADCAST_TYPE btype){
        AddressWrapper wrapper;
        String msg = "";
        boolean terminate = false;
        log("Broadcasting.");
        ArrayList<String> nodesToBeNotified = new ArrayList<>(connectedNodes.keySet());
        nodesToBeNotified.remove(id);
        switch (btype){
            case SUCCESS: resources = pendingResources.isEmpty() ? resources : new HashMap<>(pendingResources); msg = "-6 "; break;
            case FAILURE: pendingResources = resources; msg = "-7 "; break;
            case TERMINATION: msg = "-8 "; terminate = true; break;
        }
        for (String checkedID : nodesToBeNotified) {
            log("NOTIFYING NODE: " + checkedID);
            wrapper = connectedNodes.get(checkedID);
            sender.send(wrapper.getAddress(), wrapper.getPort(), msg + this.ID);
        }
        if(terminate)
            System.exit(0);
        reset();
    }
    private void closeClientsocket(){
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void log(String msg) {
        if(!isSilent)
            System.out.println("[NODE " + ID + "]: " + msg);
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket newConnection = communicationSocket.accept();
                new Thread(new Connection(newConnection, this)).start();
            } catch (IOException e) {
                log("blad odbioru danych.");
            }
        }
    }
    public enum BROADCAST_TYPE{
        TERMINATION, SUCCESS, FAILURE
    }
}