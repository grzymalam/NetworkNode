package com.S22658;

import java.io.PrintWriter;
import java.net.Socket;

public class Sender {
    public Sender(){}
    public void send(String address, int port, String message) {
        PrintWriter writer;
        Socket socket;
        try {
            socket = new Socket(address, port);
            writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println(message);
            socket.close();
        } catch (Exception e) {
            System.out.println("Error sending message. STRING/STRING");
        }
    }

    public void send(AddressWrapper wrapper, String message) {
        PrintWriter writer;
        Socket socket;
        try {
            socket = new Socket(wrapper.getAddress(), wrapper.getPort());
            writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println(message);
            socket.close();
        } catch (Exception e) {
            System.out.println("Error sending message. ADDRESSWRAPPER/STRING");
        }
    }

    public void send(Socket socket, String message) {
        PrintWriter writer;
        try {
            writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println(message);
        } catch (Exception e) {
            System.out.println("Error sending message. SOCKET/STRING");
        }
    }
}
