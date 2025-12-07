package it.unibo.oop.workers02;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * implementation of a class that sums all of the elements of a matrix using the number of number \
 * of thread passed when created.
 */
public class MultiThreadedSumMatrix implements SumMatrix {
    private final int threads;

    MultiThreadedSumMatrix(final int threads) {
        this.threads = threads;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double sum(final double[][] matrix) {
        final int division = matrix.length % threads + matrix.length / threads;
        final List<Worker> workers = new ArrayList<>();
        for (int start = 0; start < matrix.length; start += division) {
            workers.add(new Worker(matrix, start, division));
        }

        for (final Worker w : workers) {
            w.start();
        }

        double sum = 0;
        for (final Worker w : workers) {
            try {
                w.join();
            } catch (final InterruptedException e) {
                throw new IllegalStateException(e);
            }
            sum = w.getResult();
        }
        return sum;
    }

    private class Worker extends Thread {
        private final double[][] matrix;
        private final int start;
        private final int end;
        private long result;

        Worker(final double[][] m, final int start, final int division) {
           this.matrix = Arrays.copyOf(m, m.length);
           this.start = start;
           this.end = start + division;
        }

        @Override
        public void run() {
            System.out.println("starting from " + start + " to " + end); // NOPMD
            // Println used to show the working ranges for debugging purposes
            for (final double[] array : matrix) {
                for (final double data : array) {
                    result += data;
                }
            }
        }

        private double getResult() {
            return result;
        }
    }
}
