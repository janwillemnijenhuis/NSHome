package my_protocol;

import framework.IRDTProtocol;
import framework.Utils;

import java.util.Arrays;

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
 */
public class MyProtocol extends IRDTProtocol {

    // change the following as you wish:
    static final int HEADERSIZE=1;   // number of header bytes in each packet
    static final int DATASIZE=128;   // max. number of user data bytes in each packet

    @Override
    public void sender() {
        System.out.println("Sending...");

        // read from the input file
        Integer[] fileContents = Utils.getFileContents(getFileID());
        int remainingLen = fileContents.length;
        boolean fileEnd = false;
        int filePointer = 0;
        int index = 0;
        int datalen = 0;
        while (!fileEnd) {
            // create a new packet of appropriate size
            if ((remainingLen - filePointer) < DATASIZE) {
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
            System.out.println("Sent one packet with header=" + pkt[0]);

            // schedule a timer for 1000 ms into the future, just to show how that works:
            index++;
            remainingLen -= datalen;
            filePointer += datalen;
            if (remainingLen == 0) {
                fileEnd = true;
            }
            framework.Utils.Timeout.SetTimeout(1000, this, 28);
        }

        // and loop and sleep; you may use this loop to check for incoming acks...
        boolean stop = false;
        while (!stop) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                stop = true;
            }
        }

    }

    @Override
    public void TimeoutElapsed(Object tag) {
        int z=(Integer)tag;
        // handle expiration of the timeout:
        System.out.println("Timer expired with tag="+z);
    }

    @Override
    public Integer[] receiver() {
        System.out.println("Receiving...");

        // create the array that will contain the file contents
        // note: we don't know yet how large the file will be, so the easiest (but not most efficient)
        //   is to reallocate the array every time we find out there's more data
        Integer[] fileContents = new Integer[0];

        // loop until we are done receiving the file
        boolean stop = false;
        int index = 0;
        while (!stop) {

            // try to receive a packet from the network layer
            Integer[] packet = getNetworkLayer().receivePacket();

            // if we indeed received a packet
            if (packet != null) {

                // tell the user
                System.out.println("Received packet, length="+packet.length+"  first byte="+packet[0] );

                // append the packet's data part (excluding the header) to the fileContents array, first making it larger
                int oldlength=fileContents.length;
                int datalen= packet.length - HEADERSIZE;
                fileContents = Arrays.copyOf(fileContents, oldlength+datalen);
                System.arraycopy(packet, HEADERSIZE, fileContents, oldlength, datalen);
                index++;


            }else{
                // wait ~10ms (or however long the OS makes us wait) before trying again
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    stop = true;
                }
            }
            if (index == 2) {
                stop = true;
            }
        }

        // return the output file
        return fileContents;
    }
}
