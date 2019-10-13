package com.playtech.casino.common;

import java.io.Serializable;

public class Player implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private float balanceVersion;
	private double balance;
	private String userName;

	public float getBalanceVersion() {
		return balanceVersion;
	}

	public void setBalanceVersion(float balanceVersion) {
		this.balanceVersion = balanceVersion;
	}

	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Override
	public String toString() {
		return "Player [balanceVersion=" + balanceVersion + ", balance=" + balance + ", userName=" + userName + "]";
	}
}
