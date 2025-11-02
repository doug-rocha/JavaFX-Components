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
package com.nonacept.javafx.listeners;

/**
 * Listener used to receive notifications when a child window or child
 * controller is opened or closed.
 *
 * <p>
 * This interface is typically implemented by a parent controller that needs to
 * react when a child UI element (such as an {@code InternalWindow}) is shown or
 * closed.
 *
 * <p>
 * Common use cases include:
 * <ul>
 * <li>Disabling parent controls while a child window is open</li>
 * <li>Refreshing data when a child window closes</li>
 * <li>Tracking active internal windows</li>
 * </ul>
 *
 * <p>
 * Example:
 * <pre>{@code
 * public class MainController implements ChildListener {
 *     @Override
 *     public void onChildOpen(Object child) {
 *         System.out.println("Child window opened: " + child);
 *     }
 *
 *     @Override
 *     public void onChildClose(Object child) {
 *         System.out.println("Child window closed: " + child);
 *         refreshData();
 *     }
 * }
 * }</pre>
 *
 * @see com.nonacept.javafx.scene.layout.InternalWindow
 *
 * @author Douglas Rocha de Oliveira
 */
public interface ChildListener {

    /**
     * Called when a child view or child window is opened.
     *
     * @param obj the child object associated with the opening event
     */
    void onChildOpen(Object obj);

    /**
     * Called when a child view or child window is closed.
     *
     * @param obj the child object associated with the closing event
     */
    void onChildClose(Object obj);
}
