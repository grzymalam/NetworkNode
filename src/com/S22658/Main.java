package com.S22658;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) {
        HashMap<String, Integer> resources = new HashMap<>();
        resources.put("A", 3);
        resources.put("B", 2);
        Node pierwszy = new Node("2137", 4656, resources);
        new Thread(pierwszy).start();
        HashMap<String, Integer> resources2 = new HashMap<>();
        resources2.put("A", 10);
        resources2.put("C", 7);
        Node node2 = new Node("1337", 4765, "localhost", 4656, resources2);
        new Thread(node2).start();
        HashMap<String, Integer> resources3 = new HashMap<>();
        resources3.put("A", 2);
        resources3.put("C", 6);
        Node node3 = new Node("1234", 4768, "localhost", 4656, resources3);
        new Thread(node3).start();
        //pierwszy.getConnectedNodes().forEach((id, lista) -> System.out.println("ID: " + id));
        HashMap<String, Integer> resources4 = new HashMap<>();
        resources4.put("B", 17);
        resources4.put("D", 4);
        Node node4 = new Node("1989", 4878, "localhost", 4768, resources4);
        new Thread(node4).start();
        HashMap<String, Integer> resources5 = new HashMap<>();
        resources5.put("F", 19);
        Node node5 = new Node("2000", 4989, "localhost", 4768, resources5);
        new Thread(node5).start();
        ArrayList<Node> noudy = new ArrayList<>(Arrays.asList(pierwszy, node2, node3, node4, node5));
        new Thread()
        {
            public void run() {
                TCPClient client = new TCPClient("localhost", 4656);
                client.send("2000 A:10 B:1 C:3 D:1 F:10");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("CLIENT1 RECEIVED: " + client.get());
            }
        }.start();
        new Thread()
        {
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                TCPClient client2 = new TCPClient("localhost", 4878);
                client2.send("2137 A:2 B:5");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("CLIENT2 RECEIVED: " + client2.get());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                noudy.forEach(node -> System.out.println(node.getID() + ": " + node.getResources().keySet() + node.getResources().values()));
            }
        }.start();
    }
}