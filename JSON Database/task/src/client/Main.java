package client;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final String ADDRESS = "127.0.0.1";
    private static final int PORT = 23456;

    @Parameter(names = {"--type", "-t"}, description = "Request type")
    private String type;

    @Parameter(names = {"--key", "-k"}, description = "Key to fetch")
    private String key;

    @Parameter(names = {"--value", "-v"}, description = "Message to be send")
    private String value;

    @Parameter(names = {"--input", "-in"}, description = "Filename to read request from")
    private String filename;

    void run() {
        try (Socket socket = new Socket(InetAddress.getByName(ADDRESS), PORT)) {
            System.out.println("Client started!");
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());

            Gson gson = new Gson();
            String request;

            if (filename == null) {
                request = gson.toJson(this);
            } else {
                File file = new File(System.getProperty("user.dir") + "/src/client/data/" + filename);
                // File file = new File(System.getProperty("user.dir") + "/JSON Database/task/src/client/data/" + filename);
                try (Scanner scanner = new Scanner(file)){
                    request = scanner.nextLine();
                } catch (IOException e) {
                    e.printStackTrace();
                    request = "{type=\"exit\"}";
                }
            }

            output.writeUTF(request);
            System.out.printf("Sent: %s%n", request);

            String answer = input.readUTF();
            System.out.printf("Received: %s%n", answer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Main main = new Main();
        JCommander.newBuilder()
                .addObject(main)
                .build()
                .parse(args);
        try {
            Thread.sleep(50L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        main.run();
    }

    private static class Converter implements IStringConverter<List<String>> {
        @Override
        public List<String> convert(String value) {
            return new ArrayList<>(Arrays.asList(value.substring(1, value.length() - 1).split(",")));
        }
    }
}
