import java.io.Serializable;

/**
 * Created by Srikanth on 7/10/2016.
 */
public class ReplyMessage extends Message implements Serializable {

    private static final long serialVersionUID = 1147742072340994819L;

    public ReplyMessage(Node sourceNode, int clock) {
	super(sourceNode);
	this.clock = clock;
    }
}
