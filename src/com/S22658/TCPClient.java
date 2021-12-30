package com.S22658;

import java.net.*;
import java.util.LinkedList;
import java.io.*;

/**
 * TCPClient is the clientside wrapper that can receive and send strings.
 */
public class TCPClient extends Thread {

    private Socket socket = null;
    private BufferedReader input = null;
    private PrintWriter output = null;
    private boolean silentMode = false;
    /**
     * The data in TCP messages has no line terminators.
     */
    private LinkedList<String> received = new LinkedList<>();

    public void connect(String address, int port){

        while(true){
            try {
                socket = new Socket(address, port);

                if(!silentMode)
                    System.out.println("CLIENT START");

                output = new PrintWriter(socket.getOutputStream(), true);
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                new Thread(this).start();
                break;
            } catch (IOException e){
                System.out.println("While connecting to: " + address + ":" + port);
                System.out.println(e);
                System.out.println("Retrying in 5s");
                try{
                    Thread.sleep(5000);
                } catch (InterruptedException f){
                    System.out.println(f);
                    break;
                }
            }
        }

    }

    /**
     * Creates the client instance, needs to connect to a specified server.
     * @param address Server's IPv4 address
     * @param port Server's port
     */
    public TCPClient(String address, int port){
        connect(address, port);
    }

    /**
     * Unconnected constructor
     */
    public TCPClient(){};

    /**
     * Gives you the local port automatically assigned to create the connection.
     * @return the local port you used to make the connection.
     * @since 1.0.3
     */
    public int getOwnPort(){
        return socket.getLocalPort();
    }

    /**
     * Gives you the current socket for direct interaction.
     * @return the socket
     * @since 1.0.3
     */
    public Socket getSocket(){
        return socket;
    }

    /**
     * In silent mode, only errors are printed.
     * @param shouldBeSilent true to enable, false to disable.
     * @since 1.0.3
     */
    public void setSilentMode(boolean shouldBeSilent){
        this.silentMode = shouldBeSilent;
    }

    /**
     * You can use this method to send a message to the server you have connected to.
     * @param message the message to be sent
     */
    public void send(Object message){

        if(output == null){
            System.out.println("Can't send when unconnected! Connect first!");
            return;
        }

        output.println(String.valueOf(message));
    }

    /**
     *
     * @return oldest received message
     */
    public String get(){
        if(received.size() > 0){
            return received.removeFirst();
        } else {
            System.out.println("Tried to read empty received buffer, returning ''");
            return null;
        }
    }

    public void run() {
        while(true){
            if(input != null){
                try {
                    String receivedStr = input.readLine();
                    received.add(receivedStr);
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        }
    }

}