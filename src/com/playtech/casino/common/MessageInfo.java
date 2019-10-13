package com.playtech.casino.common;

import java.io.Serializable;
import java.util.List;

public class MessageInfo implements Serializable {

	public static final long serialVersionUID = 52L;

	private int clientPort, transactionId, errorCode;

	private double balanceChange;
	private String command;

	public int getClientPort() {
		return clientPort;
	}

	public int getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(int transactionId) {
		this.transactionId = transactionId;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public double getBalanceChange() {
		return balanceChange;
	}

	public void setBalanceChange(double balanceChange) {
		this.balanceChange = balanceChange;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public void setClientPort(int clientPort) {
		this.clientPort = clientPort;
	}

	private Player player;

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	@Override
	public String toString() {
		return "MessageInfo [clientPort=" + clientPort + ", transactionId=" + transactionId + ", errorCode=" + errorCode
				+ ", balanceChange=" + balanceChange + ", command=" + command + ", player=" + player + "]";
	}

}
