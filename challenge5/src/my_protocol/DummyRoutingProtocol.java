package my_protocol;

import framework.*;

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
public class DummyRoutingProtocol implements IRoutingProtocol {
    private LinkLayer linkLayer;

    // You can use this data structure to store your routing table.
    private HashMap<Integer, DummyRoute> myRoutingTable = new HashMap<>();

    @Override
    public void init(LinkLayer linkLayer) {
        this.linkLayer = linkLayer;
    }


    @Override
    public void tick(PacketWithLinkCost[] packetsWithLinkCosts) {
        // Get the address of this node
        int myAddress = this.linkLayer.getOwnAddress();

        System.out.println("tick; received " + packetsWithLinkCosts.length + " packets");
        int i;

        // first process the incoming packets; loop over them:
        for (i = 0; i < packetsWithLinkCosts.length; i++) {
            Packet packet = packetsWithLinkCosts[i].getPacket();
            int neighbour = packet.getSourceAddress();             // from whom is the packet?
            int linkcost = packetsWithLinkCosts[i].getLinkCost();  // what's the link cost from/to this neighbour?
            DataTable dt = packet.getDataTable();                  // other data contained in the packet
            System.out.printf("received packet from %d with %d rows and %d columns of data%n", neighbour, dt.getNRows(), dt.getNColumns());

            // you'll probably want to process the data, update your data structures (myRoutingTable) , etc....

            // reading one cell from the DataTable can be done using the  dt.get(row,column)  method

           /* example code for inserting a route into myRoutingTable:
               DummyRoute r = new DummyRoute();
               r.nextHop = ...someneighbour...;
               myRoutingTable.put(...somedestination... , r);
           */

           /* example code for checking whether some destination is already in myRoutingTable, and accessing it:
               if (myRoutingTable.containsKey(dest)) {
                   DummyRoute r = myRoutingTable.get(dest);
                   // do something with r.cost and r.nextHop; you can even modify them
               }
           */

        }

        // and send out one (or more, if you want) distance vector packets
        // the actual distance vector data must be stored in the DataTable structure
        DataTable dt = new DataTable(6);   // the 6 is the number of columns, you can change this
        // you'll probably want to put some useful information into dt here
        // by using the  dt.set(row, column, value)  method.

        // next, actually send out the packet, with our own address as the source address
        // and 0 as the destination address: that's a broadcast to be received by all neighbours.
        Packet pkt = new Packet(myAddress, 0, dt);
        this.linkLayer.transmit(pkt);

        /*
        Instead of using Packet with a DataTable you may also use Packet with
        a byte[] as data part, if you really want to send your own data structure yourself.
        Read the JavaDoc of Packet to see how you can do this.
        PLEASE NOTE! Although we provide this option we do not support it.
        */
    }

    public Map<Integer, Integer> getForwardingTable() {
        // This code extracts from your routing table the forwarding table.
        // The result of this method is send to the server to validate and score your protocol.

        // <Destination, NextHop>
        HashMap<Integer, Integer> ft = new HashMap<>();

        for (Map.Entry<Integer, DummyRoute> entry : myRoutingTable.entrySet()) {
            ft.put(entry.getKey(), entry.getValue().nextHop);
        }

        return ft;
    }
}
