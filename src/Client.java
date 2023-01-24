import jdk.jshell.spi.SPIResolutionException;
//import ru.polina.lessons.ArgsAndProps;
//import ru.polina.lessons.les21.common.Connection;
//import ru.polina.lessons.les21.common.Message;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Properties;
import java.util.Scanner;

public class Client {
    private final String ip;
    private final int port;

    private Connection<Message> connection;
    private final String clientName;

    public Client(String ip, int port) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите имя");
        this.clientName = scanner.nextLine();
        this.ip = ip;
        this.port = port;
    }


    public String getClientName() {
        return clientName;
    }

    public void run() throws IOException {

        Socket clientSocket = new Socket(ip, port);
        connection = new Connection<>(clientSocket, clientName);

        new SendMessage().start();
        new ReadMessage().start();
    }

    public class ReadMessage extends Thread {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    System.out.println(connection.readMessage());
                } catch (IOException e) {
                    System.out.println("Попробуйте перезапустить приложение");
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public class SendMessage extends Thread {
        @Override
        public void run() {
            Scanner scanner = new Scanner(System.in);
            while (!Thread.currentThread().isInterrupted()) {
                System.out.println("Введите сообщение");
                String text = scanner.nextLine();
                Message message = new Message(clientName, text);
                try {
                    connection.sendMessage(message);
                } catch (IOException e) {
                    System.out.println("Попробуйте перезапустить приложение");
                }

            }
        }

    }

        public static void main(String[] args) {
            //new Client("127.0.0.1", 8090).run();

            Properties properties = new Properties();
            try (InputStream input = Client.class.getClassLoader()
                    .getResourceAsStream("config.properties")) {
                properties.load(input);
            } catch (IOException e) {
                System.out.println("Возникли проблемы с чтением config.properties ");
            }
            Client client = new Client(properties.getProperty("ip"), Integer.parseInt(properties.getProperty("port")));
            try {
                client.run();

            } catch (IOException e) {
                System.out.println("Попробуйте перезапустить приложение");
            }


        }

    }
