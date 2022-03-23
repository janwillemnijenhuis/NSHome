package ns.tcphack;

import java.net.InetAddress;
import java.net.UnknownHostException;

class MyTcpHandler extends TcpHandler {
	public static void main(String[] args) {
		new MyTcpHandler();
	}

	public MyTcpHandler() {
		super();

		boolean done = false;

		// array of bytes in which we're going to build our packet:
		int[] txpkt = new int[60];		// 40 bytes long for now, may need to expand this later

		txpkt[0] = 0x60;	// first byte of the IPv6 header contains version number in upper nibble
		// fill in the rest of the packet yourself...:
		txpkt[1] = 0;
		txpkt[2] = 0;
		txpkt[3] = 0;

		// Payload length
		txpkt[4] = 0;
		txpkt[5] = 20;
		// Next header = TCP
		txpkt[6] = 253;
		// Hop limit
		txpkt[7] = 64;
		// setup src
		txpkt[8] = 0x20;
		txpkt[9] = 0x01;
		txpkt[10] = 0x6;
		txpkt[11] = 0x7c;
		txpkt[12] = 0x25;
		txpkt[13] = 0x64;
		txpkt[14] = 0xa3;
		txpkt[15] = 0x03;
		txpkt[16] = 0x64;
		txpkt[17] = 0x57;
		txpkt[18] = 0x30;
		txpkt[19] = 0x6b;
		txpkt[20] = 0xfe;
		txpkt[21] = 0x93;
		txpkt[22] = 0xe2;
		txpkt[23] = 0x21;
		// setup dest
		txpkt[24] = 0x20;
		txpkt[25] = 0x01;
		txpkt[26] = 0x6;
		txpkt[27] = 0x10;
		txpkt[28] = 0x19;
		txpkt[29] = 0x08;
		txpkt[30] = 0xff;
		txpkt[31] = 0x02;
		txpkt[32] = 0xf5;
		txpkt[33] = 0x7a;
		txpkt[34] = 0xc5;
		txpkt[35] = 0x34;
		txpkt[36] = 0x9c;
		txpkt[37] = 0x8c;
		txpkt[38] = 0xfb;
		txpkt[39] = 0xd4;

		/// TCP header
		// 0000010011010010 scr port
		txpkt[40] = 0b00000100;
		txpkt[41] = 0b11010010;
		// 0001111000011110  dest port
		txpkt[42] = 0b00011110;
		txpkt[43] = 0b00011110;
		// sequence number
		txpkt[44] = txpkt[45] = txpkt[46] = 0;
		txpkt[47] = 0;
		// ack number
		txpkt[48] = txpkt[49] = txpkt[50] = 0;
		txpkt[51] = 0;
		// header length only first 4 bits count
		txpkt[52] = 0b01010000;
		// reserved + flags
		txpkt[53] = 0b00000010;
		// window size 0111000010000000
		txpkt[54] = 0b01110000;
		txpkt[55] = 0b10000000;


		this.sendData(txpkt);	// send the packet

		while (!done) {
			// check for reception of a packet, but wait at most 500 ms:
			int[] rxpkt = this.receiveData(500);
			if (rxpkt.length==0) {
				// nothing has been received yet
				System.out.println("Nothing...");
				continue;
			}

			// something has been received
			int len=rxpkt.length;

			// print the received bytes:
			int i;
			System.out.print("Received "+len+" bytes: ");
			System.out.println(Integer.toBinaryString(rxpkt[53]));
			int s1 = rxpkt[44];
			int s2 = rxpkt[45];
			int s3 = rxpkt[46];
			int s4 = rxpkt[47] + 1;
			txpkt[47] += 1;
			txpkt[53] = 0b00010000;
			txpkt[48] = s1;
			txpkt[49] = s2;
			txpkt[50] = s3;
			txpkt[51] = s4;
			this.sendData(txpkt);
		}   
	}
}
