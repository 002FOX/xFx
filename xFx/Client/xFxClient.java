import java.net.*;
import java.io.*;
import java.util.StringTokenizer;
import java.security.MessageDigest;
import java.util.Scanner;


public class xFxClient {

    public static void main(String[] args) throws Exception {

        Scanner sc = new Scanner(System.in);
        System.out.println("s - Get all files.\n d - download a file.\n u- upload a file.\n r- resume a file download previously interrupted.\n ");
        String command = sc.next();

        try (Socket connectionToServer = new Socket("localhost", 80)) {


            InputStream in = connectionToServer.getInputStream();
            OutputStream out = connectionToServer.getOutputStream();

            BufferedReader headerReader = new BufferedReader(new InputStreamReader(in));
            BufferedWriter headerWriter = new BufferedWriter(new OutputStreamWriter(out));


            DataInputStream dataIn = new DataInputStream(in);
            DataOutputStream dataOut = new DataOutputStream(out);

            // Download
            if (command.equals("d")) {
                System.out.println("Which file would you like to download? (full path can be seen by the show command)\n");

                String fileName;
                do {
                    System.out.println("Do not include ..");
                    fileName = sc.next();
                }while(fileName.contains(".."));

                // If file already exists send a hash with the header, if not send 0 as the hash
                File f = new File("ClientShare/" + fileName);
                String hash = "0";
                if(f.isFile()){
                    MessageDigest mdigest = MessageDigest.getInstance("MD5");
                    hash = checksum(mdigest, f);
                }
                String header = "download " + fileName + " " + hash +  "\n";
                headerWriter.write(header, 0, header.length());
                headerWriter.flush();

                header = headerReader.readLine();

                if (header.equals("NOT FOUND")) {
                    System.out.println("We're extremely sorry, the file you specified is not available!");
                } else {
                    StringTokenizer strk = new StringTokenizer(header, " ");

                    String status = strk.nextToken();

                    if (status.equals("OK")) {

                        String temp = strk.nextToken();

                        int size = Integer.parseInt(temp);

                        byte[] space = new byte[size];

                        dataIn.readFully(space);
                        File file = new File(fileName);
                        try (FileOutputStream fileOut = new FileOutputStream("ClientShare/" + file.getName())) {
                            fileOut.write(space, 0, size);
                        }

                    } else if(status.equals("EXISTING")){
                        System.out.println("You already downloaded the file");
                    } else {
                        System.out.println("You're not connected to the right Server!");
                    }

                }

            // Show: get all files
            } else if (command.equals("s")) {
                String header = "show \n";
                headerWriter.write(header, 0, header.length());
                headerWriter.flush();

                header = headerReader.readLine();
                StringTokenizer strk = new StringTokenizer(header, " ");

                String status = strk.nextToken();

                if (status.equals("OK")) {
                    BufferedReader stringReader = new BufferedReader(new InputStreamReader(in));
                    // number of files. (not used)
                    int size = Integer.parseInt(strk.nextToken());
                    String message = stringReader.readLine();
                    System.out.println("These are all the files in the server:\n" + message);
                }

            } else if (command.equals("r")) {
                System.out.println("Which file would you like to resume download? (full path can be seen by the show command)\n");

                String fileName;
                do {
                    fileName = sc.next();
                }while(fileName.contains(".."));

                File f = new File("ClientShare/" + fileName);
                if(f.isFile()) {
                    String header = "resume " + fileName + " " + f.length() + "\n";
                    headerWriter.write(header, 0, header.length());
                    headerWriter.flush();

                    header = headerReader.readLine();

                    if (header.equals("NOT FOUND")) {
                        System.out.println("We're extremely sorry, the file you specified is not available!");
                    } else if(header.equals("COMPLETED")) {
                        System.out.println("The file is finished downloading!");
                    }else {
                            StringTokenizer strk = new StringTokenizer(header, " ");

                            String status = strk.nextToken();

                            if (status.equals("RESUMING")) {

                                String temp = strk.nextToken();

                                int size = Integer.parseInt(temp);

                                byte[] remainingSize = new byte[size];

                                dataIn.read(remainingSize);
                                // We append the existing file to download the remaining data sent by the server
                                try (FileOutputStream fileOut = new FileOutputStream("ClientShare/" + fileName, true)) {
                                    fileOut.write(remainingSize, 0, size);
                                }

                            }
                        }

                }else{
                    System.out.println("You did not download this file before!");
                }
            // Upload
            } else if(command.equals("u")){
                try {
                    System.out.println("Which file would you like to upload? (no subdirectories)\n");

                    String fileName;
                    do {
                        fileName = sc.next();
                    }while(fileName.contains(".."));

                    FileInputStream fileIn = new FileInputStream("ClientShare/" + fileName);
                    int fileSize = fileIn.available();
                    String header = "upload " + fileName + " " + fileSize + "\n";

                    headerWriter.write(header, 0, header.length());
                    headerWriter.flush();

                    byte[] bytes = new byte[fileSize];
                    fileIn.read(bytes);

                    fileIn.close();

                    dataOut.write(bytes, 0, fileSize);

                    header = headerReader.readLine();
                    if (header.equals("OK")) {
                        System.out.println("Successfully uploaded the file: " + fileName);
                    } else if (header.equals("OVERRIDDEN")) {
                        System.out.println("Successfully changed the file on the server.");
                    } else {
                        System.out.println("Server Problem...");
                    }
                }catch(Exception ex){
                    System.out.println("No such file exists on ClientShare/.");
                }

            }else {
                System.out.println("Incorrect command!");
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