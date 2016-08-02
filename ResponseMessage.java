import java.io.Serializable;

/**
 * Created by Srikanth on 7/10/2016.
 */
public class ResponseMessage extends Message implements Serializable {

    private static final long serialVersionUID = -46275438406883457L;
    private int[] mutualExclusionClock;

    public ResponseMessage(Node sourceNode, int[] mutualExclusionClock) {
        super(sourceNode);
        this.mutualExclusionClock = mutualExclusionClock;
    }

    public int[] getMutualExclusionClock() {
        return mutualExclusionClock;
    }
}
