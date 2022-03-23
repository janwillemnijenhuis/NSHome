package ns.tcphack;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

class TcpHackClient {
	public static final String CON_IP = "127.0.0.1";
	public static final int CON_PORT = 1234;

	private BlockingQueue<Byte[]> packetQueue = new LinkedBlockingQueue<Byte[]>();

	private DataInputStream in;
	private DataOutputStream out;

	public TcpHackClient() {
		try {
			new Thread(new Communicator()).start();
			Thread.sleep(100); // Give the communicator a chance
		} catch (InterruptedException e) { }
	}

	public void send(int[] data) {
		if (out != null) {
			try {
				out.writeInt(data.length);
				byte[] box = new byte[data.length];
				for (int i = 0; i < box.length; i++) box[i] = (byte)data[i];
				out.write(box);
				out.flush();
			} catch (IOException e) {
				System.err.println("Couldn't write socket: " + e.getMessage());
			}
		} else {
			System.err.println("Didn't write socket: not connected");
		}
	}

	public int[] dequeuePacket(long timeout) {
		Byte[] box;
		
		try {
			box = packetQueue.poll(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) { return new int[0]; }
		
		if (box == null) {
			return new int[0];
		}
		
		int[] unbox = new int[box.length];
		for (int i = 0; i < box.length; i++) {
			unbox[i] = box[i]&0xff;
		}
		return unbox;
	}

	public boolean hasPackets() {
		return !packetQueue.isEmpty();
	}

	class Communicator implements Runnable {
		public void run() {
			Socket clientSocket = null;
			try {
				clientSocket = new Socket(CON_IP, CON_PORT);
				out = new DataOutputStream(clientSocket.getOutputStream());
				in = new DataInputStream(clientSocket.getInputStream());

				while (true) {
					int size = in.readInt();
					byte[] data = new byte[size];
					in.read(data, 0, size);
					
					Byte[] box = new Byte[size];
					
					for (int i = 0; i < size; i++) {
						box[i] = data[i];
					}
					
					packetQueue.offer(box);
				}
			} catch (IOException e) {
				System.err.println("Couldn't read socket: " + e.getMessage());
			} finally {
				try {
					if (clientSocket != null)
						clientSocket.close();
				} catch (IOException e) { }
			}

			System.err.println("Communicator stopped!");
		}
	}
}
