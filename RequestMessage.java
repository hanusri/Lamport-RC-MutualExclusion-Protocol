import java.io.Serializable;

/**
 * Created by Srikanth on 7/10/2016.
 */
public class RequestMessage extends Message implements Serializable, Comparable<RequestMessage> {

    private static final long serialVersionUID = 1147742072340994819L;

    public RequestMessage(Node sourceNode, int clock) {
        super(sourceNode);
        this.clock = clock;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        RequestMessage other = (RequestMessage) obj;
        if (this.compareTo(other) != 0)
            return false;
        return true;
    }

    @Override
    public int compareTo(RequestMessage o) {
        int d = clock - o.clock;
        if (d != 0)
            return d;
        return getSourceNode().getNodeID() - o.getSourceNode().getNodeID();
    }
}
