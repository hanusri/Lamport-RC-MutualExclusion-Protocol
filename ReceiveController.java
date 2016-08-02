import java.io.EOFException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class ReceiveController implements Runnable {
    private ObjectInputStream inputStream;
    private Socket socket;
    private Node currentNode;
    private int peerNodeId;

    public ReceiveController(Socket socket, Node currentNode) {
        this.socket = socket;
        this.currentNode = currentNode;
        peerNodeId = ApplicationConstants.PEER_NODE_ID;
        try {
            inputStream = new ObjectInputStream(socket.getInputStream());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                Message incomingMessage = (Message) inputStream.readObject();
                if (peerNodeId == ApplicationConstants.PEER_NODE_ID) {
                    peerNodeId = incomingMessage.getSourceNode().getNodeID();
                }
                System.out.println(incomingMessage.getClass().getSimpleName() + " received from Node " + peerNodeId);

                if (incomingMessage instanceof TerminationMessage) {
                    synchronized (currentNode.getTerminationLog()) {
                        currentNode.getTerminationLog().put(incomingMessage.getSourceNode().getNodeID(), true);
                    }
                    if (currentNode.isAllTerminationReceived()) {
                        inputStream.close();
                        socket.close();
                        currentNode.terminateNode();
                        break;
                    }

                } else {
                    currentNode.getiCriticalSection().processMessage(incomingMessage);
                }
            }

        } catch (EOFException ex) {
        /* Connection closed by peer */
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        System.out.println("Connection closed by " + peerNodeId);
    }
}
