package com.zarpator.tombot.servicelayer.receiving.telegramobjects;

public class TgmUser {
	private int id;
	private boolean is_bot;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public boolean isIs_bot() {
		return is_bot;
	}
	public void setIs_bot(boolean is_bot) {
		this.is_bot = is_bot;
	}
	public String getFirst_name() {
		return first_name;
	}
	public void setFirst_name(String first_name) {
		this.first_name = first_name;
	}
	private String first_name;
}
