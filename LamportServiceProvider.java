import java.util.HashMap;
import java.util.PriorityQueue;

/**
 * Created by Group on 7/7/2016.
 */
public class LamportServiceProvider implements ICriticalSection {
    private Node listeningNode;
    private RequestMessage selfRequest;
    private PriorityQueue<RequestMessage> requestPQ;
    private HashMap<Integer, Boolean> grantsLog;

    public LamportServiceProvider(Node listeningNode) {
        this.listeningNode = listeningNode;
        requestPQ = new PriorityQueue<>(NodeRunner.getTotalNodes());
        grantsLog = new HashMap<>(NodeRunner.getTotalNodes());
        clearGrantsLog();
    }

    @Override
    public void csEnter() {
        synchronized (listeningNode.getCsStatus()) {
            listeningNode.setCsStatus(CSStatus.CSREQUESTED);
            clearGrantsLog();
            selfRequest = new RequestMessage(listeningNode, listeningNode.getClock().get());
            listeningNode.broadcast(selfRequest);
        }

        while (!allGrantsReceived() || !selfRequest.equals(requestPQ.peek())) {
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
            listeningNode.broadcast(new ReleaseMessage(listeningNode, listeningNode.getClock().get(), listeningNode.getMutualExclusionClock()));
            selfRequest = null;
        }
    }

    @Override
    public void processMessage(Message incomingMessage) {
        synchronized (listeningNode.getCsStatus()) {
            if (incomingMessage instanceof RequestMessage) {
                int clock = listeningNode.getClock().incrementAndGet();
                requestPQ.add(new RequestMessage(incomingMessage.getSourceNode(), incomingMessage.getClock()));
                listeningNode.send(incomingMessage.getSourceNode(), new ReplyMessage(listeningNode, clock));

            } else if (incomingMessage instanceof ReleaseMessage) {
                // check for mutual exclusion vector clock for testing mutual exclusion
                int[] receivedMutualExclusionCheck = ((ReleaseMessage) incomingMessage).getMutualExclusionClock();
                listeningNode.processIncomingMutualExclusionClock(receivedMutualExclusionCheck);

                RequestMessage completedRequest = null;
                for (RequestMessage request : requestPQ) {
                    if (request.getSourceNode().equals(incomingMessage.getSourceNode())) {
                        completedRequest = request;
                        break;
                    }
                }
                requestPQ.remove(completedRequest);
            }

            if (listeningNode.getCsStatus() == CSStatus.CSREQUESTED
                    && incomingMessage.getClock() > selfRequest.getClock()) {
                grantsLog.put(incomingMessage.getSourceNode().getNodeID(), true);
            }
        }
    }

    private boolean allGrantsReceived() {
        for (Boolean hasKey : grantsLog.values())
            if (!hasKey)
                return false;
        return true;
    }

    private void clearGrantsLog() {
        for (Integer nodeId : NodeRunner.getNodeDictionary().keySet())
            grantsLog.put(nodeId, false);
    }
}
