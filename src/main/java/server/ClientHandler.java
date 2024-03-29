package server;

import disconnection.Disconnection;
import logger.Logger;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable, Disconnection {
    private final Logger logger;
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private ClientHandlerObserver observer;
    private String clientUsername;

    public ClientHandler(ClientHandlerObserver observer, Socket socket) {
        this.logger = new Logger("src/main/java/info/ChatHistory.txt");
        try {
            this.socket = socket;
            this.observer = observer;
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            clientUsername = setClientUsername();
            distribute("Server: " + clientUsername + " joined!");
            observer.onConnection(this);
        } catch (IOException exception) {
            disconnect(socket, reader, writer);
        }
    }

    @Override
    public void run() {
        while (socket.isConnected()) {
            try {
                String messageFromClient = reader.readLine();
                if (messageFromClient.equals(EXIT)) {
                    disconnectClientHandler();
                    break;
                } else {
                    observer.onMsgReceived(this, messageFromClient);
                    distribute(messageFromClient);
                }
            } catch (IOException e) {
                disconnect(socket, reader, writer);
                break;
            }
        }
    }

    @Override
    public String toString() {
        return "Port: " + socket.getPort() + " | " + clientUsername;
    }

    private String setClientUsername() {
        try {
            return reader.readLine();
        } catch (IOException exception) {
            disconnect(socket, reader, writer);
        }
        return null;
    }

    private void sendString(String message) {
        try {
            writer.write(message);
            writer.newLine();
            writer.flush();
        } catch (IOException exception) {
            disconnect(socket, reader, writer);
        }
    }

    private void distribute(String message) {
        logger.log(message);
        for (ClientHandler clientHandler : Server.CLIENT_HANDLERS) {
            if (!clientHandler.clientUsername.equals(clientUsername)) {
                clientHandler.sendString(message);
            }
        }
    }

    private void disconnectClientHandler() {
        observer.onDisconnection(this);
        distribute("Server: " + clientUsername + " left!");
        disconnect(socket, reader, writer);
    }
}