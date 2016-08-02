import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Group on 7/7/2016.
 */
public class RCServiceProvider implements ICriticalSection {
    private Node listeningNode;
    private ArrayList<Message> releaseMessageList;
    private HashMap<Integer, Boolean> keysLog;

    // region Constructor

    public RCServiceProvider(Node node) {
        this.listeningNode = node;
        releaseMessageList = new ArrayList<>();
        keysLog = new HashMap<>(NodeRunner.getTotalNodes());
        for (Integer nodeId : NodeRunner.getNodeDictionary().keySet()) {
            keysLog.put(nodeId, nodeId >= listeningNode.getNodeID());
        }
    }

    // endregion

    // region Override Methods

    @Override
    public void csEnter() {
        synchronized (listeningNode.getCsStatus()) {
            listeningNode.setCsStatus(CSStatus.CSREQUESTED);
            listeningNode.getClock().incrementAndGet();
            requestAllKeys();
        }

        while (!allKeysKnown()) {
            try {
                Thread.sleep(ApplicationConstants.KEYLOG_VALIDATION_WAIT);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        synchronized (listeningNode.getCsStatus()) {
            System.out.println("Node " + listeningNode.getNodeID() + " entering the CS : " + listeningNode.getClock());
            listeningNode.setCsStatus(CSStatus.CSIN);
        }
    }

    @Override
    public void csLeave() {
        synchronized (listeningNode.getCsStatus()) {
            System.out.println("Node " + listeningNode.getNodeID() + " leaving the CS : " + listeningNode.getClock());
            listeningNode.setCsStatus(CSStatus.CSOUT);
            releaseResponseMessages();
        }
    }

    @Override
    public void processMessage(Message incomingMessage) {
        // if incoming message is Request Message then do the following
        // a. if listening node is already in CS, then add the request
        // to release message list
        // b. if listening node is requested for cs, then compare clock
        // and decide accordingly
        // c. if lisening node is not looking for CS, then send response
        if (incomingMessage instanceof RequestMessage) {
            synchronized (listeningNode.getCsStatus()) {
                switch (listeningNode.getCsStatus()) {
                    case CSIN:
                        releaseMessageList.add(incomingMessage);
                        break;

                    case CSREQUESTED:
                        int currentListeningNodeClock = listeningNode.getClock().get();
                        int clockDifference = incomingMessage.getClock() - currentListeningNodeClock;

                        if (clockDifference > 0 || (clockDifference == 0
                                && listeningNode.getNodeID() < incomingMessage.getSourceNode().getNodeID())) {
                            releaseMessageList.add(incomingMessage);
                        } else if (clockDifference < 0 || (clockDifference == 0
                                && listeningNode.getNodeID() > incomingMessage.getSourceNode().getNodeID())) {
                            keysLog.put(incomingMessage.getSourceNode().getNodeID(), false);
                            Message requestResponseMessage =
                                    new RequestResponseMessage(listeningNode, currentListeningNodeClock);
                            listeningNode.send(incomingMessage.getSourceNode(), requestResponseMessage);
                        }
                        break;

                    case CSOUT:
                        keysLog.put(incomingMessage.getSourceNode().getNodeID(), false);
                        Message responseMessage = new ResponseMessage(listeningNode, listeningNode.getMutualExclusionClock());
                        listeningNode.send(incomingMessage.getSourceNode(), responseMessage);
                        break;
                }
            }

        } else if (incomingMessage instanceof ResponseMessage) {
            keysLog.put(incomingMessage.getSourceNode().getNodeID(), true);
            // check for mutual exclusion vector clock for testing mutual exclusion
            int[] receivedMutualExclusionCheck = ((ResponseMessage) incomingMessage).getMutualExclusionClock();
            listeningNode.processIncomingMutualExclusionClock(receivedMutualExclusionCheck);

        } else if (incomingMessage instanceof RequestResponseMessage) {
            keysLog.put(incomingMessage.getSourceNode().getNodeID(), true);
            releaseMessageList.add(incomingMessage);
        }
    }

    // endregion

    // region Private Methods

    private void requestAllKeys() {
        Message requestMessage = new RequestMessage(listeningNode, listeningNode.getClock().get());
        for (Integer nodeId : keysLog.keySet())
            // send request to all nodes whose key log is false
            if (!keysLog.get(nodeId))
                listeningNode.send(NodeRunner.getNodeDictionary().get(nodeId), requestMessage);
    }

    private boolean allKeysKnown() {
        for (Boolean hasKey : keysLog.values())
            // send request to all nodes whose key log is false
            if (!hasKey)
                return false;
        return true;
    }

    private void releaseResponseMessages() {
        for (Message message : releaseMessageList) {
            Message responseMessage = new ResponseMessage(listeningNode, listeningNode.getMutualExclusionClock());
            listeningNode.send(message.getSourceNode(), responseMessage);
            keysLog.put(message.getSourceNode().getNodeID(), false);
        }
        releaseMessageList.clear();
    }

    // endregion
}
