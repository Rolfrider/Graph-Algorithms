package com.company;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import java.io.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;


public class Network {
    private Vertex [] vertices;
    private Edge []edges;
    private String algorithm;
    private Path shortestPath = null;
    private int startPath , endPath;
    private Path mstPath = null;
    private ArrayList<Pair> floydPts = new ArrayList<>();

    private final static int INF = Integer.MAX_VALUE;
    private final String MST = "MST",
            FLOYD = "FLOYD",
            SCIEZKA ="SCIEZKA";

    private String styleSheet =
            "node {" +
                    "	fill-color: black;" +
                    "   size: 15px;" +
                    "   text-size: 20 ;"+
                    "}" +
                    "node.marked {" +
                    "	fill-color: blue;" +
                    "   text-color: blue ;"+
                    "}"+
             "edge {" +
                    "  fill-color: black;" +
                    "text-size: 20 ;"+
                    "}" +
                    "edge.marked {" +
                    "  fill-color: blue;" +
                    "   text-color: blue ;"+
                    "   size: 3px;"+
                    "}";

    public void readNetwork( String fileName){
        String line = null;

        try{
            // FileReader reads text files in the default encoding.
            FileReader fileReader = new FileReader(fileName);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            int nV = 0 , iV = 0, nE = 0, iE = 0;
            while((line = bufferedReader.readLine()) != null) {
                if(line.startsWith("#"))
                    continue;

                if(line.contains("WEZLY")){
                    vertices = new Vertex[takeNumOf(line)];
                    nV = vertices.length;
                    iV =0;
                    continue;
                }

                if( nV > iV){
                    vertices[iV] = updateVertex(line);
                    iV++;
                    continue;
                }

                if(line.contains("LACZA")){
                    edges = new Edge[takeNumOf(line)];
                    nE = edges.length;
                    iE =0;
                    continue;
                }

                if( nE > iE){
                    edges[iE] = updateEdge(line);
                    iE++;
                    continue;
                }

                if(line.contains("ALGORYTM")){
                    String[] parts = line.split("= ");
                    algorithm = parts[1];
                    continue;
                }

                if(algorithm.equals(SCIEZKA)){
                    String[] parts = line.split(" ");
                    startPath = Integer.parseInt(parts[0]);
                    endPath = Integer.parseInt(parts[1]);
                }

                if(algorithm.equals(FLOYD)){
                    do{
                    String[] parts = line.split(" ");
                    floydPts.add(new Pair(Integer.parseInt(parts[0]), Integer.parseInt(parts[1])));
                    }while((line = bufferedReader.readLine()) != null && !line.startsWith("#"));
                }
                //for (int i = 0; i < floydPts.size(); i++)
                //    System.out.println(floydPts.get(i).getLeft() + " " + floydPts.get(i).getRight());
            }

            // Always close files.
            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                    "Unable to open file '" +
                            fileName + "'");
        }
        catch(IOException ex) {
            System.out.println(
                    "Error reading file '"
                            + fileName + "'");
            // Or we could just do this:
            // ex.printStackTrace();
        }
    }

    public void useAlgorithm(){
        switch (algorithm){
            case MST:
                mstPath = Algorithm.KruskalMST(vertices, edges);
                break;
            case SCIEZKA:
                shortestPath = Algorithm.Dijkstra(startPath, endPath,vertices, edges);
                break;
            case FLOYD:
                Algorithm.Floyd(toMatrix(), vertices.length, floydPts);
                break;
        }
    }

    public void showNet(){
        System.out.println(vertices.length);
        System.out.println(edges.length);
        System.out.println(algorithm);
        for (int i = 0; i < edges.length; ++i) {
            System.out.println("Id: " + edges[i].getId() + " | " + edges[i].getStartVertex() + " -- " +
                    edges[i].getEndVertex() + " == " + edges[i].getValue());
        }

        Graph graph = new SingleGraph("Net") ;

        graph.addAttribute("ui.antialias");
        graph.addAttribute("ui.stylesheet", styleSheet);

        int i ;
        for (i = 0 ; i < vertices.length ; i++){
            Node n = graph.addNode(vertices[i].getId() + "");
            n.addAttribute("ui.label", "V" + vertices[i].getId());
            n.addAttribute("x", vertices[i].getX());
            n.addAttribute("y", vertices[i].getY());

        }
        for (i = 0; i < edges.length; i++){
            org.graphstream.graph.Edge e = graph.addEdge(edges[i].getId() +"", edges[i].getStartVertex() +"", edges[i].getEndVertex() +"");
            e.addAttribute("ui.label",""+ edges[i].getValue() );
        }

        graph.display();

        Path path = null;
        if (shortestPath != null)
            path = shortestPath;
        else if(mstPath != null)
            path = mstPath;

        if(path != null){
            for (Integer id: path.vertexesId){
                Node n = graph.getNode(id + "");
                n.addAttribute("ui.class", "marked");
            }
            for (Integer id: path.edgesId){
                org.graphstream.graph.Edge e = graph.getEdge(id +"");
                e.addAttribute("ui.class", "marked");
            }
           // graph.display();
        }
    }

    private int[][] toMatrix(){
        int size = vertices.length;
        int[][] graph = new int [size][size];

        for (int i = 0; i < size; i++ ) {
            for (int n = 0; n < size; n++ ){
                graph[i][n] = INF;
            }
            graph[i][i] = 0;
        }

        for (Edge edge : edges){
            int from = edge.getStartVertex() - 1; // -1 because Id starts at 1 and arrays start from 0
            int to  = edge.getEndVertex() - 1;
            int val = edge.getValue();
            graph[from][to] = val;
            graph[to][from] = val;
        }

        //System.out.println("Following matrix shows the values of existing edges");
        //printMatrix(graph);

        return graph;
    }

    private int takeNumOf(String line){
        String[] parts =line.split(" ");
        int i = Integer.parseInt(parts[2]);
        return i;
    }

    private Vertex updateVertex(String line){
        String[] parts =line.split(" ");
        return new Vertex(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2]));
    }

    private Edge updateEdge(String line){
        String[] parts =line.split(" ");
        int startId =Integer.parseInt(parts[1]),
                endId =Integer.parseInt(parts[2]);
        return new Edge(Integer.parseInt(parts[0]), startId,
                endId, calcValue(startId, endId) );
    }

    private int calcValue(int startId, int endId){
        double dx = vertices[startId - 1].getX() - vertices[endId - 1].getX();
        double dy = vertices[startId-1].getY() - vertices[endId-1].getY();
        Double v = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
        return v.intValue();
    }

    public class Pair {
        private int left;
        private int right;

        public Pair(int left, int right) {
            this.left = left;
            this.right = right;
        }

        public int getLeft() {
            return left;
        }
        public int getRight() {
            return right;
        }
    }
}
