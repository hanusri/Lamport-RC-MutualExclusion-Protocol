import java.io.Serializable;

/**
 * Created by Srikanth on 7/10/2016.
 */
public class ReleaseMessage extends Message implements Serializable {

    private static final long serialVersionUID = 1147742072340994819L;
    private int[] mutualExclusionClock;

    public ReleaseMessage(Node sourceNode, int clock, int[] mutualExclusionClock) {
        super(sourceNode);
        this.clock = clock;
        this.mutualExclusionClock = mutualExclusionClock;
    }

    public int[] getMutualExclusionClock() {
        return mutualExclusionClock;
    }
}
