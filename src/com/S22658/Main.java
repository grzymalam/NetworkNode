package com.S22658;

import java.util.HashMap;

public class Main {

    public static void main(String[] args) {
        HashMap<String, Integer> resources = new HashMap<>();
        resources.put("A", 3);
        resources.put("B", 2);
        Node pierwszy = new Node("123", 4444, resources);
        new Thread(pierwszy).start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        HashMap<String, Integer> resources2 = new HashMap<>();
        resources2.put("A", 10);
        resources2.put("C", 7);
        new Thread(new Node("1337", 4454, "localhost", 4444, resources2)).start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        HashMap<String, Integer> resources3 = new HashMap<>();
        resources3.put("A", 10);
        resources3.put("C", 7);
        new Thread(new Node("1234", 4467, "localhost", 4444, resources3)).start();
        System.out.println("Nody 1szego noda");
        //pierwszy.getConnectedNodes().forEach((id, lista) -> System.out.println("ID: " + id));
        TCPClient client = new TCPClient("localhost", 4444);
        client.send("2137 A:4");
    }
}