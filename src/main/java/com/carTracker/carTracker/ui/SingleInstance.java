package com.carTracker.carTracker.ui;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public final class SingleInstance {

    private static final int PORT = 47_391;
    private static volatile Runnable activateHandler = () -> { };

    private SingleInstance() {
    }

    public static boolean acquire() {
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(PORT, 10, InetAddress.getLoopbackAddress());
        } catch (IOException alreadyRunning) {
            try (Socket ignored = new Socket(InetAddress.getLoopbackAddress(), PORT)) {
            } catch (IOException ignored) {
            }
            return false;
        }
        Thread listener = new Thread(() -> {
            while (!serverSocket.isClosed()) {
                try (Socket ignored = serverSocket.accept()) {
                    activateHandler.run();
                } catch (IOException ignored) {
                }
            }
        }, "single-instance");
        listener.setDaemon(true);
        listener.start();
        return true;
    }

    public static void setActivateHandler(Runnable handler) {
        activateHandler = handler;
    }
}
