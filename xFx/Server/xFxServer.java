import java.net.*;
import java.io.*;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.*;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

public class xFxServer {

    public static void main(String[] args) throws Exception {



        try (ServerSocket ss = new ServerSocket(80)) {
            while (true) {
                System.out.println("Server waiting...");
                Socket connectionFromClient = ss.accept();
                System.out.println(
                        "Server got a connection from a client whose port is: " + connectionFromClient.getPort());

                try {
                    InputStream in = connectionFromClient.getInputStream();
                    OutputStream out = connectionFromClient.getOutputStream();

                    String errorMessage = "NOT FOUND\n";

                    BufferedReader headerReader = new BufferedReader(new InputStreamReader(in));
                    BufferedWriter headerWriter = new BufferedWriter(new OutputStreamWriter(out));

                    DataInputStream dataIn = new DataInputStream(in);
                    DataOutputStream dataOut = new DataOutputStream(out);


                    String header = headerReader.readLine();
                    StringTokenizer strk = new StringTokenizer(header, " ");
                    String command = strk.nextToken();
                    System.out.println(command);

                    if (command.equals("download")) {
                        try {
                            String fileName = strk.nextToken();
                            FileInputStream fileIn = new FileInputStream("ServerShare/" + fileName);
                            String clientFileHash = strk.nextToken();
                            File f = new File("ServerShare/" + fileName);
                            // Client already has file, we compare the two hashes to see if it needs a new download
                            if(clientFileHash.length() > 0){
                                MessageDigest mdigest = MessageDigest.getInstance("MD5");
                                String hash = checksum(mdigest, f);
                                if(hash.equals(clientFileHash)){
                                    header = "EXISTING\n";
                                    headerWriter.write(header, 0, header.length());
                                    headerWriter.flush();
                                    connectionFromClient.close();
                                }
                            }

                            int fileSize = fileIn.available();
                            header = "OK " + fileSize + "\n";

                            headerWriter.write(header, 0, header.length());
                            headerWriter.flush();

                            byte[] bytes = new byte[fileSize];
                            fileIn.read(bytes);

                            fileIn.close();

                            dataOut.write(bytes, 0, fileSize);

                        } catch (Exception ex) {
                            headerWriter.write(errorMessage, 0, errorMessage.length());
                            headerWriter.flush();

                        } finally {
                            connectionFromClient.close();
                        }
                    } else if (command.equals("show")) {

                        try {
                            BufferedWriter stringWriter = new BufferedWriter(new OutputStreamWriter(out));
                            final File folder = new File("ServerShare");
                            List<String> allFiles = new ArrayList<String>();
                            String output = "";

                            getFiles(folder, allFiles);
                            header = "OK " + allFiles.size() + "\n";

                            headerWriter.write(header, 0, header.length());
                            headerWriter.flush();

                            for(String file: allFiles) {
                                output += file + " ";
                            }

                            stringWriter.write(output);
                            stringWriter.flush();

                        } catch (Exception ex) {
                            System.out.println(ex);
                        }finally {
                            connectionFromClient.close();
                        }

                    } else if (command.equals("resume")){
                        try {
                            String fileName = strk.nextToken();
                            String temp = strk.nextToken();
                            int downloadedSize = Integer.parseInt(temp);
                            FileInputStream fileIn = new FileInputStream("ServerShare/" + fileName);

                            int remainingSize = fileIn.available() - downloadedSize;
                            if(remainingSize > 0) {
                                header = "RESUMING " + remainingSize + "\n";

                                headerWriter.write(header, 0, header.length());
                                headerWriter.flush();

                                byte[] bytes = new byte[remainingSize];
                                fileIn.skip(downloadedSize);
                                fileIn.read(bytes);

                                fileIn.close();
                            } else {
                                header = "COMPLETED\n";
                                headerWriter.write(header, 0, header.length());
                                headerWriter.flush();
                            }

                        } catch (Exception ex) {
                            headerWriter.write(errorMessage, 0, errorMessage.length());
                            headerWriter.flush();

                        } finally {
                            connectionFromClient.close();
                        }


                    } else if(command.equals("upload")) {
                        String error = "ERROR\n";
                        try {
                            String fileName = strk.nextToken();
                            final File folder = new File("ServerShare");
                            List<String> fileNames = new ArrayList<String>();

                            getFileNames(folder, fileNames);

                            String temp = strk.nextToken();
                            int size = Integer.parseInt(temp);

                            byte[] space = new byte[size];

                            dataIn.readFully(space);

                            try (FileOutputStream fileOut = new FileOutputStream("ServerShare/" + fileName)) {
                                fileOut.write(space, 0, size);
                            }
                            System.out.println(fileNames);
                            System.out.println(fileName);
                            if(fileNames.contains(fileName)){
                                header = "OVERRIDDEN\n";
                            }else {
                                header = "OK\n";
                            }
                            headerWriter.write(header, 0, header.length());
                            headerWriter.flush();
                        } catch (Exception ex) {
                            System.out.println(ex);
                            headerWriter.write(error, 0, error.length());
                            headerWriter.flush();
                        } finally {
                            connectionFromClient.close();
                        }

                    }else {

                        System.out.println("Connection got from an incompatible client");

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void getFiles(final File dir, List<String> allFiles){
        for (final File file : dir.listFiles()){
            if (file.isDirectory()){
                getFiles(file, allFiles);
            } else {
                allFiles.add(file.getPath().replace("ServerShare\\", "").replace("\\", "/"));
            }
        }
    }

    public static void getFileNames(final File dir, List<String> allFiles){
        for (final File file : dir.listFiles()){
            if (file.isDirectory()){
                getFiles(file, allFiles);
            } else {
                allFiles.add(file.getName());
            }
        }
    }
    private static String checksum(MessageDigest digest, File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);

        byte[] byteArray = new byte[1024];
        int bytesCount = 0;

        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        };

        fis.close();
        byte[] bytes = digest.digest();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < bytes.length; i++) {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
}