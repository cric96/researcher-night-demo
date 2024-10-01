package it.unibo.artificial_vision_tracking.aruco_markers;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.security.Key;

public class MyKeyListener implements KeyListener {
    
    private boolean running = true;
    public MyKeyListener(boolean running) {
        super();
        this.running = running;
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public void keyPressed(final KeyEvent e) {
        check(e);
    }
    
    public void keyTyped(final KeyEvent e) {
        check(e);
    }
    
    public void keyReleased(final KeyEvent e) {
        check(e);
    }

    private boolean check(final KeyEvent e) {
        return this.running = e.getKeyCode() == KeyEvent.VK_Q ? false : this.running;
    }
}
