package com.S22658;

public class AddressWrapper {
    private final String address;
    private final int port;

    public AddressWrapper(String address, int port) {
        this.address = address;
        this.port = port;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString(){
        return "IP: " + address + " PORT: " + port;
    }
}
