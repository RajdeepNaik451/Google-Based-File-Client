import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class GFSClient {

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 8080);

            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            Message msg = new Message();
            msg.type = RequestType.WRITE_CHUNK;
            msg.fileName = "chunk_1";
            msg.data = "Hello GFS".getBytes();

            out.writeObject(msg);
            out.flush();

            Object response = in.readObject();
            System.out.println("Response from master: " + response);

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
