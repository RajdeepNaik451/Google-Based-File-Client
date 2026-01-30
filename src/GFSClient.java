import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GFSClient {

    private static final int CHUNK_SIZE = 64 * 1024; // 64 KB

    public static void main(String[] args) {

        try {
            Thread.sleep(2000); // wait for chunkservers

            // ================= GFS PATH =================
            String gfsPath = "/docs/The Compound Effect.pdf";

            // ================= CREATE DIRECTORY =================
            Message mkdir = new Message();
            mkdir.type = RequestType.CREATE_DIRECTORY;
            mkdir.fileName = "/docs";
            RPC.masterOneWay(mkdir);

            // ================= READ LOCAL FILE =================
            Path localFile = Path.of(
                    "C:/Users/rajde/Downloads/The Compound Effect.pdf"
            );
            byte[] fileData = Files.readAllBytes(localFile);

            // ================= CREATE FILE =================
            Message create = new Message();
            create.type = RequestType.CREATE_FILE;
            create.fileName = gfsPath;
            create.fileSize = fileData.length;
            create.fileType = "pdf";

            Message createRes = RPC.master(create);

            List<String> chunkIds;

            // File already exists → fetch metadata
            if (createRes.chunkList == null) {
                System.out.println("File already exists. Fetching metadata...");

                Message get = new Message();
                get.type = RequestType.GET_CHUNKS;
                get.fileName = gfsPath;

                Message getRes = RPC.master(get);
                chunkIds = getRes.chunkList;

            } else {
                chunkIds = createRes.chunkList;
            }

            // ================= SPLIT FILE =================
            List<byte[]> dataChunks = splitFile(fileData);

            // ================= GET REPLICA LOCATIONS ONCE =================
            Message meta = new Message();
            meta.type = RequestType.GET_CHUNKS;
            meta.fileName = gfsPath;

            Message info = RPC.master(meta);
            List<String> replicas = info.chunkServerList;

            // ================= WRITE EACH CHUNK =================
            for (int i = 0; i < chunkIds.size(); i++) {

                Message write = new Message();
                write.type = RequestType.WRITE_CHUNK;
                write.chunkId = chunkIds.get(i);
                write.data = dataChunks.get(i);

                for (String server : replicas) {
                    String[] p = server.split(":");
                    RPC.chunk(p[0], Integer.parseInt(p[1]), write);
                }
            }

            System.out.println("File written successfully");

            // ================= READ BACK FILE =================
            List<byte[]> downloaded = new ArrayList<>();

            for (String chunkId : chunkIds) {

                Message read = new Message();
                read.type = RequestType.READ_CHUNK;
                read.chunkId = chunkId;

                // Read from first available replica
                String[] p = replicas.get(0).split(":");
                Message readRes =
                        RPC.chunk(p[0], Integer.parseInt(p[1]), read);

                downloaded.add(readRes.data);
            }

            // ================= MERGE & SAVE =================
            int total = downloaded.stream().mapToInt(b -> b.length).sum();
            byte[] finalData = new byte[total];

            int pos = 0;
            for (byte[] part : downloaded) {
                System.arraycopy(part, 0, finalData, pos, part.length);
                pos += part.length;
            }

            Files.write(
                    Path.of("downloaded_The_Compound_Effect.pdf"),
                    finalData
            );

            System.out.println("File retrieved successfully");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= FILE CHUNKING =================
    private static List<byte[]> splitFile(byte[] data) {

        List<byte[]> chunks = new ArrayList<>();

        for (int i = 0; i < data.length; i += CHUNK_SIZE) {
            int end = Math.min(data.length, i + CHUNK_SIZE);
            byte[] part = new byte[end - i];
            System.arraycopy(data, i, part, 0, part.length);
            chunks.add(part);
        }

        return chunks;
    }
}
