import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class RPC {

    // MASTER RPC
    public static Message master(Message msg) throws Exception {

        Socket socket = new Socket("localhost", 8080);

        // OUTPUT FIRST (prevents deadlock)
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();

        // SEND REQUEST
        out.writeObject(msg);
        out.flush();

        // INPUT AFTER WRITE
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        Message response = (Message) in.readObject();

        socket.close();
        return response;
    }

    // CHUNKSERVER RPC
    public static Message chunk(String host, int port, Message msg)
            throws Exception {

        Socket socket = new Socket(host, port);

        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();

        out.writeObject(msg);
        out.flush();

        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        Message response = (Message) in.readObject();

        socket.close();
        return response;
    }
}
