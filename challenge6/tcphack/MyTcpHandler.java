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
		int[] txpkt = new int[40];		// 40 bytes long for now, may need to expand this later

		txpkt[0] = 0x60;	// first byte of the IPv6 header contains version number in upper nibble
		// fill in the rest of the packet yourself...:
		txpkt[1] = 0;
		txpkt[2] = 0;
		txpkt[3] = 0;

		// Payload length
		txpkt[4] = 0;
		txpkt[5] = 0;
		// Next header = TCP
		txpkt[6] = 6;
		// Hop limit
		txpkt[7] = 64;

		// setup src and destination address
//		byte[] src = new byte[16];
//		byte[] dest = new byte[16];
//		InetAddress a = null;
//		InetAddress b = null;
//		try {
//			a = InetAddress.getByName("2001:67c:2564:a303:6457:306b:fe93:e221");
//			b = InetAddress.getByName("2001:610:1908:ff02:f57a:c534:9c8c:fbd4");
//		} catch (UnknownHostException e) {
//			e.printStackTrace();
//		}
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
//		b = InetAddress.getByName("2001:610:1908:ff02:f57a:c534:9c8c:fbd4");
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

//		assert a != null;
//		src = a.getAddress();
//		assert b != null;
//		dest = b.getAddress();
//		System.out.println(src);
//		System.out.println(dest);
//		// Source address
//		for (int i = 8; i < 24; i++) {
//			txpkt[i] = src[i - 8];
//			txpkt[i + 16] = dest [i - 8];
//		}

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
			for (i=0;i<len;i++) System.out.print(rxpkt[i]+" ");
			System.out.println("");
		}   
	}
}
