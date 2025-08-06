package com.nonacept.javafx.scene.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import com.nonacept.javafx.listeners.ChildListener;
import com.nonacept.javafx.scene.layout.InternalWindow;
import com.nonacept.javafx.scene.layout.InternalWindowContent;
import com.nonacept.javafx.scene.layout.InternalWindowInitializer;

/**
 *
 * @author Douglas Rocha de Oliveira
 */
public class InternalWindowManager implements ChildListener {

    private static InternalWindowManager instance;

    private final Map<Object, Node> childs = new HashMap<>();
    private Pane managedPane;
    private ChildListener childListener;
    private InternalWindowInitializer<? extends InternalWindowContent> initializer;

    private InternalWindowManager() {
    }

    public static InternalWindowManager create() {
        if (instance == null) {
            instance = new InternalWindowManager();
        }
        return instance;
    }

    public InternalWindowManager managing(Pane pane) {
        managedPane = pane;
        Scene mainScene = pane.getScene();
        mainScene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.TAB) {
                InternalWindow activeWindow = findActiveInternalWindow(managedPane);
                if (activeWindow == null) {
                    return;
                }
                event.consume();

                Node focusOwner = mainScene.getFocusOwner();
                List<Node> focusables = collectFocusableNodes(activeWindow);
                if (focusables.isEmpty()) {
                    return;
                }

                int currentIndex = focusables.indexOf(focusOwner);
                boolean shift = event.isShiftDown();

                int nextIndex;
                if (currentIndex == -1) {
                    nextIndex = 0;
                } else {
                    nextIndex = (currentIndex + (shift ? -1 : 1) + focusables.size()) % focusables.size();
                }
                Node next = focusables.get(nextIndex);
                while (next.isDisable()) {
                    nextIndex = (nextIndex + (shift ? -1 : 1) + focusables.size()) % focusables.size();
                    next = focusables.get(nextIndex);
                }
                Platform.runLater(next::requestFocus);
            }
        });

        return this;
    }

    public InternalWindowManager withListener(ChildListener listener) {
        childListener = listener;
        return this;
    }

    public InternalWindowManager withInitializer(InternalWindowInitializer<? extends InternalWindowContent> init) {
        initializer = init;
        return this;
    }

    public static InternalWindowManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Create the InternalWindowManager first", new NullPointerException("instance is null"));
        }
        return instance;
    }

    private <T extends InternalWindowContent> InternalWindow ciw(String viewLocation,
            Stage stage, String classId, Consumer<T> customInit) {
        String className = classId;
        InternalWindow iw = (InternalWindow) childs.get(className);
        if (iw != null) {
            return iw;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(viewLocation));
            Pane p = loader.load();
            iw = new InternalWindow();
            if (initializer != null) {
                initializer.accept(loader.getController(), stage, iw);
            }
            if (customInit != null) {
                customInit.accept(loader.getController());
            }
            iw.addContent(p);
            iw.setControllerClass(loader.getController());
            iw.setClassId(className);
            iw.addChildListener(this);
            if (childListener != null) {
                iw.addChildListener(childListener);
            }
            childs.put(className, iw);
            return iw;
        } catch (Exception ex) {
            System.out.println(ex);
            throw new RuntimeException(ex.getMessage(), ex.getCause());
        }
    }

    public void siw(InternalWindow iw) {
        Objects.requireNonNull(managedPane, "A Pane must be managed in order to show a InternalWindow. Use: .managing(Pane) when creating the InternalWindowManager");
        if (managedPane.getChildren().contains(iw)) {
            iw.requestFocus();
            return;
        }
        managedPane.getChildren().add(iw);
    }

    public <T extends InternalWindowContent> void createUniqueInternalWindow(String viewLocation, Stage stage, Class<T> type, Consumer<T> customInit) {

        String key = type.getName();
        siw(ciw(viewLocation, stage, key, customInit));
    }

    public <T extends InternalWindowContent> void createInternalWindow(String viewLocation, Stage stage, Consumer<T> customInit) {
        String key = "" + System.currentTimeMillis();
        siw(ciw(viewLocation, stage, key, customInit));
    }

    public <T extends InternalWindowContent> InternalWindow createInternalWindow(String viewLocation, Stage stage, String identifier, Consumer<T> customInit) {
        return ciw(viewLocation, stage, identifier, customInit);
    }

    private InternalWindow findActiveInternalWindow(Pane root) {
        for (Node node : root.getChildren()) {
            if (node instanceof InternalWindow iw && iw.isActive()) {
                return iw;
            }
        }
        return null;
    }

    private List<Node> collectFocusableNodes(Parent parent) {
        List<Node> result = new ArrayList<>();
        for (Node node : parent.getChildrenUnmodifiable()) {
            if (node.isFocusTraversable() && node.isVisible()) {
                result.add(node);
            } else if (node instanceof Parent child) {
                result.addAll(collectFocusableNodes(child));
            }
        }
        return result;
    }

    @Override
    public void onChildOpen(Object obj) {
    }

    @Override
    public void onChildClose(Object obj) {
        childs.remove(obj);
    }
}
