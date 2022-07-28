package no.woldseth.evolutionary_algorithm.util;

import no.woldseth.DebugLogger;
import no.woldseth.evolutionary_algorithm.representation.PixelConnectionType;
import no.woldseth.image.Image;
import no.woldseth.image.Pixel;

import java.awt.Point;
import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Stream;

public class PrimsAlgorithm {

    private static Random rng = new Random();
    public static DebugLogger dbl = new DebugLogger(false);
    private static List<PixelConnectionType> connectionTypes = new ArrayList<>(List.of(PixelConnectionType.UP,
                                                                                       PixelConnectionType.DOWN,
                                                                                       PixelConnectionType.LEFT,
                                                                                       PixelConnectionType.RIGHT
                                                                                      ));
    private PriorityQueue<GraphEdge> edgesQue = new PriorityQueue<>();
    private Image image;
    private PixelConnectionType[] currentState;

    private int counter;
    private int counterSelfSet;

    private boolean[] validList = null;
    private static Random rand = new Random();

    private List<GraphEdge> edgeList;

    private PrimsAlgorithm(Image image) {
        this.image          = image;
        this.currentState   = new PixelConnectionType[image.numPixels];
        this.counter        = 0;
        this.counterSelfSet = 200;
        this.edgeList       = new ArrayList<>(image.numPixels * 2);
        //        Arrays.fill(currentState, PixelConnectionType.SELF);
    }

    public static PixelConnectionType[] getSpanningTree(Image image) {
        var prim = new PrimsAlgorithm(image);
        return prim.getSpanningTree();
    }

    public static PixelConnectionType[] connectGroups(Image image, PixelConnectionType[] currentState, int startId) {
        var prim = new PrimsAlgorithm(image);
        prim.currentState = currentState;
        prim.validList    = new boolean[currentState.length];
        int cnt = 0;
        for (int i = 0; i < currentState.length; i++) {
            if (currentState[i] == null) {
                cnt += 1;
                prim.validList[i] = currentState[i] == null;
            }
        }
        //        System.out.println("num null" + cnt);
        if (cnt == 1) {

            for (int i = 0; i < currentState.length; i++) {
                if (currentState[i] == null) {
                    currentState[i] = PixelConnectionType.RIGHT;
                    return currentState;
                }
            }

        }

        return prim.getSpanningTree(startId);
    }

    private PixelConnectionType[] getSpanningTree() {
        int startPoint = rand.nextInt(this.image.numPixels);
        return this.getSpanningTree(startPoint);

    }

    private PixelConnectionType[] getSpanningTree(int startPoint) {

        this.addNode(startPoint, true);
        dbl.log(startPoint);
        //        dbl.dumpStackHere();


        int n = 0;
        while (! edgesQue.isEmpty()) {
            n++;

            GraphEdge minEdge = edgesQue.remove();

            tryConnectEdge(minEdge);
            //            if (n > 100) {
            //                currentState[minEdge.fromNode] = PixelConnectionType.SELF;
            //                                                 n = 0;
            //            }


        }
        //        for (var ct : this.currentState) {
        //            if (ct == null) {
        //                throw new RuntimeException();
        //            }
        //        }
        //        System.out.println(n);
        //        if (n == 1) {
        //            System.out.println(edgesQue.toString());
        //            System.exit(0);
        //        }

        //        edgeList.sort(GraphEdge::compareTo);
        //        boolean stop = false;
        //        int     idx  = 0;
        //        double  avg  = edgeList.stream().mapToDouble(graphEdge -> graphEdge.edgeValue).average().getAsDouble();
        //        while (! stop) {
        //            idx += 1;
        //            //        for (int i = 0; i < 500; i++) {
        //            var edge = edgeList.get(edgeList.size() - idx - 1);
        //            System.out.println(edge.edgeValue);
        //            currentState[edge.fromNode] = PixelConnectionType.SELF;
        //            currentState[edge.toNode]   = PixelConnectionType.SELF;
        //            if (edge.edgeValue < avg * 8) {
        //                stop = true;
        //            }
        //
        //        }
        //        System.out.println("avg value:" + avg);

        return currentState;
    }

    private void tryConnectEdge(GraphEdge edge) {
        if (currentState[edge.fromNode] == null) {
            this.assignEdge(edge.fromNode, edge.toNode);
        } else if (currentState[edge.toNode] == null) {
            this.assignEdge(edge.toNode, edge.fromNode);
        } else {
            dbl.log(edge.fromNode, edge.toNode);
            dbl.log("issues mebbe", currentState[edge.fromNode], currentState[edge.toNode]);

        }

    }

