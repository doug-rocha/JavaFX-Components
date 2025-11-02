# NonaCept JavaFX Components

![GitHub release](https://img.shields.io/github/v/release/doug-rocha/JavaFX-Components?style=for-the-badge)
![License](https://img.shields.io/github/license/doug-rocha/JavaFX-Components?style=for-the-badge)


Some JavaFX components for making your life easier.

---

## InternalWindow

`com.nonacept.javafx.scene.layout.InternalWindow` is similar to `JInternalWindow` from Swing.  

It extends `Pane` and can be added to any Parent, allowing content to be added inside a "window within a window". You can **move, resize, and maximize** it.

---

## How to use

For this example, I will assume you already know a bit of JavaFX and have a `Pane` ready to be used as a "Desktop":

### Create the InternalWindowManager

```java
InternalWindowManager iwm = InternalWindowManager
        .create()
        .managing(contentPanel);
```
After this the object `iwm` can be used to manage the creation of `InternalWindow`.<br>
You can create windows with:

- `createInternalWindow` → multiple instances allowed.
- `createUniqueInternalWindow` → only one instance; focuses if called again. (<b>Note:</b> the uniqueness is based on the type `Class`)

```java
iwm.createUniqueInternalWindow("/example.fxml",
                mainStage,
                ExampleController.class,
                customInitConsumer
);
```

Parameters:

| Parameter | Description |
|-----------|-------------|
| `/example.fxml` | Location of the FXML file |
| `mainStage` | Main `Stage` (can be null) |
| `ExampleController.class` | Your FXML controller implementing `InternalWindowContent` (also used to manage the uniqueness)|
| `customInitConsumer` | Custom code to run on initialization |

<b>Note:</b> every parameter passed when creating the `InternalWindowManager` will be used for all `InternalWindow` created by it.

---

### Optional: Global Initializer
```java
InternalWindowInitializer<ExampleController> initializer = (controller, stage, internalWindow) -> {
    controller.doSomething(stage);
    controller.alsoDoThis(internalWindow);
};

InternalWindowManager iwm = InternalWindowManager
        .create()
        .managing(contentPanel)
        .withInitializer(initializer);
```
The global initializer code, will be executed when creating a `InternalWindow`.<br/>
As you may see the initializer must be a implementation of `InternalWindowInitializer` with a controller type as parameter.<br/>
<b>Note:</b> if you intend to use InternalWindows with multiple types of controllers, define a more generic Controller class to be used here and extend by the actual Controllers, or use a customInitConsumer for every Window, as the `InternalWindow` creation example.

---

### Optional: Listener & Theme
You can provide an Object that implements `ChildListener` in order to receive calls when the `InternalWindow` opens or closes; a `InternalWindow.Theme` can also be passed:

```java
InternalWindowManager iwm = InternalWindowManager
        .create()
        .managing(contentPanel)
        .withListener(this)                             // listens to open/close
        .defaultTheme(InternalWindow.Theme.NONACEPT);   // set theme
```

Currently available themes: `JAVAFX` and `NONACEPT`.

---

### Complete Example

```java
InternalWindowInitializer<ExampleController> initializer = ((controller, stage, internalWindow) -> {
            controller.doSomething(stage);
            controller.alsoDoThis(internalWindow);
        });

InternalWindowManager iwm = InternalWindowManager
        .create()
        .managing(contentPanel)
        .withListener(this)
        .withInitializer(initializer)
        .defaultTheme(InternalWindow.Theme.NONACEPT);

iwm.createUniqueInternalWindow("/example.fxml", mainStage, ExampleController.class, customInitConsumer);
iwm.createInternalWindow("/example.fxml", mainStage, customInitConsumer);
```

---

## Screenshots

Click the images to enlarge:

<a href="./screen/iw-move.gif"><img src="./screen/iw-move.gif" width="255" alt="Move"></a>
<a href="./screen/iw-max.gif"><img src="./screen/iw-max.gif" width="255" alt="Maximize"></a>
<a href="./screen/iw-resize.gif"><img src="./screen/iw-resize.gif" width="255" alt="Resize"></a>