package com.playtech.casino.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import com.playtech.casino.common.Player;
import com.playtech.casino.common.QueryStatistics;
import com.playtech.casino.common.ServerManageInfo;
import com.playtech.casino.util.Constants;
import com.playtech.casino.util.SERVERMNGCMDS;
import com.playtech.casino.util.Utility;

public class CasinoServerManager {

	public static void main(String[] args) throws Exception {

		//DatagramSocket ds = new DatagramSocket();

		//InetAddress ip = InetAddress.getLocalHost();
		
		Scanner sc = new Scanner(System.in);
		// Async call for recieving server messages
		recieveMessagesFromServer();

		// loop while user not enters "bye"
		sendMngCmdToServer(InetAddress.getByName(Constants.LOCALHOST), sc);
		

	}

	private static void sendMngCmdToServer(InetAddress ip, Scanner sc) throws Exception, SocketException, IOException {
		while (true) {
			System.out.print("Enter Command:");
			String inp = sc.nextLine().toUpperCase();
			System.out.println("recieved input" + inp);
			SERVERMNGCMDS cmd = Arrays.stream(SERVERMNGCMDS.values()).filter(e -> e.name().equalsIgnoreCase(inp))
					.findAny().orElse(null);
			if (null == cmd) {
				System.out.println("Invalid server management command :" + inp);
				continue;
			}

			ServerManageInfo serverManageInfo = new ServerManageInfo();
			serverManageInfo.setClientPort(Constants.SERVER_MGR_LISTENING_PORT);
			Map<String, String> map = new HashMap<String, String>();
			switch (cmd) {
			case SHUTDOWN:

				map.put("SHUTDOWN", null);

				break;
			case SHOWCLIENTS:
				map.put("SHOWCLIENTS", null);

				break;
			case KICKPLAYER:
				System.out.print("Enter USername to kickoff :");
				String userName = sc.nextLine();
				if (null != userName) {
					map.put("KICKPLAYER", userName);
				}
				break;
			case SHOWSTATICS:
				map.put("SHOWSTATICS", null);
				break;

			}
			serverManageInfo.setCommand(map);
			DatagramPacket packet = Utility.writeMessageObject(ip, serverManageInfo.getClientPort(), serverManageInfo);
			DatagramSocket sendSoc = new DatagramSocket();
			sendSoc.send(packet);
			// break the loop if user enters "bye"
			if (inp.equals("bye"))
				break;
		}
	}

	private static void recieveMessagesFromServer() {
		new Thread(new Runnable() {
			public void run() {
				DatagramSocket mngrConnection = null;
						try {
					mngrConnection = new DatagramSocket(Constants.SERVER_MGR_SENDER_PORT);
				} catch (SocketException e1) {

				}
				while (true) {

					try {

						byte[] buf = new byte[5000];

						DatagramPacket packet = new DatagramPacket(buf, buf.length);
						mngrConnection.receive(packet);
						ServerManageInfo serverManageInfo = (ServerManageInfo) Utility.readMessageObject(packet);
						System.out.println("Server mngr recieved data" + serverManageInfo);
						Set<String> players=serverManageInfo.getPlayers();
						QueryStatistics stats=serverManageInfo.getQueryStatistics();
						if(players!=null) {
							System.out.println("List of users online:");
							players.forEach(item->{								
									System.out.println(item);
								}
							);
								
						}else if(stats!=null) {
							System.out.format("%32s%10d%16s", stats.getMinProcessTime(), stats.getMaxProcessTime(), stats.getAverageProcessTime(),stats.getNoOfQueriesProcessed());
						}
						// processServerCmds(new String(buf,0,packet.getLength()));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}).start();
	}



}
