package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    private static final String ADDRESS = "127.0.0.1";
    private static final int PORT = 23456;

    private ServerSocket server;

    private ExecutorService executorService;
    private final FileHandler fileHandler;

    Main() {
        fileHandler = new FileHandler();
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    void run() {
        try {
            server = new ServerSocket(PORT, 50, InetAddress.getByName(ADDRESS));
            System.out.println("Server started!");
            while (!getServer().isClosed()) {
                // TODO pass server or client?
                executorService.submit(new RequestHandler(server, fileHandler, this));
            }
            executorService.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized ServerSocket getServer() {
        return server;
    }

    public static void main(String[] args) {
        Main main = new Main();
        main.run();
    }
}
