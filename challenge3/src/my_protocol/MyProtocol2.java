package my_protocol;

import framework.IMACProtocol;
import framework.MediumState;
import framework.TransmissionInfo;
import framework.TransmissionType;
import java.util.Random;

/**
 * A fairly trivial Medium Access Control scheme.
 *
 * @author Jaco ter Braak, University of Twente
 * @version 05-12-2013
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
public class MyProtocol2 implements IMACProtocol {
    public boolean RTS = false; // currently in requesting mode
    public boolean sending = false; // currently in sending mode, obtained CTS
    public int frameCount = 0;
    public int wait = 0; // the current amount of waits
    public int p = 50; // probability of transmitting a RTS
    public int m = 4; // max no of slots to wait
    public boolean takingOver = false;
    public double aggressiveness = 1.025;



    @Override
    public TransmissionInfo TimeslotAvailable(MediumState previousMediumState,
                                              int controlInformation, int localQueueLength) {

        // display the current number of frames in the queue
        System.out.println("CURRENT QUEUE LENGTH: " + localQueueLength);
        System.out.println("CONTROL INFORMATION: " + controlInformation);

        // if there is no data, don't send anything
        if (localQueueLength == 0) {
            System.out.println("EMPTY QUEUE - No data to send.");
            this.RTS = false;
            this.sending = false;
            return sendNothing();
        } else if (this.sending) {
            if (previousMediumState != MediumState.Succes) {
                // collision detected in previous round, wait some random slots before sending again
                this.wait = randomWaits();
                this.RTS = false;
                this.sending = false;
                System.out.println("SETTING WAIT TO: " + this.wait);
            }
        }

        if (this.wait == 0) {
            if (this.RTS && parsingClearToSend(previousMediumState) == 2) {
                // the channel was free, and we did send DATA, so send the next data
                return sendData();
            } else if (this.RTS && parsingClearToSend(previousMediumState) == 0) {
                // a collision was detected, and we did send a RTS, so wait some random slots
                if (!this.takingOver) {
                    this.wait = randomWaits();
                    this.RTS = false;
                    return sendNothing();
                } else {
                    return sendData();
                }
            } else if (parsingClearToSend(previousMediumState) == 1) {
                // the channel is idle, transmit RTS with probability p
                if (new Random().nextInt(100) < this.p) {
                    return sendRTS();
                } else {
                    return sendNothing();
                }
            } else if (parsingClearToSend(previousMediumState) == 2 && controlInformation >=  this.aggressiveness * this.frameCount) {
                // if the framecount of some sender is twice ours, and the previous transmission was success, transmit RTS
                this.takingOver = true;
                return sendRTS();
            }

            else {
                // the channel is occupied or a collision was detected, don't send anything
                return sendNothing();
            }
        } else {
            // we're currently waiting until our waits have passed
            this.wait--;
            return sendNothing();
        }
    }

    public int parsingClearToSend(MediumState previousMediumState) {
        return switch (previousMediumState) {
            case Succes -> 2;
            case Collision -> 0;
            case Idle -> 1;
        };
    }

    public int randomWaits() {
        return (int) (Math.random() * this.m);
    }

    public TransmissionInfo sendRTS() {
        this.RTS = true;
        System.out.println("SENDING RTS - requesting to send data");
        return new TransmissionInfo(TransmissionType.Data, this.frameCount);
    }

    public TransmissionInfo sendNothing() {
        System.out.println("SENDING NOTHING");
        return new TransmissionInfo(TransmissionType.Silent, this.frameCount);
    }

    public TransmissionInfo sendData() {
        System.out.println("CLEAR TO SEND - SENDING DATA");
        this.frameCount += 1;
        this.sending = true;
        return new TransmissionInfo(TransmissionType.Data, this.frameCount);
    }

}
