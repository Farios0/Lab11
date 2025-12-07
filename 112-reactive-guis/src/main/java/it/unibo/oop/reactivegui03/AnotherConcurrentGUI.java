package it.unibo.oop.reactivegui03;

import java.io.Serial;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.oop.JFrameUtil;

/**
 * Third experiment with reactive gui.
 */
public final class AnotherConcurrentGUI extends JFrame {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(AnotherConcurrentGUI.class);
    private final JLabel display = new JLabel();

    /**
     * constructor that sets all the components of the GUI and makes it visible.
     */
    public AnotherConcurrentGUI() {
        super();
        JFrameUtil.dimensionJFrame(this);
        final JPanel canvas = new JPanel();
        this.add(canvas);
        final JButton stop = new JButton("stop");
        final JButton up = new JButton("up");
        final JButton down = new JButton("down");
        canvas.add(display);
        canvas.add(stop);
        canvas.add(up);
        canvas.add(down);
        this.setVisible(true);
        final Agent agent = new Agent();
        final ChronoAgent crono = new ChronoAgent(agent);
        up.addActionListener(e -> agent.countUp());
        down.addActionListener(e -> agent.countDown());
        stop.addActionListener(e -> agent.stopCounting());
        new Thread(agent).start();
        new Thread(crono).start();
    }

    /*
     * The counter agent is implemented as a nested class. This makes it
     * invisible outside and encapsulated.
     */
    private final class Agent implements Runnable {
        /*
         * Stop is volatile to ensure visibility. Look at:
         *
         * http://archive.is/9PU5N - Sections 17.3 and 17.4
         *
         * For more details on how to use volatile:
         *
         * http://archive.is/4lsKW
         *
         */
        private volatile boolean stop;
        private volatile boolean positive = true;
        private int counter;

        @Override
        public synchronized void run() {
            while (!stop) {
                if (positive) {
                    positiveCount();
                } else {
                    negativeCount();
                }
            }
        }

        private void negativeCount() {
            while (!this.stop && !this.positive) {
                try {
                    // The EDT doesn't access `counter` anymore, it doesn't need to be volatile
                    final var nextText = Integer.toString(this.counter);
                    SwingUtilities.invokeAndWait(() -> AnotherConcurrentGUI.this.display.setText(nextText));
                    this.counter--;
                    Thread.sleep(100);
                } catch (InvocationTargetException | InterruptedException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            }
        }

        private void positiveCount() {
            while (!this.stop && this.positive) {
                try {
                    // The EDT doesn't access `counter` anymore, it doesn't need to be volatile
                    final var nextText = Integer.toString(this.counter);
                    SwingUtilities.invokeAndWait(() -> AnotherConcurrentGUI.this.display.setText(nextText));
                    this.counter++;
                    Thread.sleep(100);
                } catch (InvocationTargetException | InterruptedException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            }
        }

        /**
         * External command to stop counting.
         */
        public void stopCounting() {
            this.stop = true;
        }

        /**
         * External command to change counting mode.
         */
        public void countUp() {
            this.positive = true;
        }

        public void countDown() {
            this.positive = false;
        }
    }

    private final class ChronoAgent implements Runnable {
        private static final int TIME_SEC = 10;
        private Agent agentTocontrol;

        ChronoAgent(Agent a){
            agentTocontrol = a;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(TIME_SEC * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            agentTocontrol.stopCounting();
        }

    }
}
