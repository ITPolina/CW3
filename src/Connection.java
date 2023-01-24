
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class Connection<T extends Message> implements AutoCloseable {
    private Socket socket;
    private ObjectInputStream input;
    public ObjectOutputStream output;
    private Server server;
    public Client client;
    private String clientName;
    public Connection(Socket socket, String clientName) throws IOException {
        this.socket = socket;
        this.clientName = clientName;
        output = new ObjectOutputStream(this.socket.getOutputStream());
        input = new ObjectInputStream(this.socket.getInputStream());

    }
    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        output = new ObjectOutputStream(this.socket.getOutputStream());
        input = new ObjectInputStream(this.socket.getInputStream());

    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public void sendMessage(T message ) throws IOException {
        message.setDateTime();
        output.writeObject(message);
        output.flush();

    }

    public T readMessage() throws IOException, ClassNotFoundException{
        return (T) input.readObject();
    }

    @Override
    public void close() throws Exception {
        input.close();
        output.close();
        socket.close();
    }

    public String getClientName() {
        return clientName;
    }
}
