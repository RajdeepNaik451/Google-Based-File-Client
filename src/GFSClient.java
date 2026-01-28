import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class GFSClient {

    public static void main(String[] args) {

        try {
            //CREATE FILE
            Message createReq = new Message();
            createReq.type = RequestType.CREATE_FILE;
            createReq.fileName = "test.txt";

            Message createRes = talkToMaster(createReq);
            System.out.println("File created, chunks: " + createRes.chunkList);

            //GET CHUNKS
            Message getReq = new Message();
            getReq.type = RequestType.GET_CHUNKS;
            getReq.fileName = "test.txt";

            Message getRes = talkToMaster(getReq);
            List<String> chunks = getRes.chunkList;
            System.out.println("Chunks from master: " + chunks);

            String chunkId = chunks.get(0); // first chunk

            //WRITE CHUNK (to ChunkServer)
            Message writeReq = new Message();
            writeReq.type = RequestType.WRITE_CHUNK;
            writeReq.chunkId = chunkId;
            writeReq.data = "Hello GFS".getBytes();

            talkToChunkServer("localhost", 6001, writeReq);
            System.out.println("Data written to chunk: " + chunkId);

            // READ CHUNK
            Message readReq = new Message();
            readReq.type = RequestType.READ_CHUNK;
            readReq.chunkId = chunkId;

            Message readRes = talkToChunkServer("localhost", 6001, readReq);
            System.out.println("Read data: " + new String(readRes.data));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Master communication
    private static Message talkToMaster(Message msg) throws Exception {
        Socket socket = new Socket("localhost", 8080);

        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

        out.writeObject(msg);
        out.flush();

        Message response = (Message) in.readObject();
        socket.close();

        return response;
    }

    //ChunkServer communication
    private static Message talkToChunkServer(String host, int port, Message msg)
            throws Exception {

        Socket socket = new Socket(host, port);

        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

        out.writeObject(msg);
        out.flush();

        Message response = (Message) in.readObject();
        socket.close();

        return response;
    }
}
