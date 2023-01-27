

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private ArrayBlockingQueue<Message> messages = new ArrayBlockingQueue<>(15);
    private CopyOnWriteArrayList<Connection<Message>> clients = new CopyOnWriteArrayList<>();
    private int port;


    public void removeClient(Connection client) {
        clients.remove(client);
    }

    public Server(int port) {
        this.port = port;

    }

    public void run() {
        Socket clientSocket = null;
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Сервер запущен ...");
            new SendMessageToAll().start();
            while (true) {
                clientSocket = serverSocket.accept();
                Connection<Message> connection = new Connection<>(clientSocket);
                connection.setClientName(connection.readMessage().getSender());

                // connection.setClientName(connection.client.getClientName()); // устанавливаем имя прямо при подключении,
                // а не после получения первого сообщения от клиента
                clients.add(connection);

                System.out.println("количество подключенных клиентов: " + clients.size());
                new GetMessage(connection).start();


            }


        } catch (IOException | ClassNotFoundException e /*| ClassNotFoundException | InterruptedException e*/) {
            System.out.println("Обработка IOException или ClassNotFoundException");
        } finally {
            try {
                clientSocket.close();
                serverSocket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

    }

    public class SendMessageToAll extends Thread {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                Message message = null;
                try {
                    message = messages.take();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                for (Connection<Message> client : clients) {
                    try {

                        if (!client.getClientName().equalsIgnoreCase(message.getSender())) {
                            client.sendMessage(message);
                        }
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                        removeClient(client);
                        //clients.remove(client);
                    }

                }
            }
        }
    }

    public class GetMessage extends Thread {
        private Connection<Message> clientConnection;

        public GetMessage(Connection<Message> clientConnection) {
            this.clientConnection = clientConnection;
        }

        @Override
        public void run () {
            while (!Thread.currentThread().isInterrupted()){
                try {
                    //Message message = clientConnection.readMessage();
                    //clientConnection.setClientName(message.getSender());
                    //messages.put(message);
                    messages.put(clientConnection.readMessage());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    removeClient(clientConnection);
                    //throw new RuntimeException(e);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


        public static void main(String[] args) {

            new Server(8090).run();

            //Server server = new Server(Integer.parseInt(args[0]));
        }

    }
