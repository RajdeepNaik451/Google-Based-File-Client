import java.util.List;

public class GFSClient {

    public static void main(String[] args) {
        try {
            Thread.sleep(2000); // wait for ChunkServer registration

            System.out.println("Sending CREATE_FILE request");

            Message createReq = new Message();
            createReq.type = RequestType.CREATE_FILE;
            createReq.fileName = "test.txt";

            Message createRes = RPC.master(createReq);
            System.out.println("CREATE_FILE response: " + createRes.chunkList);

            Message getReq = new Message();
            getReq.type = RequestType.GET_CHUNKS;
            getReq.fileName = "test.txt";

            Message getRes = RPC.master(getReq);
            System.out.println("GET_CHUNKS response: " + getRes.chunkList);

            String chunkId = getRes.chunkList.get(0);

            Message writeReq = new Message();
            writeReq.type = RequestType.WRITE_CHUNK;
            writeReq.chunkId = chunkId;
            writeReq.data = "Hello GFS".getBytes();

            RPC.chunk("localhost", 6001, writeReq);
            System.out.println("WRITE complete");

            Message readReq = new Message();
            readReq.type = RequestType.READ_CHUNK;
            readReq.chunkId = chunkId;

            Message readRes = RPC.chunk("localhost", 6001, readReq);
            System.out.println("READ data: " + new String(readRes.data));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
