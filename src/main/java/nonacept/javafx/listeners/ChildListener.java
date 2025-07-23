package nonacept.javafx.listeners;

/**
 * A listener for a class that is called from another, that needs to know when it's closed
 * @author Douglas Rocha de Oliveira
 */
public interface ChildListener {

    void onChildOpen(Object obj);

    void onChildClose(Object obj);
}
