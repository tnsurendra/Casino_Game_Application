package com.playtech.casino.common;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ServerManageInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6037213669113040270L;
	private Set<String> players;
	private QueryStatistics queryStatistics;
	private Map<String, String> command;
	private int clientPort;

	public Set<String> getPlayers() {
		return players;
	}

	public void setPlayers(Set<String> players) {
		this.players = players;
	}

	public QueryStatistics getQueryStatistics() {
		return queryStatistics;
	}

	public void setQueryStatistics(QueryStatistics queryStatistics) {
		this.queryStatistics = queryStatistics;
	}

	public Map<String, String> getCommand() {
		return command;
	}

	public void setCommand(Map<String, String> command) {
		this.command = command;
	}

	public int getClientPort() {
		return clientPort;
	}

	public void setClientPort(int clientPort) {
		this.clientPort = clientPort;
	}

	@Override
	public String toString() {
		return "ServerManageInfo [players=" + players + ", queryStatistics=" + queryStatistics + ", command=" + command
				+ ", clientPort=" + clientPort + "]";
	}

}
