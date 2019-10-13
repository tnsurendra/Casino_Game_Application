package com.playtech.casino.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.playtech.casino.common.MessageInfo;
import com.playtech.casino.common.Player;
import com.playtech.casino.common.QueryStatistics;
import com.playtech.casino.common.ServerManageInfo;
import com.playtech.casino.dao.PlayerDBService;
import com.playtech.casino.util.Constants;
import com.playtech.casino.util.SERVERMNGCMDS;
import com.playtech.casino.util.Utility;

public class CasinoServer {
	private final static Logger logger = Logger.getLogger(CasinoServer.class.getName());
	private DatagramSocket serverReciever;
	private int port;
	private PlayerDBService dbService;
	private int processedQueries;
	private List<Long> queryStatistics = new ArrayList<Long>();
	private boolean close;
	private Map<String, Integer> players = new ConcurrentHashMap<String, Integer>();

	private void run() throws Exception {

		DatagramPacket packet;

		// Receive the messages and process them by calling processMessage(...)
		CasinoServer.log( "Server is ready to receive messages from client...");

		// Async call for server mng messages
		listenForServerMngCommands();

		// Recieve message from clients and process
		listenForClientMessages();

	}

	private void listenForClientMessages() throws IOException, Exception {
		DatagramPacket packet;
		while (!close) {

			// Receive request from client
			int pacSize = 5000;
			packet = new DatagramPacket(new byte[pacSize], pacSize);

			serverReciever.receive(packet);
			Instant start = Instant.now();
			MessageInfo msg = (MessageInfo) Utility.readMessageObject(packet);
			String userName = msg.getPlayer().getUserName();

			if (null == userName) {
				CasinoServer.log( "Invalid user");
				continue;
			}
			if (players.containsKey(msg.getPlayer().getUserName())) {

			} else {
				CasinoServer.log( "Player Entered:" + userName);
				players.put(msg.getPlayer().getUserName(), msg.getClientPort());
			}
			processMessage(msg);
			Instant end = Instant.now();
			long timeElapsed = Duration.between(start, end).toMillis();
			queryStatistics.add(timeElapsed);
			processedQueries++;
		}
	}

