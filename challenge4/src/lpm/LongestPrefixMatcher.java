/**
 * LongestPrefixMatcher.java
 *
 *   Version: 2019-07-10
 * Copyright: University of Twente,  2015-2019
 *
 **************************************************************************
 *                            Copyright notice                            *
 *                                                                        *
 *            This file may  ONLY  be distributed UNMODIFIED.             *
 * In particular, a correct solution to the challenge must NOT be posted  *
 * in public places, to preserve the learning effect for future students. *
 **************************************************************************
 */

package lpm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class LongestPrefixMatcher {

    public ArrayList<int[]> ip;
    public ArrayList<Integer> ips;
    public ArrayList<Integer> prefix;
    public ArrayList<Integer> port;
    public ArrayList<Integer> ids;
    public ArrayList<ArrayList<ArrayList<Integer>>> ms;

    public int bestPort;
    public int bestLength;

    /**
     * You can use this function to initialize variables.
     */
    public LongestPrefixMatcher() {
        this.ip = new ArrayList<>() ;
        this.prefix = new ArrayList<>();
        this.port = new ArrayList<>();
        this.ips = new ArrayList<>();
        this.ids = new ArrayList<>();
        this.bestPort = -1;
        this.bestLength = 0;

        this.ms = new ArrayList<>();
    }

    /**
     * Looks up an IP address in the routing tables
     * @param ip The IP address to be looked up in integer representation
     * @return The port number this IP maps to
     */
    public int lookup(int ip) {
        // TODO: Look up this route


        int humanIP = Integer.parseInt(ipToHuman(ip).split("\\.")[0]);

        for (int i = 0; i < this.ips.size(); i++) {
            if (this.ids.get(i) == humanIP) {
                int prefix = this.prefix.get(i);
                int pf = this.ips.get(i) >> (32 - (this.prefix.get(i)));
                int compare = ip >> (32 - (this.prefix.get(i)));
                int result = pf ^ compare;
                if (result == 0) {
                    return this.port.get(i);
                }
            }
        }
        return -1;
    }

    /**
     * Adds a route to the routing tables
     * @param ip The IP the block starts at in integer representation
     * @param prefixLength The number of bits indicating the network part
     *                     of the address range (notation ip/prefixLength)
     * @param portNumber The port number the IP block should route to
     */
    public void addRoute(int ip, byte prefixLength, int portNumber) {
        // TODO: Store this route for later use in lookup() method
        int[] item = new int[4];
        item[0] = ip;
        item[1] = prefixLength;
        item[2] = portNumber;
        item[3] = Integer.parseInt(ipToHuman(ip).split("\\.")[0]);
        this.ip.add(item);

    }

    /**
     * This method is called after all routes have been added.
     * You don't have to use this method but can use it to sort or otherwise
     * organize the routing information, if your datastructure requires this.
     */
    public void finalizeRoutes() {
        // TODO: Optionally do something
        Collections.sort(this.ip, new Comparator<int[]>() {
            @Override
            public int compare(int[] o1, int[] o2) {
                if(o1[1] < o2[1]){
                    return 1;
                } else if (o1[1]>o2[1]) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        System.out.println(this.ip.get(0)[1]);
        for (int i = 0; i < this.ip.size(); i++) {
            int[] thisIP = this.ip.get(i);
            this.ips.add(thisIP[0]);
            this.prefix.add(thisIP[1]);
            this.port.add(thisIP[2]);
            this.ids.add(thisIP[3]);
        }
    }

    /**
     * Converts an integer representation IP to the human readable form
     * @param ip The IP address to convert
     * @return The String representation for the IP (as xxx.xxx.xxx.xxx)
     */
    private String ipToHuman(int ip) {
        return Integer.toString(ip >> 24 & 0xff) + "." +
            Integer.toString(ip >> 16 & 0xff) + "." +
            Integer.toString(ip >> 8 & 0xff) + "." +
            Integer.toString(ip & 0xff);
    }

    /**
     * Parses an IP
     * @param ipString The IP address to convert
     * @return The integer representation for the IP
     */
    private int parseIP(String ipString) {
        String[] ipParts = ipString.split("\\.");

        int ip = 0;
        for (int i = 0; i < 4; i++) {
            ip |= Integer.parseInt(ipParts[i]) << (24 - (8 * i));
        }

        return ip;
    }
}