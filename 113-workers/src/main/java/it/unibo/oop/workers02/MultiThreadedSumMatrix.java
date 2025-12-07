package it.unibo.oop.workers02;

import java.util.ArrayList;
import java.util.List;

public class MultiThreadedSumMatrix implements SumMatrix {
    private final int threads;

    MultiThreadedSumMatrix(int threads) {
        this.threads = threads;
    }

    @Override
    public double sum(double[][] matrix) {
        final int division = matrix.length % threads + matrix.length / threads;
        final List<Worker> workers = new ArrayList<>();
        for(int start = 0; start < matrix.length; start += division) {
            workers.add(new Worker(matrix, start, division));
        }

        for(Worker w : workers) {
            w.start();
        }

        double sum = 0;
        for(Worker w : workers) {
            try {
                w.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
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

        Worker(double[][] m, int start, int division) {
           matrix = m;
           this.start = start;
           this.end = start + division;
        }

        @Override
        public void run() {
            System.out.println("starting from " + start + " to " + end);
            for (int i = 0; i < matrix.length; i++) {
                for (int j = 0; j < matrix[i].length; j++) {
                    result += matrix[i][j];
                }
            }
        }

        private double getResult() {
            return result;
        }
    }
}
