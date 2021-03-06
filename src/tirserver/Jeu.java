/*
 * 
 * 
 * 
 */
package tirserver;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Jeu.java
 *
 */
public class Jeu implements Observer {

    private final Reception serveur;
    private final Map map;

    private final ArrayList<Client> listClient;
    private int id;

    public Jeu() throws IOException {
	serveur = new Reception();
	serveur.addObserver(this);
	map = new Map();
	listClient = new ArrayList<Client>();
	id = 1;
	System.out.println(map);
    }

    public void start() {
	serveur.start();
    }

    public void stop() {
	serveur.stop();
	listClient.stream().forEach((client) -> {
	    try {
		client.stop();
	    } catch (IOException ex) {
		Logger.getLogger(Jeu.class.getName()).log(Level.SEVERE, null, ex);
	    }
	});
    }

    @Override
    public void update(Observable o, Object arg) {
	try {
	    if (o instanceof Reception) {
		initClient((Socket) arg);
	    } else if (o instanceof Client) {
		initClientFin((Client) o, (Paquet) arg);
	    }
	} catch (IOException ex) {
	    Logger.getLogger(Jeu.class.getName()).log(Level.SEVERE, null, ex);
	}
    }

    private void initClient(Socket socket) throws IOException {
	id++;
	Client client = new Client(id, socket);
	client.addObserver(this);
	listClient.add(client);
	client.start();
    }

    private void initClientFin(Client client, Paquet paqPseudo) throws IOException {
	if (existPseudo(paqPseudo.getFirstMessage())) {
	    client.envoi("id", "false");
	    listClient.remove(client);
	    client.stop();
	} else {
	    client.envoi("id", Integer.toString(id));
	    map.addClient(client.getId());
	    System.out.println(map);
	    client.envoi("map", map.toEnvoi());
	}
    }

    private boolean existPseudo(String pseudo) {
	return listClient.stream().anyMatch((client) -> (pseudo.equals(client.getPseudo())));
    }

}
