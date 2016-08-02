import java.io.Serializable;

/**
 * Created by Srikanth on 7/10/2016.
 */
public class RequestResponseMessage extends Message implements Serializable {

    private static final long serialVersionUID = -6349458521341465075L;

    public RequestResponseMessage(Node sourceNode, int clock) {
	super(sourceNode);
	this.clock = clock;
    }
}
