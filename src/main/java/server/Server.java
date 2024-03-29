package server;

import logger.Logger;
import settings.SettingsConfigurator;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.LocalTime;
import java.util.LinkedList;

public class Server extends SettingsConfigurator implements ClientHandlerObserver {
    private static final Logger LOGGER = new Logger("src/main/java/info/ServerInfo.txt");
    public static final LinkedList<ClientHandler> CLIENT_HANDLERS = new LinkedList<>();

    public void launch() {
        try (ServerSocket serverSocket = new ServerSocket(readPort())) {
            System.out.println("Server is running...");
            LOGGER.log("\nServer is running...");
            while (!serverSocket.isClosed()) {
                ClientHandler clientHandler = new ClientHandler(this, serverSocket.accept());
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException exception) {
            System.out.println("Server IOException: " + exception.getMessage());
            LOGGER.log("Server IOException: " + exception.getMessage());
        }
    }

    private String getTime() {
        return LocalTime.now().getHour() + ":" +  LocalTime.now().getMinute() + ":" +  LocalTime.now().getSecond();
    }

    @Override
    public void onMsgReceived(ClientHandler clientHandler, String msg) {
        System.out.println(getTime() + " | " + msg);
        LOGGER.log(getTime() + " | " + msg);
    }

    @Override
    public void onConnection(ClientHandler clientHandler) {
        CLIENT_HANDLERS.add(clientHandler);
        System.out.printf("%s | %s joined! Users online: %d.\n",
                getTime(), clientHandler, CLIENT_HANDLERS.size());
        LOGGER.log(String.format("%s | %s joined! Users online: %d.",
                getTime(), clientHandler, CLIENT_HANDLERS.size()));
    }

    @Override
    public void onDisconnection(ClientHandler clientHandler) {
        CLIENT_HANDLERS.remove(clientHandler);
        System.out.printf("%s | %s left! Users online: %d.\n",
                getTime(), clientHandler, CLIENT_HANDLERS.size());
        LOGGER.log(String.format("%s | %s left! Users online: %d.",
                getTime(), clientHandler, CLIENT_HANDLERS.size()));
    }
}