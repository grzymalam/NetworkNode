package com.S22658;

import java.util.HashMap;

public class Main {

    public static void main(String[] args) {
        HashMap<String, Integer> resources = new HashMap<>();
        resources.put("A", 3);
        resources.put("B", 2);
        new Thread(new Node("123", 4444, resources)).start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        HashMap<String, Integer> resources2 = new HashMap<>();
        resources.put("A", 10);
        resources.put("C", 7);
        new Thread(new Node("1337", 4454, "localhost", 4444, resources2)).start();

    }
}