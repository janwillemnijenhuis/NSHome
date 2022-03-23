package ns.tcphack;

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
		txpkt[8] = 0x2001;
		txpkt[9] = 0x67c;
		txpkt[10] = 0x2565;
		txpkt[11] = 0xa303;
		txpkt[12] = 0x598b;
		txpkt[13] = 0xa4ef;
		txpkt[14] = 0x889b;
		txpkt[15] = 0x361f;

		txpkt[16] = 0x2001;
		txpkt[17] = 0x610;
		txpkt[18] = 0x1908;
		txpkt[19] = 0xff02;
		txpkt[20] = 0xf57a;
		txpkt[21] = 0xc534;
		txpkt[22] = 0x9c8c;
		txpkt[23] = 0xfbd4;


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
