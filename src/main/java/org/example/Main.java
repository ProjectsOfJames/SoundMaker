package org.example;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Main extends JFrame implements MouseListener, MouseMotionListener {
    JPanel panel;
    JLabel lbl;

    int frequency = 44100;
    AudioFormat af = new AudioFormat((float) frequency, 16, 1, true, false);
    SourceDataLine sdl = AudioSystem.getSourceDataLine(af);

    public Main() throws LineUnavailableException {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel = new JPanel();
        lbl = new JLabel();

        addMouseListener(this);
        addMouseMotionListener(this);

        setLayout(new BorderLayout());
        setSize(500, 500);
        add(lbl, BorderLayout.NORTH);
        add(panel, BorderLayout.CENTER);

        sdl.open();
        setVisible(true);
    }

    public static void main(String[] args) throws LineUnavailableException {
        new Main();
    }

    // mouseListener
    @Override
    public void mouseClicked(MouseEvent  e) {
        if (e.getClickCount() == 2) {
            lbl.setText("Cleared dots");
            panel.repaint();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        lbl.setText("Drawing started at (" + e.getX() + "," + e.getY() + ")");

        sdl.start();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        lbl.setText("Drawing stopped");

        sdl.drain();
        sdl.stop();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        panel.setBackground(Color.LIGHT_GRAY);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        panel.setBackground(Color.WHITE);
    }

    public void drawDot(Graphics g, int X, int Y) {
        g.setColor(Color.cyan);
        g.fillOval(X, Y, 5, 5);
    }

    // mouseMotion
    @Override
    public void mouseDragged(MouseEvent e) {
        lbl.setText("Dragging");
        drawDot(panel.getGraphics(), e.getX()-10, e.getY()-50);

        byte[] buf = new byte[2];
        int durationMs = 50;
        int numberOfTimesFullSinFuncPerSec = Math.max((e.getX() * e.getY() / 100), 1);

        int totalSamples = (int)(durationMs * (float)frequency / 1000);

        int fadeSamples = Math.min(200, Math.max(1, totalSamples / 10));

        for (int i = 0; i < totalSamples; i++) {
            float numberOfSamplesToRepresentFullSin = (float) frequency / numberOfTimesFullSinFuncPerSec;
            double angle = i / (numberOfSamplesToRepresentFullSin / 2.0) * Math.PI;
            short a = (short) (Math.sin(angle) * 32767);

            // fade-in/out
            double env = 1.0;
            if (i < fadeSamples) env = (double) i / fadeSamples;
            else if (i > totalSamples - fadeSamples - 1) env = (double) (totalSamples - 1 - i) / fadeSamples;

            a = (short) (a * env);

            buf[0] = (byte) (a & 0xFF);
            buf[1] = (byte) (a >> 8);
            sdl.write(buf, 0, 2);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        lbl.setText("Position: (" + e.getX() + "," + e.getY() + ")");
    }
}