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
    private static final int NODES = 6;
    private static final int INF = 500; // represents infinite, as 100 is the highest cost link we dont expect to go over this

    // You can use this data structure to store your routing table.
    private HashMap<Integer, MyRoute> myRoutingTable = new HashMap<>();
    private DataTable myTable = new DataTable(6);

    @Override
    public void init(LinkLayer linkLayer) {
        this.linkLayer = linkLayer;
        this.myAddress = this.linkLayer.getOwnAddress();
        updateMyRoutingTable(0, this.myAddress, this.myAddress);
        for (int i = 1; i <=NODES; i++) {
            Integer[] values = new Integer[NODES];
            Arrays.fill(values, INF);
            if (i == this.myAddress) {
                values[i-1] = 0;
            }
            this.myTable.setRow(i-1, values);
        }
    }


    @Override
    public void tick(PacketWithLinkCost[] packetsWithLinkCosts) {
        // Get the address of this node

        System.out.println("tick; received " + packetsWithLinkCosts.length + " packets");
        int i;

        // first process the incoming packets; loop over them:
        for (i = 0; i < packetsWithLinkCosts.length; i++) {
            Packet packet = packetsWithLinkCosts[i].getPacket();
            int neighbour = packet.getSourceAddress();             // from whom is the packet?
            int linkcost = packetsWithLinkCosts[i].getLinkCost();  // what's the link cost from/to this neighbour?
            DataTable dt = packet.getDataTable();
            System.out.printf("received packet from %d with %d rows and %d columns of data%n", neighbour, dt.getNRows(), dt.getNColumns());
            //System.out.println("[RECEIVING DT] : \n" + dataTableToString(dt));

            updateTable(dt);
            for (int l = 0; l<dt.getNRows(); l++) {
                Integer[] row = dt.getRow(l);
                if (l == neighbour - 1) {
                    for (int j = 0; j < row.length; j++) {
                        int cost = row[j];
                        if (this.myRoutingTable.get(j + 1) != null) {
                            if (cost + linkcost < this.myRoutingTable.get(j + 1).cost) {
                                updateMyRoutingTable(linkcost + cost, neighbour, j + 1);
                            }
                            else if (this.myRoutingTable.get(j + 1).nextHop == neighbour) {
                                updateMyRoutingTable(linkcost + cost, neighbour, j + 1);
                            }
                        } else {
                            updateMyRoutingTable(linkcost + cost, neighbour, j + 1);
                        }
                        if (this.myRoutingTable.get(j + 1).nextHop == neighbour) {
                            this.myRoutingTable.get(j + 1).TTL++;
                        }
                    }
                }
            }
        }
        decrementTTL();
        for (int k =1; k<=NODES; k++) {
            MyRoute route = this.myRoutingTable.get(k);
            if (route != null) {
                if (route.TTL <= 0) {

                    for (int m = 1; m <= 6; m++) {
                        if (this.myRoutingTable.get(m) != null) {
                            if (this.myRoutingTable.get(m).nextHop == route.nextHop) {
                                this.myRoutingTable.remove(m);
                            }
                        }
                    }
                    this.myRoutingTable.remove(k);
//                    updateMyRoutingTable(INF, route.nextHop, k);
//                    this.myRoutingTable = new HashMap<>();
//                    updateMyRoutingTable(0, this.myAddress, this.myAddress);
                }
            }
        }


        for (int k = 1; k<=NODES; k++) {
            MyRoute route = this.myRoutingTable.get(k);
            if (route != null) {
                myTable.set(this.myAddress - 1, k - 1, route.cost);
            } else {
                myTable.set(this.myAddress - 1, k-1, INF);
            }
        }
        //System.out.println("[SENDING DT: " + this.myAddress + "] : \n" + dataTableToString(myTable));

        for (int q = 1; q <= NODES; q++) {
            if (q != this.myAddress) {
                DataTable dt;
                dt = deepCopy(this.myTable);
                Packet pkt = new Packet(myAddress, q, splitHorizon(dt, q));
                this.linkLayer.transmit(pkt);
            }
        }

    }

    public void decrementTTL() {
        for (int i = 1; i <= NODES; i++) {
            if (this.myRoutingTable.get(i) != null) {
                if (i != this.myAddress) {
                    this.myRoutingTable.get(i).TTL--;
                }
            }
        }
    }

    public DataTable splitHorizon(DataTable dt, int sendTo) {
        for (int i = 1; i <= NODES; i++) {
            MyRoute myRoute = this.myRoutingTable.get(i);
            if (myRoute != null) {
                if (myRoute.nextHop == sendTo) {
                    dt.set(this.myAddress - 1, sendTo - 1, 1000);
                }
            }
        }
        return dt;
    }

    public String dataTableToString(DataTable dt) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i<NODES; i++) {
            s.append(Arrays.toString(dt.getRow(i))).append("\n");
        }
        return s.toString();
    }

    public DataTable deepCopy(DataTable dt) {
        DataTable newDataTable = new DataTable(dt.getNColumns());
        for (int i = 0; i< dt.getNRows(); i++) {
            Integer[] row = new Integer[dt.getNColumns()];
            System.arraycopy(dt.getRow(i), 0, row, 0, row.length);
            newDataTable.setRow(i, row);
        }
        return newDataTable;
    }

    public void updateMyRoutingTable(int cost, int nextHop, int node) {
        MyRoute myRoute = new MyRoute();
        myRoute.cost = cost;
        myRoute.nextHop = nextHop;
        this.myRoutingTable.put(node, myRoute);
    }

    public void updateTable(DataTable dt) {
        for (int i = 0; i < dt.getNRows(); i++) {
            if (i != this.myAddress - 1) {
                myTable.setRow(i, dt.getRow(i));
            }
        }
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