	private void listenForServerMngCommands() throws Exception {
		new Thread(new Runnable() {
			public void run() {
				DatagramSocket mngrConnection = null;
				try {
					mngrConnection = new DatagramSocket(Constants.SERVER_MGR_LISTENING_PORT);
				} catch (SocketException e1) {
					System.err.print(e1);					
				}
				while (true) {

					try {						
						byte[] buf = new byte[5000];
						DatagramPacket packet = new DatagramPacket(buf, buf.length);
						mngrConnection.receive(packet);
						ServerManageInfo serverManageInfo = (ServerManageInfo) Utility.readMessageObject(packet);
						processServerCmds(serverManageInfo);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	public void processServerCmds(ServerManageInfo manageCmd) throws Exception {
		Map.Entry<String, String> entry = manageCmd.getCommand().entrySet().iterator().next();

		SERVERMNGCMDS cmd = Arrays.stream(SERVERMNGCMDS.values()).filter(e -> e.name().equalsIgnoreCase(entry.getKey()))
				.findAny().orElse(null);
		if (null == cmd) {
			CasinoServer.log( "Invalid server management command" + manageCmd);
			return;
		}
		CasinoServer.log( "Recieved message from SErver mgr:" + manageCmd);
		switch (cmd) {
		case SHUTDOWN:
			shutdownAllPlayers();
			break;
		case SHOWCLIENTS:
			ServerManageInfo serverManageInfo = new ServerManageInfo();
			serverManageInfo.setPlayers(players.keySet());
			serverManageInfo.setClientPort(Constants.SERVER_MGR_SENDER_PORT);
			Utility.sendMessageToHost(serverManageInfo);

			break;
		case KICKPLAYER:
			kickOffPlayer(entry.getValue());
			break;
		case SHOWSTATICS:
			ServerManageInfo serverMngInfo = new ServerManageInfo();
			serverMngInfo.setQueryStatistics(getStatistics());
			serverMngInfo.setClientPort(Constants.SERVER_MGR_SENDER_PORT);
			Utility.sendMessageToHost(serverMngInfo);

			break;

		}

	}

	public QueryStatistics getStatistics() {

		QueryStatistics stats = new QueryStatistics();
		stats.setNoOfQueriesProcessed(processedQueries);

		DoubleSummaryStatistics stat = queryStatistics.stream().mapToDouble((x) -> x).summaryStatistics();

		stats.setAverageProcessTime(stat.getAverage());
		stats.setMinProcessTime(stat.getMin());
		stats.setMaxProcessTime(stat.getMax());
		CasinoServer.log( "Statics:" + stats);
		return stats;

	}

	public boolean kickOffPlayer(String userName) throws Exception {
		boolean result = false;
		int port = players.get(userName);
		CasinoServer.log( "Kickingoff player" + userName + ":" + port);
		MessageInfo msg = new MessageInfo();
		msg.setClientPort(port);
		msg.setCommand(Constants.CLOSE_CONN);
		CasinoServer.log( "Player Exited:" + userName);
		Utility.sendMessageToHost(msg);
		players.remove(userName);
		result = true;
		return result;
	}

	public void shutdownAllPlayers() throws Exception {
		players.forEach((userName, port) -> {
			try {
				kickOffPlayer(userName);

			} catch (Exception e) {
				System.err.println(e);
			}
		});

		close = true;
		players.clear();
		System.exit(0);

	}

	public void processMessage(MessageInfo msg) throws Exception {
		String userName = msg.getPlayer().getUserName();
		Player plyr = msg.getPlayer();
		if (Constants.CLOSE_CONN.equalsIgnoreCase(msg.getCommand())) {
			players.remove(userName);

		} else if (userName != null && plyr != null && msg.getTransactionId() > 0) {
			// save data to DB
			dbService.savePlayer(msg.getPlayer());
			// reply back to cient
			int clientPort = players.get(userName);
			MessageInfo reply = new MessageInfo();
			reply.setClientPort(clientPort);
			Player player = new Player();
			reply.setTransactionId(msg.getTransactionId());
			// get prevoius balnce and find diff
			reply.setBalanceChange(dbService.getBalanceDiff(plyr.getUserName(), plyr.getBalance()));
			reply.setErrorCode(200);// Asuming OK
			player.setBalanceVersion(dbService.getNextBalanceVersion(userName));
			player.setBalance(plyr.getBalance());
			player.setUserName(userName);
			reply.setPlayer(player);
			Utility.sendMessageToHost(reply);

		} else {
			CasinoServer.log( "Invalid command from client");
		}

	}

	public CasinoServer(int rp) {
		// Initialize UDP socket for receiving data
		try {
			port = rp;
			serverReciever = new DatagramSocket(port);
			dbService = new PlayerDBService();
		} catch (SocketException e) {
			CasinoServer.log( "Error: Could not create socket on port " + port);
			System.exit(-1);
		}
		// Make it so the server can run.
		close = false;
	}

	public static void main(String args[]) throws Exception {
		int recvPort;

		recvPort = Constants.SERVER_LISTENING_PORT;// Integer.parseInt(args[0]);

		// Initialize Server object and start it by calling run()
		CasinoServer udpsrv = new CasinoServer(recvPort);
		try {
			udpsrv.run();
		} catch (SocketTimeoutException e) {
		}
	}
	 private static FileHandler fh;

	    static {
	        try {
	            fh = new FileHandler("casino_users.log", 0, 1, true);
	        } catch (IOException | SecurityException e) {
	        }
	    }

	    static void log(String msg) {
	       // Date dir1 = new java.util.Date(System.currentTimeMillis());
	        Logger logger = Logger.getLogger("Casino");
	        logger.addHandler(fh);
	        SimpleFormatter formatter = new SimpleFormatter();
	        fh.setFormatter(formatter);
	        logger.info(msg);

	    }
}
