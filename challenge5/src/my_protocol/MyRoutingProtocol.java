package my_protocol;

import framework.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @version 12-03-2019
 *
 * Copyright University of Twente, 2013-2019
 *
 **************************************************************************
 *                            Copyright notice                            *
 *                                                                        *
 *             This file may ONLY be distributed UNMODIFIED.              *
 * In particular, a correct solution to the challenge must NOT be posted  *
 * in public places, to preserve the learning effect for future students. *
 **************************************************************************
 */
public class MyRoutingProtocol implements IRoutingProtocol {
    private LinkLayer linkLayer;
    private int myAddress;

    // You can use this data structure to store your routing table.
    private HashMap<Integer, MyRoute> myRoutingTable = new HashMap<>();

    @Override
    public void init(LinkLayer linkLayer) {
        this.linkLayer = linkLayer;
        this.myAddress = this.linkLayer.getOwnAddress();
        MyRoute myRoute = new MyRoute();
        myRoute.nextHop = this.myAddress;
        myRoute.cost = 0;
        this.myRoutingTable.put(this.myAddress, myRoute);
    }


    @Override
    public void tick(PacketWithLinkCost[] packetsWithLinkCosts) {
        // Get the address of this node

        System.out.println("tick; received " + packetsWithLinkCosts.length + " packets");
        int i;

        DataTable myDT = new DataTable(6);

        // first process the incoming packets; loop over them:
        for (i = 0; i < packetsWithLinkCosts.length; i++) {
            Packet packet = packetsWithLinkCosts[i].getPacket();
            int neighbour = packet.getSourceAddress();             // from whom is the packet?
            int linkcost = packetsWithLinkCosts[i].getLinkCost();  // what's the link cost from/to this neighbour?
            DataTable dt = packet.getDataTable();
            System.out.println("[RECEIVED DT]: " + Arrays.toString(dt.getRow(0)));
            System.out.printf("received packet from %d with %d rows and %d columns of data%n", neighbour, dt.getNRows(), dt.getNColumns());

            Integer[] row = dt.getRow(0);
            for (int j = 0; j<row.length; j++) {
                int cost = row[j];
                if (this.myRoutingTable.get(j+1) != null) {
                    if (cost < this.myRoutingTable.get(j + 1).cost) {
                        MyRoute myRoute = new MyRoute();
                        myRoute.cost = linkcost + cost;
                        myRoute.nextHop = neighbour;
                        this.myRoutingTable.put(j + 1, myRoute);
                    }
                } else {
                    MyRoute myRoute = new MyRoute();
                    myRoute.cost = linkcost + cost;
                    myRoute.nextHop = neighbour;
                    this.myRoutingTable.put(j + 1, myRoute);
                }
            }
        }
        for (int k = 1; k<=6; k++) {
            MyRoute route = this.myRoutingTable.get(k);
            if (route != null) {
                myDT.set(0, k-1, route.cost);
            } else {
                myDT.set(0, k-1, 1000);
            }
        }
        System.out.println("[SENDING DT: " + this.myAddress + "] : " + Arrays.toString(myDT.getRow(0)));

        Packet pkt = new Packet(myAddress, 0, myDT);
        this.linkLayer.transmit(pkt);
    }

    public Map<Integer, Integer> getForwardingTable() {
        // This code extracts from your routing table the forwarding table.
        // The result of this method is send to the server to validate and score your protocol.

        // <Destination, NextHop>
        HashMap<Integer, Integer> ft = new HashMap<>();

        for (Map.Entry<Integer, MyRoute> entry : myRoutingTable.entrySet()) {
            ft.put(entry.getKey(), entry.getValue().nextHop);
        }

        return ft;
    }
}
