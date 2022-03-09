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


public class LongestPrefixMatcher {

    int bestPort;

    public Node root;

    class Node {
        int port;
        Node left, right;

        public Node(int p) {
            port = p;
            left = right = null;
        }
    }

    /**
     * You can use this function to initialize variables.
     */
    public LongestPrefixMatcher() {
        this.root = new Node(-1);
        this.bestPort = -1;
    }

    /**
     * Looks up an IP address in the routing tables
     * @param ip The IP address to be looked up in integer representation
     * @return The port number this IP maps to
     */
    public int lookup(int ip) {
        // TODO: Look up this route

        boolean[] bits = new boolean[32];

        for (int i = 31; i >= 0; i--) {
            bits[i] = (ip & (1 << i)) != 0;
        }

        Node node = root;
        for (int i = 31; i >= 0; i--) {
            if (!bits[i]) {
                if (node.left == null) {
                    if (node.port != -1) {
                        this.bestPort = -1;
                        return node.port;
                    } else {
                        int port = this.bestPort;
                        this.bestPort = -1;
                        return port;
                    }
                }
                node = node.left;
            } else {
                if (node.right == null) {
                    if (node.port != -1) {
                        this.bestPort = -1;
                        return node.port;
                    } else {
                        int port = this.bestPort;
                        this.bestPort = -1;
                        return port;
                    }
                }
                node = node.right;
            }
            if (node.port != -1) {
                this.bestPort = node.port;
            }
        }

        int port = this.bestPort;
        this.bestPort = -1;
        return port;
    }

    public String combine_elements(int ip) {
        String ip_string = ipToHuman(ip);
        String[] ip_vals = ip_string.split("\\.");
        int[] elements = new int[4];
        for (int i = 0; i < 4; i++) {
            elements[i] = Integer.parseInt(ip_vals[i]);
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            result.append(
                String.format("%8s", Integer.toBinaryString(elements[i])).replace(' ', '0'));
        }
        return result.toString();
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

        boolean[] bits = new boolean[32];
        for (int i = 32-1; i >= 0; i--) {
            bits[i] = (ip & (1 << i)) != 0;
        }

        Node node = root;
        for (int i = 32-1; i >= 32-prefixLength; i--) {
            if (!bits[i]) {
                if (node.left == null) {
                    node.left = new Node(-1);
                }
                node = node.left;
            } else {
                if (node.right == null) {
                    node.right = new Node(-1);
                }
                node = node.right;
            }
        }
        node.port = portNumber;


    }

    /**
     * This method is called after all routes have been added.
     * You don't have to use this method but can use it to sort or otherwise
     * organize the routing information, if your datastructure requires this.
     */
    public void finalizeRoutes() {
        // TODO: Optionally do something
//        Collections.sort(this.ip, new Comparator<int[]>() {
//            @Override
//            public int compare(int[] o1, int[] o2) {
//                if(o1[1] < o2[1]){
//                    return 1;
//                } else if (o1[1]>o2[1]) {
//                    return -1;
//                } else {
//                    return 0;
//                }
//            }
//        });
//        System.out.println(this.ip.get(0)[1]);
//        for (int i = 0; i < this.ip.size(); i++) {
//            int[] thisIP = this.ip.get(i);
//            this.ips.add(thisIP[0]);
//            this.prefix.add(thisIP[1]);
//            this.port.add(thisIP[2]);
//            this.ids.add(thisIP[3]);
//        }
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