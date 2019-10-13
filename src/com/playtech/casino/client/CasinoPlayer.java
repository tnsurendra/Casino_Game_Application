package com.playtech.casino.client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Logger;

import com.playtech.casino.common.MessageInfo;
import com.playtech.casino.common.Player;
import com.playtech.casino.util.Constants;
import com.playtech.casino.util.Utility;

public class CasinoPlayer {
	private final static Logger logger = Logger.getLogger(CasinoPlayer.class.getName());
	private DatagramSocket sendSoc, recvSocket;
	private int noOfBU;

	public static void main(String[] args) throws Exception {

		final int senderPort = Constants.SERVER_LISTENING_PORT;
		final InetAddress serverAddr = InetAddress.getByName(Constants.LOCALHOST);
		CasinoPlayer client = new CasinoPlayer();
		client.execute(serverAddr, senderPort);
		client.handleClose();
	}


	public CasinoPlayer() throws Exception {
		sendSoc = new DatagramSocket();
	}

	private void execute(InetAddress serverAddr, int senderPort) throws Exception {
		MessageInfo message = new MessageInfo();
		/* 49152 is reserved for Server management
		 49153 is reserved for server
		 49154 is reserved for servermgt reciever */		

		//Generate Random port for client
		int clientRecvPort = Utility.nextFreePort(49155, 65535);
		System.out.println( clientRecvPort + "clientRecvPort");
		message.setClientPort(clientRecvPort);
		
		// Async call for send Random BU
		sendAsyncBalanceUpdates(serverAddr, senderPort, message);

		// Recieving messages from server in another port
		recvSocket = new DatagramSocket(clientRecvPort);
		listenForMessagesFromServer(clientRecvPort);

	}


	private void listenForMessagesFromServer(int clientRecvPort) throws Exception {
		while (true) {
			MessageInfo serverMessage = Utility.recieveMessageFromServer(recvSocket);
			
			if (Constants.CLOSE_CONN.equals(serverMessage.getCommand())) {
				if (handleClose()) {
					recvSocket.close();
					System.out.println( "Closing port :" + clientRecvPort);
					System.exit(0);
				}
			} else if (serverMessage.getErrorCode() != 404) {
				// TODO:log message
				System.out.println( "cLIENT RECIEVED MESSAGE FROM SERVER:" + serverMessage);
			} else {
				System.out.println( "Invalid Condition");
			}
			
		}
	}


	private void sendAsyncBalanceUpdates(InetAddress serverAddr, int senderPort, MessageInfo message) {
		new Thread(new Runnable() {
			public void run() {
				try {
					sendRandomBalanceUpdate(serverAddr, senderPort, message);

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	private void sendRandomBalanceUpdate(InetAddress serverAddr, int recvPort, MessageInfo message) throws Exception {
		noOfBU++;
		Player player = new Player();
		Scanner sc = new Scanner(System.in);
		System.out.print("Enter Username :");
		String userName = sc.nextLine();
		if (null == userName || userName.length() < 2) {
			return;
		}
		// TODO:username has to select
		player.setUserName(userName);
		// player.setUserName("test"+Math.random());
		while (true) {
			message.setTransactionId(new Random().nextInt((100 - 1) + 1) + 100);
			player.setBalance(Math.random());
			player.setBalanceVersion((float) Math.random());
			message.setPlayer(player);
			DatagramPacket packet = Utility.writeMessageObject(serverAddr, recvPort, message);
			sendSoc.send(packet);
			Thread.sleep(new Long(Utility.getValue("interval.bw.balanceupdate")));
		}
	}
	private boolean handleClose() throws Exception {
		boolean result = false;
		System.out.println( "NO OF BU" + noOfBU);
		if (noOfBU >= Integer.parseInt(Utility.getValue("no.of.rounds"))) {
			result = true;
		}
		return result;
	}

}
