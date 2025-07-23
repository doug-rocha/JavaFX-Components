package nonacept.javafx.scene.layout;

import java.util.HashSet;
import java.util.Set;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import nonacept.javafx.listeners.ChildListener;

/**
 * A Node that works like a {@link javax.swing.JInternalFrame}, it can be a
 * child of a {@link Pane}, and can be moved, resized and closed;
 *
 * @author Douglas Rocha de Oliveira
 */
public class InternalWindow extends Pane {

    private final Set<ChildListener> childListeners = new HashSet<>();

    private static DropShadow shadow = new DropShadow();

    private boolean active;

    private boolean maximizable = true;
    private boolean resizable = true;
    private boolean initial = true;

    private final boolean[] resize = {false, false, false, false}; //left, right, top, bottom

    private boolean maxed;
    private boolean restoring;
    private double oldX, oldY, oldWidth, oldHeight;

    private VBox container;
    private Label header;
    private HBox headerBar;

    private String controllerClass;

    /**
     * Creates a new instance
     */
    public InternalWindow() {
        this("JavaFX Application", null);
    }

    /**
     * Creates a new instance
     *
     * @param title the title, is shown on the title bar
     * @param content the content of the Window
     */
    public InternalWindow(String title, Node content) {
        initHeaderBar(title);
        initEffects();
        initResizable(10);
        Platform.runLater(this::requestFocus);
        Platform.runLater(this::parentListener);
        if (content != null) {
            container = new VBox(headerBar, content);
        } else {
            container = new VBox(headerBar);
        }
        container.setStyle("-fx-border-color: black; -fx-background-color: #f1f1f1;");
        container.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            requestFocus();
        });

        getChildren().add(container);
        setPrefSize(100, 100);

    }

    /**
     * Add a Node to the InternalWindow
     *
     * @param content the Node to be added
     * @return true if the Node is added successfully
     */
    public boolean addContent(Node content) {
        VBox.setVgrow(content, Priority.ALWAYS);
        ((Pane) content).setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        return container.getChildren().add(content);
    }

    /**
     * Remove a Node from the InternalWindow
     *
     * @param content the Node to be removed
     * @return true if the Node is removed successfully
     */
    public boolean removeContent(Node content) {
        return container.getChildren().remove(content);
    }

    /**
     * Set the title of the InternalWindow, shown on the title bar
     *
     * @param title the title of the InternalWindow
     */
    public void setTitle(String title) {
        header.setText(title);
    }

    /**
     * Get the title of the InternalWindow
     *
     * @return the title of the InternalWindow
     */
    public String getTitle() {
        return header.getText();
    }

    /**
     * Set the ControllerClass String, it's used to identify itself to the
     * {@link ChildListener}
     *
     * @param controllerClass the ControllerClass String
     */
    public void setControllerClass(String controllerClass) {
        this.controllerClass = controllerClass;
    }

    /**
     * Checks if this InternalWindow can be resized
     *
     * @return true if can be resized
     */
    @Override
    public boolean isResizable() {
        return resizable;
    }

    /**
     * Sets if this InternalWindow can be resized
     *
     * @param resizable the valu for resizable
     */
    public void setResizable(boolean resizable) {
        this.resizable = resizable;
    }

    /**
     * Checks if this InternalWindow can be maximized
     *
     * @return true if it's maximizable
     */
    public boolean isMaximizable() {
        return maximizable;
    }

    /**
     * Sets wheter this can be maximized
     *
     * @param maximizable the value for maximizable
     */
    public void setMaximizable(boolean maximizable) {
        this.maximizable = maximizable;
    }

    /**
     * Checks if this is currently maxed/maximized
     *
     * @return true if it's maxed
     */
    public boolean isMaxed() {
        return maxed;
    }

    /**
     * Sets wheter this is currently maxed/maximized
     *
     * @param maxed the value for maxed
     */
    public void setMaxed(boolean maxed) {
        if (!maximizable) {
            return;
        }
        if (maxed) {
            maximize();
        } else {
            restore();
        }
    }

    /**
     * Adds a {@link ChildListener}
     *
     * @param listener the {@link ChildListener} to be added
     * @return true if it's added successfully
     */
    public boolean addChildListener(ChildListener listener) {
        return childListeners.add(listener);
    }

    /**
     * Adds a {@link ChildListener}
     *
     * @param listener the {@link ChildListener} to be removed
     * @return true if it's removed successfully
     */
    public boolean removeChildListener(ChildListener listener) {
        return childListeners.remove(listener);
    }

    /**
     * Notify all {@link ChildListener} when openned
     */
    protected void notifyOpenChild() {
        childListeners.forEach(x -> x.onChildOpen(controllerClass));
    }

    /**
     * Notify all {@link ChildListener} when closed
     */
    protected void notifyClosedChild() {
        childListeners.forEach(x -> x.onChildClose(controllerClass));
    }

    private void initHeaderBar(String title) {
        headerBar = new HBox();
        header = new Label(title);
        header.setStyle("-fx-text-fill: white; -fx-padding: 4px;");
        header.setFont(Font.font(14));
        headerBar.setMinHeight(28);
        headerBar.setAlignment(Pos.CENTER_LEFT);
        headerBar.getChildren().addAll(header, windowControls());

        headerBar.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                toggleMaximize();
                return;
            }
            if (!maxed) {
                container.setUserData(new double[]{e.getSceneX(), e.getSceneY(), getLayoutX(), getLayoutY()});
            }
        });
        headerBar.setOnMouseDragged(e -> {
            if (maxed) {
                restore();
                return;
            }
            requestFocus();
            double[] data = (double[]) container.getUserData();
            double offsetX = e.getSceneX() - data[0];
            double offsetY = e.getSceneY() - data[1];
            setLayoutX(data[2] + offsetX);
            setLayoutY(data[3] + offsetY);
        });
    }

    private HBox windowControls() {
        HBox wc = new HBox();
        wc.setAlignment(Pos.TOP_RIGHT);
        wc.setSpacing(10);
        wc.setPadding(new Insets(1, 10, 0, 0));
        HBox.setHgrow(wc, Priority.ALWAYS);
        wc.setMaxWidth(Double.MAX_VALUE);

        Image img = new Image(getClass().getResourceAsStream("/x.png"));
        ImageView imgView = new ImageView(img);
        imgView.setFitWidth(14);
        imgView.setFitHeight(14);

        Button btnClose = new Button("Fechar");
        btnClose.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        btnClose.setPrefSize(24, 24);
        btnClose.setGraphic(imgView);
        btnClose.setOnAction((event) -> {
            close();
        });
        wc.getChildren().add(btnClose);
        return wc;
    }

    private void initEffects() {
        shadow.setRadius(10);
        shadow.setOffsetX(0);
        shadow.setOffsetY(0);
        shadow.setColor(Color.rgb(0, 0, 0, 0.5));

        setEffect(shadow);
    }

    private void notifyOthers() {
        clearAllShadows((Pane) getParent());
        changeAllBorders((Pane) getParent());
        restoreAll((Pane) getParent());
    }

    private void clearAllShadows(Pane parent) {
        for (Node node : parent.getChildren()) {
            if (node instanceof InternalWindow iw && !iw.equals(this)) {
                iw.setEffect(null);
            }
        }
    }

    private void setActiveHeader() {
        //headerBar.setStyle("-fx-background-color: linear-gradient(to bottom right, blue, lightskyblue);");
        headerBar.setStyle("-fx-background-color: linear-gradient(to bottom right, #3333aa, #6600ff);");
        active = true;
    }

    private void setInactiveHeader() {
        //headerBar.setStyle("-fx-background-color: linear-gradient(to bottom right, gray, lightgray);");
        headerBar.setStyle("-fx-background-color: linear-gradient(to bottom right, #9180aa, #e4d3f6);");
        active = false;
    }

    private void changeAllBorders(Pane parent) {
        for (Node node : parent.getChildren()) {
            if (node instanceof InternalWindow iw && !iw.equals(this)) {
                iw.setInactiveHeader();
            }
        }
    }

    /**
     * Inform all of the others InternalWindow on the same {@link Parent} that
     * they lost focus, and proceed to take it
     */
    @Override
    public void requestFocus() {
        if (!active) {
            notifyOthers();
            this.toFront();
            setEffect(shadow);
            setActiveHeader();
        }
    }

    private void toggleMaximize() {
        if (!maximizable) {
            return;
        }
        if (maxed) {
            restore();
        } else {
            maximize();
        }
    }

    private void restore() {
        if (!maxed) {
            return;
        }
        restoring = true;

        setLayoutX(oldX);
        setLayoutY(oldY);
        container.setPrefSize(oldWidth, oldHeight);

        maxed = false;

        restoring = false;

    }

    private void maximize() {
        if (maxed || getParent() == null) {
            return;
        }
        saveState();

        Pane parent = (Pane) getParent();
        setLayoutX(0);
        setLayoutY(0);
        container.setPrefSize(parent.getWidth(), parent.getHeight());

        maxed = true;
    }

    private void parentListener() {
        Pane parent = (Pane) getParent();
        parent.widthProperty().addListener((observable, oldValue, newValue) -> {
            if (maxed) {
                container.setPrefWidth(newValue.doubleValue());
                return;
            }
            if (getLayoutX() + container.getWidth() > newValue.doubleValue()) {
                setLayoutX(newValue.doubleValue() - container.getWidth());
            }
        });
        parent.heightProperty().addListener((observable, oldValue, newValue) -> {
            if (maxed) {
                container.setPrefHeight(newValue.doubleValue());
                return;
            }
            if (getLayoutY() + container.getHeight() > newValue.doubleValue()) {
                setLayoutY(newValue.doubleValue() - container.getHeight());
            }
        });
        layoutXProperty().addListener((observable, oldValue, newValue) -> {
            if (restoring || newValue.doubleValue() == 0) {
                return;
            }
            if (newValue.doubleValue() < 0) {
                setLayoutX(0);
                return;
            }
            if (newValue.doubleValue() + container.getWidth() > parent.getWidth()) {
                setLayoutX(parent.getWidth() - container.getWidth());
            }
        });

        layoutYProperty().addListener((observable, oldValue, newValue) -> {
            if (restoring || newValue.doubleValue() == 0) {
                return;
            }
            if (newValue.doubleValue() < 0) {
                setLayoutY(0);
                return;
            }
            if (newValue.doubleValue() + container.getHeight() > parent.getHeight()) {
                setLayoutY(parent.getHeight() - container.getHeight());
            }
        });
    }

    private void restoreAll(Pane parent) {
        for (Node node : parent.getChildren()) {
            if (node instanceof InternalWindow iw && !iw.equals(this)) {
                iw.restore();
            }
        }
    }

    private void saveState() {
        oldX = getLayoutX();
        oldY = getLayoutY();
        oldWidth = container.getWidth();
        oldHeight = container.getHeight();
    }

    /**
     * Remove itself from the {@link Parent} and notify all
     * {@link ChildListener} using {@link InternalWindow#notifyClosedChild() }
     */
    public void close() {
        ((Pane) this.getParent()).getChildren().remove(this);
        notifyClosedChild();
    }

    /**
     * Check if this is the current focused InternalWindow
     *
     * @return true if it's the current focused InternalWindow
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Check if this InternalWindow is on the tree of a component. Used to
     * process the focus change when clicked on a Node of a InternalWindow
     *
     * @param node the Node to be checked
     * @return true if the node is on the Node tree of this InternalWindow
     */
    public boolean isAncestorOf(Node node) {
        while (node != null) {
            if (node == this) {
                return true;
            }
            node = node.getParent();
        }
        return false;
    }

    //I'm not really sure if this is a good implementation.
    private void initResizable(double mouseBorder) {
        this.setOnMouseMoved((event) -> {
            if (isMaxed()) {
                return;
            }
            if (!isResizable()) {
                return;
            }
            double mouseX = event.getX();
            double mouseY = event.getY();
            double width = boundsInLocalProperty().get().getWidth();
            double height = boundsInLocalProperty().get().getHeight();
            resize[0] = false;
            resize[1] = false;
            resize[2] = false;
            resize[3] = false;

            if (Math.abs(mouseX) < mouseBorder / 2) {
                resize[0] = true;
            } else if (Math.abs(mouseX - (width - 15)) < mouseBorder) {
                resize[1] = true;
            }

            if (Math.abs(-mouseY) < mouseBorder / 2) {
                resize[2] = true;
            } else if (Math.abs(mouseY - (height - 15)) < mouseBorder) {
                resize[3] = true;
            }

            if (!isResizing()) {
                setCursor(Cursor.DEFAULT);
            } else if ((resize[0] && resize[2]) || (resize[1] && resize[3])) {
                setCursor(Cursor.NW_RESIZE);
            } else if ((resize[0] && resize[3]) || (resize[1] && resize[2])) {
                setCursor(Cursor.NE_RESIZE);
            } else if (resize[0] || resize[1]) {
                setCursor(Cursor.H_RESIZE);
            } else if (resize[2] || resize[3]) {
                setCursor(Cursor.V_RESIZE);
            }

        });
        this.setOnMouseDragged((event) -> {
            if (isMaxed()) {
                return;
            }
            if (!isResizable()) {
                return;
            }
            if (!isResizing()) {
                return;
            }
            if (event.getSceneX() < 0 || event.getSceneY() < 0) {
                return;
            }
            if (resize[0]) {
                double[] data = (double[]) container.getUserData();
                double offsetX = event.getSceneX() - data[0];
                double newWidth = data[4] - offsetX;
                if (newWidth > getMinWidth()) {
                    setLayoutX(data[2] + offsetX);
                    setPrefWidth(newWidth);
                    container.setPrefWidth(newWidth);
                }
            } else if (resize[1]) {
                double newWidth = event.getX();
                if (newWidth > getMinWidth()) {
                    setPrefWidth(newWidth);
                    container.setPrefWidth(newWidth);
                }
            }
            if (resize[2]) {
                double[] data = (double[]) container.getUserData();
                double offsetY = event.getSceneY() - data[1];
                double newHeight = data[5] - offsetY;
                if (newHeight > getMinHeight() - 2) {
                    setLayoutY(data[3] + offsetY);
                    setPrefHeight(newHeight);
                    container.setPrefHeight(newHeight);
                }
            } else if (resize[3]) {
                double newHeight = event.getY();
                if (newHeight > getMinHeight()) {
                    setPrefHeight(newHeight);
                    container.setPrefHeight(newHeight);
                }
            }
        });
        this.setOnMousePressed((event) -> {
            if (isMaxed() || !isResizing()) {
                return;
            }
            /*
            if it's the first click, it calculates the minimum size.
            I'm kinda sleepy, so possibly I'm not getting something, but for some reason, getMinWidth (or widthProperty().get()) don't return the actual size
            making a nightmare to resize properly, in fact it always return 100 (the default value, as it is in the constructor)
             */
            if (initial) {
                calculateMin();
                initial = false;
            }
            if (resize[0] || resize[2]) {
                container.setUserData(new double[]{
                    event.getSceneX(),
                    event.getSceneY(),
                    getLayoutX(),
                    getLayoutY(),
                    container.getWidth(),
                    container.getHeight()});
            }
        });
    }

    private boolean isResizing() {
        return resize[0] || resize[1] || resize[2] || resize[3];
    }

    private void calculateMin() {
        double width = 0;
        double height = 0;
        for (Node n : getChildren()) {
            Pane p = (Pane) n;
            if (p.widthProperty().get() > width) {
                width = p.widthProperty().get();
            }
            height += p.heightProperty().get();
        }
        setMinSize(width, height);
    }

}
