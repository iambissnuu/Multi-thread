import java.util.concurrent.*;
import java.util.*;
import java.io.*;

class Task {
    private final int id;

    public Task(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String run() {
        try {
            Thread.sleep(100); // Simulate work delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Task " + id + " interrupted during processing.";
        }
        return "Task " + id + " processed successfully.";
    }
}

class Worker implements Runnable {
    private final int workerId;
    private final BlockingQueue<Task> queue;
    private final List<String> results;

    public Worker(int workerId, BlockingQueue<Task> queue, List<String> results) {
        this.workerId = workerId;
        this.queue = queue;
        this.results = results;
    }

    @Override
    public void run() {
        System.out.println("Worker " + workerId + " started.");
        try {
            while (true) {
                Task task = queue.poll(1, TimeUnit.SECONDS);
                if (task == null) break;
                String output = task.run();
                synchronized (results) {
                    results.add(output);
                }
            }
        } catch (Exception ex) {
            System.err.println("Worker " + workerId + " encountered an error: " + ex.getMessage());
        }
        System.out.println("Worker " + workerId + " completed.");
    }
}

public class DataProcessingSystem {
    public static void main(String[] args) {
        BlockingQueue<Task> taskQueue = new LinkedBlockingQueue<>();
        List<String> resultList = Collections.synchronizedList(new ArrayList<>());

        for (int i = 1; i <= 10; i++) {
            taskQueue.add(new Task(i));
        }

        ExecutorService pool = Executors.newFixedThreadPool(3);

        for (int i = 1; i <= 3; i++) {
            pool.submit(new Worker(i, taskQueue, resultList));
        }

        pool.shutdown();
        try {
            pool.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.err.println("Thread pool interrupted while waiting.");
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("results_java.txt"))) {
            for (String line : resultList) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }

        System.out.println("Processing complete. Output written to results_java.txt");
    }
}
