import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by Group on 7/7/2016.
 */
public class SendController {
    private HashMap<Integer, Socket> socketMap;
    private HashMap<Integer, ObjectOutputStream> outputMap;

    public SendController() {
	socketMap = new HashMap<>();
	outputMap = new HashMap<>();
    }

    public void initializeController(Collection<Node> nodes) {
	if (nodes != null) {
	    try {
		for (Node neighbour : nodes) {
		    Socket clientSocket = new Socket(neighbour.getIpAddress(), neighbour.getPort());
		    socketMap.put(neighbour.getNodeID(), clientSocket);
		    outputMap.put(neighbour.getNodeID(), new ObjectOutputStream(clientSocket.getOutputStream()));
		}
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }
	}
    }

    public void haltController() {
	try {
	    for (Integer nodeId : outputMap.keySet()) {
		outputMap.get(nodeId).close();
	    }
	    for (Integer nodeId : socketMap.keySet()) {
		socketMap.get(nodeId).close();
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	}

    }

    public void send(Node destinationNode, Message sendMessage) {
	try {
	    System.out.println(sendMessage.getClass().getSimpleName() + " sent to Node " + destinationNode.getNodeID());
	    ObjectOutputStream output = outputMap.get(destinationNode.getNodeID());
	    output.writeObject(sendMessage);
	    output.flush();
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }
}
