import java.io.Serializable;

/**
 * Created by Group on 7/7/2016.
 */
public class Message implements Serializable {

    private static final long serialVersionUID = -6212779681776447749L;

    private Node sourceNode;

    protected int clock;

    public Message(Node sourceNode) {
	this.sourceNode = sourceNode;
    }

    public Node getSourceNode() {
	return sourceNode;
    }

    public void setSourceNode(Node sourceNode) {
	this.sourceNode = sourceNode;
    }

    public int getClock() {
	return clock;
    }
}
