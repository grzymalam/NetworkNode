package com.S22658;

import java.util.HashMap;

public class Main {

    public static void main(String[] args) {
        HashMap<String, Integer> resources = new HashMap<>();
        resources.put("A", 3);
        resources.put("B", 2);
	    new Thread(new Node("123", 4444, resources)).start();
    }
}