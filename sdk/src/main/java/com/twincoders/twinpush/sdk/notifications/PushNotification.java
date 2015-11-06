package com.twincoders.twinpush.sdk.notifications;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class PushNotification implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/* Properties */
	private String id;
	private String title;
	private String message;
	private String sound;
	private String richURL;
	private Date date;
	private List<String> tags = new ArrayList<String>();
	private Map<String, String> customProperties;
	
	/* Getters & Setters */
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getSound() {
		return sound;
	}
	public void setSound(String sound) {
		this.sound = sound;
	}
	public boolean isRichNotification() {
		return richURL != null && richURL.trim().length() > 0;
	}
	public String getRichURL() {
		return richURL;
	}
	public void setRichURL(String richURL) {
		this.richURL = richURL;
	}
	public Map<String, String> getCustomProperties() {
		return customProperties;
	}
	public void setCustomProperties(Map<String, String> customProperties) {
		this.customProperties = customProperties;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public List<String> getTags() {
		return tags;
	}
	public void setTags(List<String> tags) {
		this.tags = tags;
	}
	
	public boolean hasTitle() {
		return getTitle() != null && getTitle().trim().length() > 0;
	}
}
