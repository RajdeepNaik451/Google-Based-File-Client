import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileDownloader {

    public static void download(String gfsPath, Path output) throws Exception {

        Message get = new Message();
        get.type = RequestType.GET_CHUNKS;
        get.fileName = gfsPath;

        Message res = RPC.master(get);
        List<String> chunkIds = res.chunkList;

        List<byte[]> data = new ArrayList<>();

        for (String chunkId : chunkIds) {
            Message read = new Message();
            read.type = RequestType.READ_CHUNK;
            read.chunkId = chunkId;

            Message readRes = RPC.chunk("localhost", 6001, read);
            data.add(readRes.data);
        }

        int size = data.stream().mapToInt(b -> b.length).sum();
        byte[] full = new byte[size];

        int pos = 0;
        for (byte[] part : data) {
            System.arraycopy(part, 0, full, pos, part.length);
            pos += part.length;
        }

        Files.write(output, full);
    }
}
