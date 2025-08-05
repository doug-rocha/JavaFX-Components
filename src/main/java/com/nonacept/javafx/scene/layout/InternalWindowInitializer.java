package com.nonacept.javafx.scene.layout;

import java.util.Objects;
import javafx.stage.Stage;

/**
 *
 * @author Douglas Rocha de Oliveira
 */
@FunctionalInterface
public interface InternalWindowInitializer<T extends InternalWindowContent> {

    void accept(T controller, Stage stage, InternalWindow internalWindow);

    default InternalWindowInitializer<T> andThen(InternalWindowInitializer<? super T> after) {
        Objects.requireNonNull(after);
        return (controller, stage, internalWindow) -> {
            accept(controller, stage, internalWindow);
            after.accept(controller, stage, internalWindow);
        };
    }
}
