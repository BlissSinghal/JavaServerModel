package org.cis1200;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

/**
 * The {@code ServerBackend} handles networking and communication with clients,
 * and is responsible for managing the server state and executing tasks. To
 * run the server, you should see the {@link ServerMain} class.
 *
 * You do not need to modify this file.
 */
final class ServerBackend implements Runnable {

    // The ServerModel is NOT thread-safe; it should only be touched by the
    // model thread after being initialized.
    private final ServerModel model;

    private final BlockingQueue<Task> taskQueue;

    private volatile ServerSocket serverSocket;
    private final Map<Integer, Socket> openSockets;

    private volatile boolean running;
    private volatile Thread modelThread;

    /**
     * Create the backend
     * 
     * @param model the state of the server
     */
    public ServerBackend(ServerModel model) {
        if (model == null) {
            throw new NullPointerException();
        }
        this.model = model;
        taskQueue = new LinkedBlockingQueue<>();
        serverSocket = null;
        openSockets = Collections.synchronizedMap(new HashMap<>());
        running = false;
        modelThread = null;
    }

    /** @return whether the server is processing messages */
    public boolean isRunning() {
        return running;
    }

    @Override
    public void run() {
        running = true;

        // Attempt to open the ServerSocket; abort on failure
        try {
            serverSocket = new ServerSocket(21212);
        } catch (IOException iox) {
            iox.printStackTrace();
            running = false;
            serverSocket = null;
        }

        // Start the model thread
        modelThread = new Thread(() -> {
            while (running || !taskQueue.isEmpty()) {
                Task task;
                try {
                    task = taskQueue.take();
                } catch (InterruptedException ix) {
                    continue;
                }
                try {
                    dispatchMessages(task.doTask());
                } catch (RuntimeException rx) {
                    rx.printStackTrace();
                }
            }

            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }
            } catch (IOException iox) {
                iox.printStackTrace();
            }
        }, "Model thread");
        modelThread.start();

        // Await new connections on the current thread
        ExecutorService workerPool = Executors.newCachedThreadPool();
        try {
            int nextId = 0;
            while (running && !serverSocket.isClosed()) {
                int userId = nextId++;
                Socket clientSocket = serverSocket.accept();
                openSockets.put(userId, clientSocket);
                taskQueue.add(new Registration(userId));
                workerPool.execute(new ConnectionWorker(userId, clientSocket));
            }
        } catch (IOException iox) {
            iox.printStackTrace();
        } finally {
            running = false;
            workerPool.shutdown();
            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }
            } catch (IOException iox) {
                iox.printStackTrace();
            } finally {
                serverSocket = null;
            }

            synchronized (openSockets) {
                Iterator<Socket> iterator = openSockets.values().iterator();
                while (iterator.hasNext()) {
                    Socket clientSocket = iterator.next();
                    try {
                        clientSocket.close();
                    } catch (IOException iox) {
                        iox.printStackTrace();
                    } finally {
                        iterator.remove();
                    }
                }
            }
        }
    }

    /** Ends the execution of the server */
    public void stop() {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException iox) {
                iox.printStackTrace();
            }
        }
        if (modelThread != null) {
            modelThread.interrupt();
        }
    }

    // ==========================================================================
    // ResponseSet dispatch
    // ==========================================================================

    private void dispatchMessages(ResponseSet responseSet) {
        if (responseSet == null) {
            return;
        }

        Set<Response> responses = responseSet.getResponses();
        for (Response response : responses) {
            try {
                Integer userId = response.getRecipientId();
                Socket clientSocket = openSockets.get(userId);
                PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());
                pw.println(response.getText());
                pw.flush();
                System.out.printf(
                        "Response sent to user %d: \"%s\"\n",
                        userId, response.getText()
                );
                pw.flush();
            } catch (IOException iox) {
                iox.printStackTrace();
            }
        }
    }

    // ==========================================================================
    // ConnectionWorker
    // ==========================================================================

    private final class ConnectionWorker implements Runnable {
        private final int userId;
        private final Socket clientSocket;

        public ConnectionWorker(int userId, Socket clientSocket) {
            this.userId = userId;
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(clientSocket.getInputStream())
                    )) {
                while (running && !clientSocket.isClosed()) {
                    String line = reader.readLine();
                    if (line != null) {
                        System.out.printf(
                                "Request received from user %d: " +
                                        "\"%s\"\n",
                                userId, line
                        );
                        Request request = new Request(userId, line);
                        taskQueue.add(request);
                    } else {
                        clientSocket.close();
                        taskQueue.add(new Disconnection(userId));
                    }
                }
            } catch (IOException iox) {
                iox.printStackTrace();
                taskQueue.add(new Disconnection(userId));
            } finally {
                openSockets.remove(userId);
            }
        }
    }

    // ==========================================================================
    // Tasks
    // ==========================================================================

    /**
     * A task does some work and then returns a set of responses for
     * the server to relay to clients
     */
    private interface Task {
        ResponseSet doTask();
    }

    /**
     * Represents a client's connection to the server.
     */
    private final class Registration implements Task {
        private final int userId;

        public Registration(int userId) {
            this.userId = userId;
        }

        @Override
        public ResponseSet doTask() {
            return model.registerUser(userId);
        }
    }

    /**
     * Represents a client's disconnection from the server.
     */
    private final class Disconnection implements Task {
        private final int userId;

        public Disconnection(int userId) {
            this.userId = userId;
        }

        @Override
        public ResponseSet doTask() {
            return model.deregisterUser(userId);
        }
    }

    /**
     * Represents an incoming command from a connected client.
     */
    private final class Request implements Task {
        private final int userId;
        private final String payload;

        public Request(int userId, String payload) {
            this.userId = userId;
            this.payload = payload;
        }

        @Override
        public ResponseSet doTask() {
            Command command = CommandParser.parse(userId, payload);
            assert command != null;
            return command.updateServerModel(model);
        }
    }
}
