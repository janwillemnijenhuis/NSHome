package my_protocol;

import framework.IRDTProtocol;
import framework.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

/**
 * @version 10-07-2019
 *
 * Copyright University of Twente,  2013-2019
 *
 **************************************************************************
 *                            Copyright notice                            *
 *                                                                        *
 *            This file may  ONLY  be distributed UNMODIFIED.             *
 * In particular, a correct solution to the challenge must NOT be posted  *
 * in public places, to preserve the learning effect for future students. *
 **************************************************************************
 *
 * @author bart.fischer s0016667
 * @author janwillem.nijenhuis s2935511
 */
public class MySlidingWindowProtocol extends IRDTProtocol {

    // change the following as you wish:
    static final int HEADERSIZE=1;   // number of header bytes in each packet
    static final int DATASIZE=128;   // max. number of user data bytes in each packet
    static final int MAXSEQ = 64;
    static final int SWS = 4;
    private int LFS = -1;
    private int LAR = -1;
    private boolean stop = false;
    private ArrayList<Integer[]> packets = new ArrayList<>();
    private ArrayList<Integer> acksRecvd = new ArrayList<>();
    private ArrayList<Integer> acksNotRecvd = new ArrayList<>();

    @Override
    public void sender() {
        System.out.println("Sending...");

        // read from the input file
        Integer[] fileContents = Utils.getFileContents(getFileID());

        int filePointer = 0;
        int datalen = 0;
        int index = 0;
        boolean fileEnd = false;

        while (!fileEnd) {
            if ((fileContents.length - filePointer) < DATASIZE) {
                datalen = fileContents.length - filePointer;
                fileEnd = true;
                index++;
            } else {
                datalen = DATASIZE;
                index++;
            }
            index %= MAXSEQ;
            Integer[] pkt = new Integer[HEADERSIZE + datalen];
            pkt[0] = index;
            System.arraycopy(fileContents, filePointer, pkt, HEADERSIZE, datalen);
            packets.add(pkt);
        }
        System.out.print("Starting transmission");
        while (LFS < packets.size()) {
            ArrayList<Integer> notRecvd = acksNotRecvd;
            if (!notRecvd.isEmpty()) {
                for (int i: notRecvd) {
                    getNetworkLayer().sendPacket(packets.get(i));
                    framework.Utils.Timeout.SetTimeout(10, this, i);
                    System.out.print("Sending packet " + i);
                }
            } else {
                for (int i = 0; i < SWS; i++) {
                    int j = LAR + i + 1;
                    getNetworkLayer().sendPacket(packets.get(j));
                    framework.Utils.Timeout.SetTimeout(10, this, j);
                    System.out.print("Sending packet " + j);
                }
            }

            while (!stop) {
                try {
                    Thread.sleep(10);
                    // check for incoming ack
                    Integer[] ack = getNetworkLayer().receivePacket();
                    if (ack != null) {
                        acksRecvd.add(ack[0]);
                        Collections.sort(acksRecvd);
                        for (int i = 0; i < acksRecvd.size(); i++) {
                            int j = acksRecvd.get(i);
                            if (j == LAR + 1) {
                                LAR = j;
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    System.out.println("Interruption");
                    stop = true;
                }
            }
        }
    }

    @Override
    public void TimeoutElapsed(Object tag) {
        int z=(Integer)tag;
        stop = true;
        acksNotRecvd.add(z);
        // handle expiration of the timeout:
        System.out.println("Timer expired with tag="+z);
    }

    @Override
    public Integer[] receiver() {
        System.out.println("Receiving...");

        // create the array that will contain the file contents
        // note: we don't know yet how large the file will be, so the easiest (but not most efficient)
        //   is to reallocate the array every time we find out there's more data
        HashSet<Integer> recvHeaders = new HashSet<>();
        Integer[] fileContents = new Integer[0];
        int roundCount = 0;

        // loop until we are done receiving the file
        boolean stop = false;
        int index = 0;
        while (!stop) {

            // try to receive a packet from the network layer
            Integer[] packet = getNetworkLayer().receivePacket();

            // if we indeed received a packet
            if (packet != null) {

                // tell the user
                System.out.println("Received packet, length="+packet.length+"  round-packet=" + roundCount + "-" + packet[0] );

                // append the packet's data part (excluding the header) to the fileContents array, first making it larger
                index = packet[0];
                if (index == 255) {
                    // if the index is the stopping index, terminate
                    System.out.println("Entire file received!");
                    Integer[] pkt = new Integer[1];
                    pkt[0] = index;
                    System.out.println("Sending ACK " + pkt[0]);
                    getNetworkLayer().sendPacket(pkt);
                    stop = true;
                    continue;
                } if (index == 254 && recvHeaders.isEmpty()) {
                    // this is a duplicate message, resend the ACK
                    System.out.println("Duplicate");
                    Integer[] pkt = new Integer[1];
                    pkt[0] = index;
                    System.out.println("Sending ACK " + pkt[0]);
                    getNetworkLayer().sendPacket(pkt);
                    continue;
                }
                if (!recvHeaders.contains(index)) {
                    // if the index is not contained in the received indices, add this part to the file
                    int oldlength=fileContents.length;
                    int datalen= packet.length - HEADERSIZE;
                    fileContents = Arrays.copyOf(fileContents, oldlength+datalen);
                    System.arraycopy(packet, HEADERSIZE, fileContents, oldlength, datalen);
                    recvHeaders.add(index);
                }
                // send the ACK to the sender
                Integer[] pkt = new Integer[1];
                pkt[0] = index;
                System.out.println("Sending ACK " + pkt[0]);
                getNetworkLayer().sendPacket(pkt);

                if (recvHeaders.size() == 255) {
                    // empty the hashset with received indices for this round
                    roundCount++;
                    System.out.println("Emptied hash set");
                    recvHeaders.clear();
                }
            }else{
                // wait ~10ms (or however long the OS makes us wait) before trying again
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    stop = true;
                }
            }
        }
        // return the output file
        return fileContents;
    }
}
