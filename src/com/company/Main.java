package com.company;

public class Main {

    public static void main(String[] args) {
        Network net = new Network();
        net.readNetwork("we.txt");
        net.useAlgorithm();
        System.out.println();
        //System.out.println("Following are the edges in " +
        //      "the constructed MST");
        net.showNet();

    }
}

