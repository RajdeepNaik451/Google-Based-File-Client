import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class GFSClient {

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 8080);

            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            Message msg = new Message();
            msg.type = RequestType.GET_CHUNKS;
            msg.fileName = "test.txt";
            msg.data = "Hello GFS".getBytes();

            out.writeObject(msg);
            out.flush();

            Object obj = in.readObject();

            if (!(obj instanceof Message)) {
                throw new RuntimeException(
                        "Protocol violation: expected Message, got " + obj.getClass()
                );
            }

            Message response = (Message) obj;

            System.out.println("Response type: " + response.type);
            System.out.println("File: " + response.fileName);
            System.out.println("Chunks: " + response.chunkList);


            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
