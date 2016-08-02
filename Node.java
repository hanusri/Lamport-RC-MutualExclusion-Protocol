import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.HashMap;

/**
 * Created by Group on 7/7/2016.
 */
public class Node implements Serializable {
    private static final long serialVersionUID = 3606062186191127730L;
    private int nodeID;
    private String ipAddress;
    private int port;
    private transient SendController sendController;
    private transient ICriticalSection iCriticalSection;
    private transient AtomicInteger clock;
    private transient CSStatus csStatus;
    private transient HashMap<Integer, Boolean> terminationLog;
    private int[] mutualExclusionClock;
    // region Constructor

    public Node(int nodeID, String ipAddress, int port) {
        this.nodeID = nodeID;
        this.ipAddress = ipAddress;
        this.port = port;
        sendController = new SendController();
        iCriticalSection = NodeRunner.getMutualExclusionService() == MutualExclusionService.LAMPORT
                ? new LamportServiceProvider(this) : new RCServiceProvider(this);
        clock = new AtomicInteger(0);
        csStatus = CSStatus.CSOUT;
        terminationLog = new HashMap<>(NodeRunner.getTotalNodes());
        mutualExclusionClock = new int[NodeRunner.getTotalNodes()];
    }

    // endregion

    // region Getter and Setter

    public int getNodeID() {
        return nodeID;
    }

    public void setNodeID(int nodeID) {
        this.nodeID = nodeID;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public AtomicInteger getClock() {
        return clock;
    }

    public CSStatus getCsStatus() {
        return csStatus;
    }

    public void setCsStatus(CSStatus csStatus) {
        this.csStatus = csStatus;
    }

    public ICriticalSection getiCriticalSection() {
        return iCriticalSection;
    }

    public HashMap<Integer, Boolean> getTerminationLog() {
        return terminationLog;
    }

    public void setTerminationLog(HashMap<Integer, Boolean> terminationLog) {
        this.terminationLog = terminationLog;
    }

    public int[] getMutualExclusionClock() {
        return mutualExclusionClock;
    }

    // endregion

    // region Public Methods

    public void initializeNode() {
        try {
            // start listner thread
            new Thread(new Connector()).start();
            Thread.sleep(ApplicationConstants.APPLICATION_INITIAL_DELAY);

            sendController.initializeController(NodeRunner.getNodeDictionary().values());
            initilizeterminationLog();
            Thread.sleep(ApplicationConstants.APPLICATION_INITIAL_DELAY);
            // start application thread
            new Thread(new ApplicationRunner(this)).start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void EnterCriticalSection() {
        iCriticalSection.csEnter();
    }

    public void ExitCriticalSection() {
        iCriticalSection.csLeave();
    }

    public void send(Node destinationNode, Message sendMessage) {
    /*
     * since every receiveController thread uses this resource, it must be
	 * synchronized
	 */
        synchronized (sendController) {
            sendController.send(destinationNode, sendMessage);
        }
    }

    public void broadcast(Message braodcastMessage) {
        synchronized (sendController) {
            for (Node destinationNode : NodeRunner.getNodeDictionary().values()) {
                sendController.send(destinationNode, braodcastMessage);
            }
        }
    }

    public boolean isAllTerminationReceived() {
        for (boolean value : terminationLog.values())
            if (!value)
                return false;

        return true;
    }

    public void terminateNode() {
        sendController.haltController();
        // System.exit(0);
    }

    public void mergeMutualExclusionClock(int[] messageMutualExclusionCheck) {
        for (int i = 0; i < mutualExclusionClock.length; i++) {
            mutualExclusionClock[i] = Math.max(mutualExclusionClock[i], messageMutualExclusionCheck[i]);
        }
    }

    public void processIncomingMutualExclusionClock(int[] receivedMutualExclusionCheck) {
        // merge incoming mutual exclusion clock with node's mutual exclusion clock
        synchronized (mutualExclusionClock) {
            mergeMutualExclusionClock(receivedMutualExclusionCheck);
        }

        // check for any odd value in mutual exclusion clock vector
        if (!NodeRunner.checkMutualExclusion(mutualExclusionClock)) {
            System.out.println("Fatal Error: Mutual Exclusion violated");
            broadcast(new TerminationMessage(this));
            System.exit(-1);
        }
    }

    // endregion

    // region Private Methods

    private void initilizeterminationLog() {
        for (Integer i : NodeRunner.getNodeDictionary().keySet()) {
            terminationLog.put(i, false);
        }
    }

    // endregion

    private class Connector implements Runnable {
        @Override
        public void run() {
            ServerSocket serverSocket = null;
            try {
                System.out.println("Started Listening from " + getNodeID());
                serverSocket = new ServerSocket(getPort());
        /* creating new listener thread for each neighbor */
                for (int i = 0; i < NodeRunner.getTotalNodes(); i++) {
                    Socket socket = serverSocket.accept();
                    new Thread(new ReceiveController(socket, Node.this)).start();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                try {
                    if (serverSocket != null)
                        serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
