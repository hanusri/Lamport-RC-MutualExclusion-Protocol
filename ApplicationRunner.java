import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by Srikanth on 7/8/2016.
 */
public class ApplicationRunner implements Runnable {
    private Node currentNode;

    public ApplicationRunner(Node currentNode) {
        this.currentNode = currentNode;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < NodeRunner.getMaxRequestCount(); i++) {
                currentNode.EnterCriticalSection();

                csExecute();

                currentNode.ExitCriticalSection();

                if (i < NodeRunner.getMaxRequestCount() - 1) {
            /* Inter-request delay */
                    long d = genExpRand(NodeRunner.getInterRequestDelay());
                    System.out.println("Sleeping before the next request for " + d + " milliseconds...");
                    Thread.sleep(d);
                }
            }
            currentNode.broadcast(new TerminationMessage(currentNode));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private long genExpRand(int mean) {
        return Math.round(-mean * Math.log(1 - Math.random()));
    }

    private void csExecute() {
        PrintWriter fw;
        try {
            // increment mutual exclusion clock
            synchronized (currentNode.getMutualExclusionClock()) {
                currentNode.getMutualExclusionClock()[currentNode.getNodeID()]++;
            }
            fw = new PrintWriter(new FileWriter(ApplicationConstants.CS_EXECUTION_FILE, true));
            fw.println("CS Enter by Node " + currentNode.getNodeID());
            fw.flush();
            fw.close();

	    /* CS Execution time */
            long c = genExpRand(NodeRunner.getCsExecutionTime());
            System.out.println("Executing critical section for " + c + " milliseconds...");
            Thread.sleep(c);

            fw = new PrintWriter(new FileWriter(ApplicationConstants.CS_EXECUTION_FILE, true));
            fw.println("CS Exit by Node " + currentNode.getNodeID());
            fw.flush();
            fw.close();
            // increment mutual exclusion clock
            synchronized (currentNode.getMutualExclusionClock()) {
                currentNode.getMutualExclusionClock()[currentNode.getNodeID()]++;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
