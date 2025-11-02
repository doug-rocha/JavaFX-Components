/*
 * The MIT License
 *
 * Copyright 2025 Douglas Rocha de Oliveira.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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
import javafx.scene.input.MouseEvent;

/**
 * Singleton manager for {@link InternalWindow} instances.
 *
 * <p>
 * This class handles the creation, management, and focus control of internal
 * windows inside a {@link Pane}, emulating a
 * {@link javax.swing.JDesktopPane}-like behavior in JavaFX.</p>
 *
 * <p>
 * It also handles keyboard traversal (Tab focus), window theming, and
 * communicates with child listeners.</p>
 *
 * <p>
 * Usage example:
 * <pre>{@code
 * Pane desktopPane = new Pane();
 * InternalWindowManager manager = InternalWindowManager.create()
 *     .managing(desktopPane)
 *     .defaultTheme(InternalWindow.Theme.NONACEPT)
 *     .withListener(myChildListener)
 *     .withInitializer((controller, stage, window) -> {
 *         // custom initialization
 *     });
 *
 * // Create and show a unique internal window
 * manager.createUniqueInternalWindow("/views/MyView.fxml", stage, MyController.class, ctrl -> {
 *     ctrl.initData(someData);
 * });
 * }</pre>
 *
 * @see InternalWindow
 * @see InternalWindowContent
 * @see ChildListener
 *
 * @author Douglas Rocha de Oliveira
 */
public class InternalWindowManager implements ChildListener {

    private static InternalWindowManager instance;

    private final Map<Object, Node> childs = new HashMap<>();
    private Pane managedPane;
    private ChildListener childListener;
    private InternalWindowInitializer<? extends InternalWindowContent> initializer;

    private InternalWindow.Theme defaultTheme = InternalWindow.Theme.JAVAFX;

    private InternalWindowManager() {
    }

    /**
     * Creates (or returns existing) singleton instance.
     *
     * @return InternalWindowManager instance
     */
    public static InternalWindowManager create() {
        if (instance == null) {
            instance = new InternalWindowManager();
        }
        return instance;
    }

    /**
     * Sets the {@link Pane} to be managed by this manager.
     *
     * <p>
     * The manager will add internal windows to this pane and handle focus and
     * keyboard traversal.</p>
     *
     * @param pane the container Pane
     * @return this manager
     */
    public InternalWindowManager managing(Pane pane) {
        managedPane = pane;
        managedPane.setOnMousePressed(event -> verifyFocus(event));
        Scene mainScene = pane.getScene();
        managedPane.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.TAB) {
                InternalWindow activeWindow = findActiveInternalWindow(managedPane);
                event.consume();
                if (activeWindow == null) {
                    return;
                }
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

                Platform.runLater(next::requestFocus);
            }
        });
        return this;
    }

    /**
     * Sets a {@link ChildListener} to receive notifications of child window
     * events.
     *
     * @param listener the listener
     * @return this manager
     */
    public InternalWindowManager withListener(ChildListener listener) {
        childListener = listener;
        return this;
    }

    /**
     * Sets a custom {@link InternalWindowInitializer} for all created windows.
     *
     * @param init the initializer
     * @return this manager
     */
    public InternalWindowManager withInitializer(InternalWindowInitializer<? extends InternalWindowContent> init) {
        initializer = init;
        return this;
    }

    /**
     * Sets the default theme for all new internal windows.
     *
     * @param theme the theme
     * @return this manager
     */
    public InternalWindowManager defaultTheme(InternalWindow.Theme theme) {
        defaultTheme = theme;
        return this;
    }

    /**
     * Returns the singleton instance.
     *
     * @return the InternalWindowManager singleton
     * @throws IllegalStateException if the manager hasn't been created yet
     */
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
            iw.addContent(p);
            iw.setControllerClass(loader.getController());
            iw.setClassId(className);
            iw.addChildListener(this);
            iw.setTheme(defaultTheme);
            if (childListener != null) {
                iw.addChildListener(childListener);
            }
            if (initializer != null) {
                initializer.accept(loader.getController(), stage, iw);
            }
            if (customInit != null) {
                customInit.accept(loader.getController());
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

    /**
     * Creates and shows a unique internal window identified by the controller
     * class.
     *
     * <p>
     * If a window with the same controller already exists, it will be focused
     * instead of creating a new one.</p>
     *
     * @param viewLocation FXML location
     * @param stage owner stage
     * @param type controller class
     * @param customInit custom initialization for the controller
     */
    public <T extends InternalWindowContent> void createUniqueInternalWindow(String viewLocation, Stage stage, Class<T> type, Consumer<T> customInit) {
        String key = type.getName();
        siw(ciw(viewLocation, stage, key, customInit));
    }

    /**
     * Creates and shows a unique internal window identified by a string.
     *
     * <p>
     * If a window with the same identifier already exists, it will be focused
     * instead of creating a new one.</p>
     *
     * @param viewLocation FXML location
     * @param stage owner stage
     * @param identifier custom identifier
     * @param customInit custom initialization for the controller
     */
    public <T extends InternalWindowContent> void createUniqueInternalWindow(String viewLocation, Stage stage, String identifier, Consumer<T> customInit) {
        siw(ciw(viewLocation, stage, identifier, customInit));
    }

    /**
     * Creates a new internal window without uniqueness constraint.
     *
     * @param viewLocation FXML location
     * @param stage owner stage
     * @param customInit custom initialization
     */
    public <T extends InternalWindowContent> void createInternalWindow(String viewLocation, Stage stage, Consumer<T> customInit) {
        String key = "" + System.currentTimeMillis();
        siw(ciw(viewLocation, stage, key, customInit));
    }

    /**
     * Creates and returns an internal window without adding it to the pane.
     *
     * @param viewLocation FXML location
     * @param stage owner stage
     * @param identifier custom identifier
     * @param customInit custom initialization
     * @return InternalWindow instance
     */
    public <T extends InternalWindowContent> InternalWindow createInternalWindow(String viewLocation, Stage stage, String identifier, Consumer<T> customInit) {
        return ciw(viewLocation, stage, identifier, customInit);
    }

    /**
     * Changes the theme of all managed internal windows.
     *
     * @param theme the new theme
     */
    public void changeTheme(InternalWindow.Theme theme) {
        defaultTheme = theme;
        childs.forEach((obj, node) -> {
            if (node instanceof InternalWindow iw) {
                iw.setTheme(defaultTheme);
            }
        });
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
            if (node.isFocusTraversable() && node.isVisible() && !node.isDisabled()) {
                result.add(node);
            } else if (node instanceof Parent child) {
                result.addAll(collectFocusableNodes(child));
            }
        }
        return result;
    }

    private void verifyFocus(MouseEvent event) {
        InternalWindow activeWindow = findActiveInternalWindow(managedPane);
        if (activeWindow == null) {
            return;
        }
        if (!activeWindow.isAncestorOf((Node) event.getTarget())) {
            activeWindow.releaseFocus();
        }
    }

    @Override
    public void onChildOpen(Object obj) {
    }

    /**
     * Closes a child window (called automatically when window closes).
     *
     * @param obj child object
     */
    @Override
    public void onChildClose(Object obj) {
        childs.remove(obj);
    }
}
