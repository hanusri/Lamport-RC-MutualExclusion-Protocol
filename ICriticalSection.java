/**
 * Created by Group on 7/7/2016.
 */
public interface ICriticalSection {
    void csEnter();

    void csLeave();

    void processMessage(Message msg);
}
