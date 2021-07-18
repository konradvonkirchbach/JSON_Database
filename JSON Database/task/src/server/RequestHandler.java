package server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class RequestHandler implements Runnable {

    private Socket client;
    private final FileHandler fileHandler;
    private final Main main;

    public RequestHandler(ServerSocket server, FileHandler fileHandler, Main main) throws IOException {
        try {
            this.client = server.accept();
        } catch (SocketException e) {
            this.client = null;
        }
        this.fileHandler = fileHandler;
        this.main = main;
    }

    @Override
    public void run() {
        if (client == null) {
            System.out.println("Client stopped");
            return;
        }
        Gson gson = new Gson();
        try (DataInputStream input = new DataInputStream(client.getInputStream());
             DataOutputStream output = new DataOutputStream(client.getOutputStream())) {
                String requestJson = input.readUTF();
                System.out.printf("Received: %s%n", requestJson);
                Request request;
                try {
                    request = gson.fromJson(requestJson, Request.class);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                    return;
                }

            switch (request.getType()) {
                case "set": {
                    Response response = fileHandler.writeToDatabase(request.getKeys(), request.getValue());
                    String responseString = gson.toJson(response);
                    System.out.printf("Sent: %s%n", responseString);
                    output.writeUTF(responseString);
                    break;
                }
                case "get": {
                    Response response = fileHandler.readFromKey(request.getKeys());
                    String responseString = gson.toJson(response);
                    System.out.printf("Sent: %s%n", responseString);
                    output.writeUTF(responseString);
                    break;
                }
                case "delete": {
                    Response response = fileHandler.deleteFromDataBase(request.getKeys());
                    String responseString = gson.toJson(response);
                    System.out.printf("Sent: %s%n", responseString);
                    output.writeUTF(responseString);
                    break;
                }
                case "exit": {
                    String response = gson.toJson(Response.builder().response("OK").build());
                    output.writeUTF(response);
                    main.getServer().close();
                    break;
                }
                default:
                    throw new IllegalArgumentException("No action for " + request.getType());
            }
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
