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
package com.nonacept.javafx.scene.layout;

import java.util.Objects;
import javafx.stage.Stage;

/**
 * Functional interface used to initialize an {@link InternalWindow} after its
 * FXML controller has been loaded.
 *
 * <p>
 * This initializer provides access to:
 * <ul>
 * <li>The controller associated with the window</li>
 * <li>The owner {@link Stage}</li>
 * <li>The created {@link InternalWindow} instance</li>
 * </ul>
 *
 * <p>
 * It allows custom configuration logic to be applied right after the internal
 * window is created and before it is shown.
 *
 * <p><b>Typical Usage</b>
 * <pre>{@code
 * InternalWindowManager.create()
 *     .withInitializer((controller, stage, window) -> {
 *         controller.loadData();
 *         window.setTitle("User Form");
 *     });
 * }</pre>
 *
 * <p><b>Chaining Initializers</b><br>
 * You can combine initializers using
 * {@link #andThen(InternalWindowInitializer)}:
 *
 * <pre>{@code
 * InternalWindowInitializer<MyController> initA = (c, s, w) -> c.setup();
 * InternalWindowInitializer<MyController> initB = (c, s, w) -> w.centerOnScreen();
 *
 * InternalWindowInitializer<MyController> combined = initA.andThen(initB);
 * }</pre>
 *
 * @param <T> the type of the controller for this internal window
 *
 * @see InternalWindow
 * @see com.nonacept.javafx.scene.manager.InternalWindowManager
 *
 * @author Douglas Rocha de Oliveira
 */
@FunctionalInterface
public interface InternalWindowInitializer<T extends InternalWindowContent> {

    /**
     * Performs initialization logic for an {@link InternalWindow}.
     *
     * @param controller the controller instance associated with the window
     * @param stage the owning {@link Stage}
     * @param internalWindow the window being initialized
     */
    void accept(T controller, Stage stage, InternalWindow internalWindow);

    /**
     * Returns a composed initializer that executes this initializer first,
     * followed by the {@code after} initializer.
     *
     * <p>
     * This allows multiple configurations to be chained in a clean, declarative
     * way.
     *
     * @param after the initializer to run after this one
     * @return a composed initializer that performs both initializations
     * @throws NullPointerException if {@code after} is {@code null}
     */
    default InternalWindowInitializer<T> andThen(InternalWindowInitializer<? super T> after) {
        Objects.requireNonNull(after);
        return (controller, stage, internalWindow) -> {
            accept(controller, stage, internalWindow);
            after.accept(controller, stage, internalWindow);
        };
    }
}
