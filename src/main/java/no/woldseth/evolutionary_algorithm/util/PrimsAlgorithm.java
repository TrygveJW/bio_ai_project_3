package no.woldseth.evolutionary_algorithm.util;

import no.woldseth.DebugLogger;
import no.woldseth.evolutionary_algorithm.representation.PixelConnectionType;
import no.woldseth.image.Image;

import java.awt.Point;
import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Stream;

public class PrimsAlgorithm {

    private static DebugLogger dbl = new DebugLogger(false);
    private static List<PixelConnectionType> connectionTypes = new ArrayList<>(List.of(PixelConnectionType.UP,
                                                                                       PixelConnectionType.DOWN,
                                                                                       PixelConnectionType.LEFT,
                                                                                       PixelConnectionType.RIGHT
                                                                                      ));
    private PriorityQueue<GraphEdge> edgesQue = new PriorityQueue<>();
    private Image image;
    private PixelConnectionType[] currentState;

    private static Random rand = new Random();

    private PrimsAlgorithm(Image image) {
        this.image        = image;
        this.currentState = new PixelConnectionType[image.numPixels];
        //        Arrays.fill(currentState, PixelConnectionType.SELF);
    }

    public static PixelConnectionType[] getSpanningTree(Image image) {
        var prim = new PrimsAlgorithm(image);
        return prim.getSpanningTree();
    }

    private PixelConnectionType[] getSpanningTree() {
        int startPoint = rand.nextInt(this.image.numPixels);

        this.addNode(startPoint);
        //        this.addNode(52);

        //        GraphEdge iedge = edgesQue.remove();
        //        tryConnectEdge(iedge);
        //        while (! edgesQue.isEmpty()) {
        //            System.out.println(edgesQue.size());
        //            GraphEdge edge = edgesQue.remove();
        //
        //            dbl.log(edge.edgeValue, edge.fromNode, edge.toNode);
        //        }
        //        dbl.log(Arrays.copyOfRange(this.currentState, 0, 50));
        //        dbl.log(Arrays.copyOfRange(this.currentState, 50, 100));
        //        System.exit(0);

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

        currentState[from] = connectionType;

        if (currentState[to] != null) {
            this.addNode(from);
        } else if (currentState[from] == null) {
            this.addNode(to);
        }
    }

    private void addNode(int nodeId) {

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
                dbl.log();
                continue;
            }
            int newPointId = image.getPointAsId(newX, newY);

            if (currentState[newPointId] == null) {
                double edgeVal = edgeMetric(nodePos.x, nodePos.y, newX, newY);
                dbl.log("new point id", newPointId);
                GraphEdge newEdge = new GraphEdge(nodeId, newPointId, edgeVal);
                edgesQue.add(newEdge);
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
