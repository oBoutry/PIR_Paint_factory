package networkConnections;

import java.io.Serializable;

public class Exchange implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String sender;
	private String receiver;
	private Object object;

	public Exchange(String sender, String receiver, Object object) {
		this.sender = sender;
		this.receiver = receiver;
		this.object = object;
	}

	public String toString() {
		return "--- Exchange from " + sender + " to " + receiver + " ---\n" + "         " + object;
	}

	public String getReceiver() {
		return receiver;
	}
}
