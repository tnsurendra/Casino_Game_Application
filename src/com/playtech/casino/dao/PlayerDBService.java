package com.playtech.casino.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Logger;

import com.playtech.casino.common.Player;
import com.playtech.casino.util.Utility;

public class PlayerDBService {
	Statement stmt = null;
	private final static Logger logger = Logger.getLogger(PlayerDBService.class.getName());

	private static Connection getConnection() throws Exception {
		
		Class.forName(Utility.getValue("db.driver"));
		Connection con = DriverManager.getConnection(Utility.getValue("db.url"), Utility.getValue("db.user"), Utility.getValue("db.password"));
		return con;

	}

	public boolean savePlayer(Player player) throws Exception {

		// System.out.println("saving player"+player);

		stmt = PlayerDBService.getConnection().createStatement();

		Player existingPlayer = getPlayerByUserName(player.getUserName());
		int result = 0;
		if (null == existingPlayer) {
			System.out.println( "new user");
			result = stmt.executeUpdate("INSERT INTO PLAYER VALUES ('" + player.getUserName() + "', "
					+ player.getBalanceVersion() + "," + player.getBalance() + ")");
		} else {
			System.out.println( "existing user");
			result = stmt.executeUpdate("UPDATE PLAYER SET BALANCE_VERSION =" + player.getBalanceVersion()
			+ " , BALANCE=" + player.getBalance() + "  WHERE USERNAME = '" + player.getUserName() + "'");
		}
		if (result > 0) {
			return true;
		} else {
			return false;
		}

	}

	public Player getPlayerByUserName(String userName) throws Exception {

		System.out.println( "getPlayerByUserName" + userName);

		stmt = PlayerDBService.getConnection().createStatement();
		ResultSet result = stmt.executeQuery("select * from player where USERNAME='" + userName + "'");
		Player player = null;
		while (result.next()) {
			player = new Player();
			player.setUserName(userName);
			player.setBalanceVersion(result.getFloat("BALANCE_VERSION"));
			player.setBalance(result.getDouble("BALANCE"));

		}

		return player;
	}

	public double getBalanceDiff(String userName, double updatedBalance) throws Exception {
		double balanceDiff = 0;
		Player player = getPlayerByUserName(userName);
		balanceDiff = updatedBalance - player.getBalance();
		System.out.println( "balace diff" + balanceDiff);
		return balanceDiff;

	}

	public float getNextBalanceVersion(String userName) throws Exception {
		float balVersion = 0;
		Player player = getPlayerByUserName(userName);

		System.out.println( "NEXT BALANCE VERISON:" + player.getBalanceVersion() + 1);
		return player.getBalanceVersion() + 1;
	}



}