    private void assignEdge(int from, int to) {

        Point fromPoint = image.getIdAsPoint(from);
        Point toPoint   = image.getIdAsPoint(to);

        if (Math.abs(fromPoint.x - toPoint.x) + Math.abs(fromPoint.y - toPoint.y) != 1) {
            throw new RuntimeException();
        }

        PixelConnectionType connectionType;
        if (fromPoint.x > toPoint.x) { // left
            connectionType = PixelConnectionType.LEFT;
        } else if (fromPoint.x < toPoint.x) { // right
            connectionType = PixelConnectionType.RIGHT;
        } else if (fromPoint.y > toPoint.y) { // down
            connectionType = PixelConnectionType.UP;
        } else if (fromPoint.y < toPoint.y) { // up
            connectionType = PixelConnectionType.DOWN;
        } else {
            throw new RuntimeException();
        }

             //   if (this.counter > counterSelfSet) {
             //       connectionType = PixelConnectionType.SELF;
             //       this.counter   = 0;
             //   } else {
             //       this.counter += 1;
             //   }

        currentState[from] = connectionType;

        if (currentState[to] != null) {
            this.addNode(from);
        } else {
            this.addNode(to);
        }
    }

    private void addNode(int nodeId) {
        addNode(nodeId, false);
    }

    private void addNode(int nodeId, boolean initial) {

        Point nodePos = image.getIdAsPoint(nodeId);
        Collections.shuffle(connectionTypes);

        for (var conn : connectionTypes) {
            int newX = nodePos.x;
            int newY = nodePos.y;
            switch (conn) {
                case UP -> {
                    newY -= 1;
                }
                case LEFT -> {
                    newX -= 1;
                }
                case DOWN -> {
                    newY += 1;
                }
                case RIGHT -> {
                    newX += 1;
                }
                case SELF -> {
                    System.out.println("ISSUES");
                }
            }

            dbl.log(nodePos, newX, newY);
            if ((newX >= image.width || newX < 0) || (newY >= image.height || newY < 0)) {
                dbl.log("oob");
                continue;
            }
            int newPointId = image.getPointAsId(newX, newY);

            if (validList != null) {
                if (! validList[newPointId]) {
                    dbl.log("invalid");
                    continue;
                }
            }

            if (currentState[newPointId] == null) {
                double edgeVal = edgeMetric(nodePos.x, nodePos.y, newX, newY);

                double noise_multi = rng.nextDouble(- 0.2, 0.2);
                edgeVal += edgeVal * noise_multi;

                dbl.log("new point id", newPointId);
                GraphEdge newEdge;
                //                this.edgeList.add(newEdge);

                newEdge = new GraphEdge(nodeId, newPointId, edgeVal);
                edgesQue.add(newEdge);
                if (initial) {
                    currentState[nodeId] = conn;
                                           initial = false;
                }
            } else {
                dbl.log("aaa");
            }

        }
    }

    private double edgeMetric(int fromX, int fromY, int toX, int toY) {
        return this.image.getRgbDelta(fromX, fromY, toX, toY);
    }


    private static class GraphEdge implements Comparable<GraphEdge> {
        //        public int lowNode;
        //        public int highNode;

        public int fromNode;
        public int toNode;

        public double edgeValue;

        public GraphEdge(int fromNode, int toNode, double edgeValue) {
            this.fromNode  = fromNode;
            this.toNode    = toNode;
            this.edgeValue = edgeValue;

            //            if (fromNode > toNode) {
            //                highNode = fromNode;
            //                lowNode  = toNode;
            //            } else {
            //                highNode = toNode;
            //                lowNode  = fromNode;
            //            }
        }

        @Override
        public int compareTo(GraphEdge o) {
            return Double.compare(edgeValue, o.edgeValue);
        }

        //        @Override
        //        public boolean equals(Object o) {
        //            if (this == o) {
        //                return true;
        //            }
        //            if (o == null || getClass() != o.getClass()) {
        //                return false;
        //            }
        //            GraphEdge graphEdge = (GraphEdge) o;
        //            return lowNode == graphEdge.lowNode && highNode == graphEdge.highNode;
        //        }
        //
        //        @Override
        //        public int hashCode() {
        //            return Objects.hash(lowNode, highNode);
        //        }
    }


}
