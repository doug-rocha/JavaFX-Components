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

/**
 * Interface for controllers used inside an {@link InternalWindow}.
 *
 * <p>
 * When a window is created with a controller, the window will call
 * {@link #setInternalWindow(InternalWindow)} to provide a reference back to
 * itself. This allows the content controller to interact with the window (e.g.,
 * close it, change title, etc.).
 *
 * <p>
 * Typical usage:
 * <pre>{@code
 * public class SettingsController implements InternalWindowContent {
 *     private InternalWindow window;
 *
 *     @Override
 *     public void setInternalWindow(InternalWindow iw) {
 *         this.window = iw;
 *     }
 *
 *     @Override
 *     public boolean canClose() {
 *         return true; // or validate state before closing
 *     }
 * }
 * }</pre>
 *
 * @see InternalWindow
 * @author Douglas Rocha de Oliveira
 */
public interface InternalWindowContent {

    /**
     * Provides the {@link InternalWindow} instance that hosts this content.
     *
     * <p>
     * This method is called automatically when the window is created or
     * initialized, allowing the content controller to interact with its parent
     * window.
     *
     * @param iw the parent {@code InternalWindow}
     */
    void setInternalWindow(InternalWindow iw);

    /**
     * Called before the window closes to determine whether the content allows
     * the close operation.
     *
     * <p>
     * This can be used to prevent closing while data is unsaved or a process is
     * running.
     *
     * @return {@code true} if the window may close, {@code false} to cancel
     */
    boolean canClose();

}
