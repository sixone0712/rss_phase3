package jp.co.canon.rss.logmanager.util;

public enum ErrorMessage {
	DUPLICATE_USERNAME("duplicate username"),
	INVALID_PASSWORD("invalid password"),
	INVALID_USERNAME("invalid username"),
	INVALID_CURRENT_PASSWORD("invalid current password"),
	INVALID_ROLES("invalid roles"),
	INVALID_USER("invalid user"),
	INVALID_REFRESH_TOKEN("invalid refresh token"),
	BLOCKED_TOKEN("blocked token")


	;

	private final String message;

	ErrorMessage(String message) {
		this.message = message;
	}

	public String getMsg() {
		return this.message;
	}
}
