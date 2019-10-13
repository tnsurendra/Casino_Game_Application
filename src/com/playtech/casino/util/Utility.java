package com.playtech.casino.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import com.playtech.casino.common.MessageInfo;
import com.playtech.casino.common.ServerManageInfo;



public class Utility {
	public static String getValue(String key) throws Exception {
		InputStream input = Utility.class.getClassLoader().getResourceAsStream("config.properties");

		Properties prop = new Properties();

		// load a properties file
		prop.load(input);

		// get the property value and print it out
		return prop.getProperty(key);
	}


	public static void sendMessageToHost(ServerManageInfo message) throws Exception {

		DatagramPacket packet=Utility.writeMessageObject(InetAddress.getByName("localhost"), message.getClientPort(), message);
		DatagramSocket socket=new DatagramSocket();
		socket.send(packet);

	}

	public static void sendMessageToHost(MessageInfo message) throws Exception {

		DatagramPacket packet=Utility.writeMessageObject(InetAddress.getByName("localhost"), message.getClientPort(), message);
		DatagramSocket socket=new DatagramSocket();
		socket.send(packet);

	}

	public static DatagramPacket writeMessageObject(InetAddress serverAddr, int recvPort, Object message) throws Exception {
		ByteArrayOutputStream byteStream= new ByteArrayOutputStream(5000);
		ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(byteStream));		

		//try {

		os.flush();
		os.writeObject(message);
		os.flush();
		/*} catch (IOException e) {
			System.out.println("Error serializing object for transmition.");
			//System.exit(-1);
		}*/

		//retrieves byte array
		byte[] data=byteStream.toByteArray();
		//send(byteStream.toByteArray(), serverAddr, recvPort);
		return new DatagramPacket(data, data.length, serverAddr, recvPort);

	}

	public static Object readMessageObject(DatagramPacket packet) throws Exception {

		// Use the data to construct a new MessageInfo object
		ObjectInputStream is=convertPacketToStream(packet);
		Object msg =  is.readObject();
		is.close();
		return msg;
	}

	private static ObjectInputStream convertPacketToStream(DatagramPacket packet) throws IOException, ClassNotFoundException {
		ByteArrayInputStream byteStream = new ByteArrayInputStream(packet.getData());
		ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(byteStream));


		return is;
	}
	/*
	 * public static ServerManageInfo readServerManageInfo(DatagramPacket packet)
	 * throws Exception { ObjectInputStream is=convertPacketToStream(packet);
	 * ServerManageInfo msg = (ServerManageInfo) is.readObject(); is.close(); return
	 * msg; }
	 */







	public static MessageInfo recieveMessageFromServer(DatagramSocket socket) throws Exception {
		System.out.println("Client is acepting messages on port "+socket.getLocalPort());
		//Receive request from client
		int	pacSize = 5000;
		byte [] pacData = new byte[5000];
		DatagramPacket packet = new DatagramPacket(pacData, pacSize);
		socket.receive(packet);				
		ByteArrayInputStream byteStream = new ByteArrayInputStream(packet.getData());
		ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(byteStream));
		MessageInfo msg = (MessageInfo) is.readObject();
		return msg;

	}
	public static void main(String[] args) {
		System.out.println(Utility.nextFreePort(49155,65535));
	}
	public static int nextFreePort(int from, int to) {
		int port = new Random().nextInt(( to-from) + 1) + from;//randPort(from, to);
		while (true) {
			if (isLocalPortFree(port)) {
				return port;
			} else {
				port = ThreadLocalRandom.current().nextInt(from, to);
			}
		}
	}

	private static boolean isLocalPortFree(int port) {
		try {
			new ServerSocket(port).close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	//public boolean sendMessageToClient(String host,int port,String message) {}

}
