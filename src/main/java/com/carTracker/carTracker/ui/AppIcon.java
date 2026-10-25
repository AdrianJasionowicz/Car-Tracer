package com.carTracker.carTracker.ui;

import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public final class AppIcon {

    private static final int BACKGROUND = 0x1E88E5;
    private static final int WHEELS = 0x263238;

    private AppIcon() {
    }

    public static java.awt.Image awtImage(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.scale(size / 32.0, size / 32.0);
        g.setColor(new java.awt.Color(BACKGROUND));
        g.fillRoundRect(0, 0, 32, 32, 8, 8);
        g.setColor(java.awt.Color.WHITE);
        g.fillRoundRect(9, 8, 14, 9, 4, 4);
        g.fillRoundRect(3, 14, 26, 8, 4, 4);
        g.setColor(new java.awt.Color(WHEELS));
        g.fillOval(7, 19, 7, 7);
        g.fillOval(18, 19, 7, 7);
        g.dispose();
        return image;
    }

    public static javafx.scene.image.Image fxImage() {
        Canvas canvas = new Canvas(32, 32);
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(javafx.scene.paint.Color.web("#1E88E5"));
        g.fillRoundRect(0, 0, 32, 32, 8, 8);
        g.setFill(javafx.scene.paint.Color.WHITE);
        g.fillRoundRect(9, 8, 14, 9, 4, 4);
        g.fillRoundRect(3, 14, 26, 8, 4, 4);
        g.setFill(javafx.scene.paint.Color.web("#263238"));
        g.fillOval(7, 19, 7, 7);
        g.fillOval(18, 19, 7, 7);
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(javafx.scene.paint.Color.TRANSPARENT);
        return canvas.snapshot(parameters, null);
    }
}
