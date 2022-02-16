package my_protocol;

import framework.IRDTProtocol;
import framework.Utils;

import java.util.Arrays;
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
public class MyAlternatingProtocol extends IRDTProtocol {

    // change the following as you wish:
    static final int HEADERSIZE=1;   // number of header bytes in each packet
    static final int DATASIZE=512;   // max. number of user data bytes in each packet
    private boolean stop = false;

    @Override
    public void sender() {
        System.out.println("Sending...");

        // read from the input file
        Integer[] fileContents = Utils.getFileContents(getFileID());
        int remainingLen = fileContents.length;
        boolean fileEnd = false;
        boolean fileSent = false;
        int roundCount = 0;
        int filePointer = 0;
        int index = 0;
        int datalen;
        while (!fileEnd) {
            stop = false;
            // create a new packet of appropriate size
            if ((fileContents.length - filePointer) < DATASIZE) {
                datalen = remainingLen;
            } else {
                datalen = DATASIZE;
            }
            System.out.println("Sending packet of length: " + datalen);
            Integer[] pkt = new Integer[HEADERSIZE + datalen];
            // write something random into the header byte
            pkt[0] = index;
            // copy databytes from the input file into data part of the packet, i.e., after the header
            System.arraycopy(fileContents, filePointer, pkt, HEADERSIZE, datalen);

            // send the packet to the network layer
            getNetworkLayer().sendPacket(pkt);
            System.out.println("Sent one packet with round-packet=" + roundCount + "-" + pkt[0]);

            // schedule a timer for 1000 ms into the future, just to show how that works:
            framework.Utils.Timeout.SetTimeout(1000, this, 28);

            // and loop and sleep; you may use this loop to check for incoming acks...
            while (!stop) {
                try {
                    Thread.sleep(100);
                    // check for incoming ack
                    Integer[] ack = getNetworkLayer().receivePacket();
                    if (ack != null && ack[0] == index) {
                        System.out.println("Acknowledgement " + ack[0] + " received");
                        // if the file is not entirely sent, send the next packet
                        if (!fileSent) {
                            // update index, filepointer and remaininglength
                            index++;
                            remainingLen -= datalen;
                            filePointer += datalen;
                            // if the index equals 255, a round of files has been done
                            if (index == 255) {
                                roundCount++;
                                index = 0;
                            }
                            stop = true;
                            // if there is nothing left to sent, send a stop message
                            if (remainingLen == 0) {
                                System.out.println("Entire file sent");
                                Integer[] stoppkt = new Integer[1];
                                stoppkt[0] = -1;
                                getNetworkLayer().sendPacket(stoppkt);
                                fileSent = true;
                                index = 255;
                                stop = false;
                            }
                        } else {
                            // if the file is sent, terminate the sending part
                            stop = true;
                            fileEnd = true;
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
