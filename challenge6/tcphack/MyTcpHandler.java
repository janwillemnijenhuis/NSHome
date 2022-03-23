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
		txpkt[1] = .........;
		txpkt[2] = .........;
		......

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
