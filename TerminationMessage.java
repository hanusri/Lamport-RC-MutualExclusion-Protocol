import java.io.Serializable;

/**
 * Created by Srikanth on 7/11/2016.
 */
public class TerminationMessage extends Message implements Serializable {

    private static final long serialVersionUID = -3762153858830175570L;

    public TerminationMessage(Node sourceNode) {
	super(sourceNode);
    }
}
