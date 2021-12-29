package com.S22658;

public class Client implements Comparable<Client>{
    private String ip;
    private int port;

    public Client(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public String getIp() {
        return ip;
    }

    @Override
    public int compareTo(Client o) {
        if(ip.equals(o.getIp()) && port == o.getPort())
            return 0;
        return -1;
    }
}
