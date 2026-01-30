import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileUploader {

    private static final int CHUNK_SIZE = 64 * 1024;

    public static void upload(Path localFile, String gfsPath) throws Exception {

        byte[] fileData = Files.readAllBytes(localFile);

        Message create = new Message();
        create.type = RequestType.CREATE_FILE;
        create.fileName = gfsPath;
        create.fileSize = fileData.length;
        create.fileType = getExtension(localFile);

        Message res = RPC.master(create);

        List<String> chunkIds = res.chunkList;

        if (chunkIds == null) {
            Message get = new Message();
            get.type = RequestType.GET_CHUNKS;
            get.fileName = gfsPath;
            chunkIds = RPC.master(get).chunkList;
        }

        List<byte[]> chunks = split(fileData);

        for (int i = 0; i < chunkIds.size(); i++) {

            Message meta = new Message();
            meta.type = RequestType.GET_CHUNKS;
            meta.fileName = gfsPath;

            Message info = RPC.master(meta);

            Message write = new Message();
            write.type = RequestType.WRITE_CHUNK;
            write.chunkId = chunkIds.get(i);
            write.data = chunks.get(i);

            for (String server : info.chunkServerList) {
                String[] p = server.split(":");
                RPC.chunk(p[0], Integer.parseInt(p[1]), write);
            }
        }
    }

    private static List<byte[]> split(byte[] data) {
        List<byte[]> parts = new java.util.ArrayList<>();
        for (int i = 0; i < data.length; i += CHUNK_SIZE) {
            int end = Math.min(data.length, i + CHUNK_SIZE);
            parts.add(java.util.Arrays.copyOfRange(data, i, end));
        }
        return parts;
    }

    private static String getExtension(Path file) {
        String name = file.getFileName().toString();
        int idx = name.lastIndexOf('.');
        return idx == -1 ? "unknown" : name.substring(idx + 1);
    }
}
