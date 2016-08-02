import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by Group on 7/7/2016.
 */
public class NodeRunner {
    private static int totalNodes;
    private static int interRequestDelay;
    private static int csExecutionTime;
    private static int maxRequestCount;
    private static MutualExclusionService mutualExclusionService;
    private static HashMap<Integer, Node> nodeDictionary;

    // region Getter and Setter

    public static int getTotalNodes() {
        return totalNodes;
    }

    public static int getInterRequestDelay() {
        return interRequestDelay;
    }

    public static int getCsExecutionTime() {
        return csExecutionTime;
    }

    public static int getMaxRequestCount() {
        return maxRequestCount;
    }

    public static HashMap<Integer, Node> getNodeDictionary() {
        return nodeDictionary;
    }

    public static MutualExclusionService getMutualExclusionService() {
        return mutualExclusionService;
    }

    // endregion

    private static void readFile(String fileName) throws IOException {
        File file = new File(fileName);
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedFile = new BufferedReader(fileReader);
        String newLine = null;
        int fileSection = 1;

        while ((newLine = bufferedFile.readLine()) != null) {
            // Comment line;Skip it
            if (newLine.startsWith(ApplicationConstants.COMMENT_INDICATOR))
                continue;
            // Blank line indicates end of section
            if (newLine.isEmpty()) {
                fileSection++;
                continue;
            }

            String[] lineList = newLine.split("\\s+");
            if (fileSection == 1) {
                totalNodes = Integer.parseInt(lineList[0]);
                interRequestDelay = Integer.parseInt(lineList[1]);
                csExecutionTime = Integer.parseInt(lineList[2]);
                maxRequestCount = Integer.parseInt(lineList[3]);
            }

            if (fileSection == 2) {
                if (nodeDictionary == null)
                    nodeDictionary = new HashMap<>(totalNodes);

                int nodeID = Integer.parseInt(lineList[0]);
                Node newNode = new Node(nodeID, lineList[1], Integer.parseInt(lineList[2]));
                nodeDictionary.put(nodeID, newNode);
            }
        }
        bufferedFile.close();
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.print("Invalid Argument passed");
            System.exit(0);
        }
        try {
            mutualExclusionService = (args[2].equals(ApplicationConstants.LAMPORT_CODE)) ? MutualExclusionService.LAMPORT
                    : MutualExclusionService.RC;

            readFile(args[1]);

            // Create Current Node
            int currentNodeId = Integer.parseInt(args[0]);
            Node currentNode = nodeDictionary.get(currentNodeId);

            currentNode.initializeNode();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static boolean checkMutualExclusion(int[] mutualExclusionCheck) {
        for (Integer i : mutualExclusionCheck)
            if (i % 2 == 1)
                return false;

        return true;
    }
}

enum MutualExclusionService {
    LAMPORT, RC
}
