package networkConnections;

import java.io.Serializable;

/**
 * Objet Exchange permettant de suivre les communications reseaux via
 * l'ExchangeMonitor recevant une copie de chaque echange
 * 
 * @author boutryoscar
 *
 */
public class Exchange implements Serializable {

	/**
	 * SerialVersionUID permettant d'eviter les problemes de versions differentes
	 * pour les objets serializables
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Emetteur de l'echange
	 */
	private String sender;
	/**
	 * Destinataire de l'echange
	 */
	private String receiver;
	/**
	 * Objet envoye
	 */
	private Object object;

	/**
	 * <b>Constructeur de Exchange</b>
	 * 
	 * Initialise chacun des attribus de l'objet Exchange
	 * 
	 * @param sender   L'emetteur
	 * @param receiver Le destinataire
	 * @param object   L'objet envoye
	 * 
	 */
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
