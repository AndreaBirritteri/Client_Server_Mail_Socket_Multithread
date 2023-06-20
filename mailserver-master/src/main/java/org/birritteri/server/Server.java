package org.birritteri.server;

import javafx.scene.layout.VBox;
import org.birritteri.mail.Email;
import org.birritteri.mail.Inbox;
import org.birritteri.main.ServerController;

import java.io.*;
import java.net.ServerSocket;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Server {
    private final ServerSocket serverSocket;
    private static HashMap<String, ServerThread> serverThreads;
    private static ArrayList<Inbox> inboxList;

    /*
        TODO:
            - riconnessione al client
    */

    public Server(List<String> emailList, ServerSocket serverSocket) throws IOException {
        serverThreads = new HashMap<>();

        inboxList = new ArrayList<>();
        for (String email :
                emailList) {
            inboxList.add(new Inbox(email));
        }

        this.serverSocket = serverSocket;
    }

    public void clientLogin(VBox vboxMessages) {
        new Thread(() -> {
            ServerController.addLog("Server online and ready to work.", vboxMessages);
            while (!serverSocket.isClosed()) {
                try {
                    ServerThread serverThread = new ServerThread(serverSocket.accept(), vboxMessages);
                    serverThread.start();
                } catch (SocketException socketException) {
                    System.out.println("Server offline.");
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        }).start();
    }

    public static void addServerThread(ServerThread serverThread) {
        serverThreads.put(serverThread.getEmailAddress(), serverThread);
    }

    public static ServerThread getServerThread(String emailAddress) {
        return serverThreads.get(emailAddress);
    }

    public static boolean isInInbox(Inbox inbox) {
        return inboxList.contains(inbox);
    }

    public static boolean areInInbox(List<String> addresses) {
        boolean areInInbox = true;

        for (String address :
                addresses) {
            areInInbox &= inboxList.contains(new Inbox(address));
        }

        return areInInbox;
    }

    public static boolean addInbox(Inbox inbox) {
        inboxList.add(inbox);
        File file = new File("C:\\Users\\Utente\\Desktop\\mailserver-master-20230203T125909Z-001\\mailserver-master\\mailserver-master\\src\\main\\java\\org\\birritteri\\server\\files\\email_addresses.txt");
        String filePath = file.getAbsolutePath();
        Path inputFilePath = Paths.get(filePath);
        String email = "\n" + inbox.getEmailAddress();

        try {
            Files.write(inputFilePath, email.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException exception) {
            System.err.println("IOException: " + exception);
        }

        return createPersistence(inbox.getEmailAddress());
    }

    private static boolean createPersistence(String emailAddress) {
        boolean created = false;

        try {
            File persistence = new File("C:\\Users\\Utente\\Desktop\\mailserver-master-20230203T125909Z-001\\mailserver-master\\mailserver-master\\src\\main\\java\\org\\birritteri\\server\\files\\email_persistence\\"
                    + emailAddress + ".csv");
            created = persistence.createNewFile();
        } catch (IOException e) {
            System.err.println("IOException: " + e);
        }

        return created;
    }

    public static void addEmailToPersistence(Email email) {
        String emailFormatted = email.csvEmailFormatter();

        try{
            File senderPersistence = new File("C:\\Users\\Utente\\Desktop\\mailserver-master-20230203T125909Z-001\\mailserver-master\\mailserver-master\\src\\main\\java\\org\\birritteri\\server\\files\\email_persistence\\"
                    + email.getSender() + ".csv");

            Charset ENCODING = StandardCharsets.UTF_8;
            Path inputFilePath = Paths.get(senderPersistence.getAbsolutePath());
            BufferedReader reader = Files.newBufferedReader(inputFilePath, ENCODING);
            String firstLine = reader.readLine();

            if(firstLine != null && !firstLine.isBlank()) {
                String newLineEmail = "\n" + emailFormatted;
                Files.write(Paths.get(senderPersistence.getAbsolutePath()), newLineEmail.getBytes(), StandardOpenOption.APPEND);
            } else
                Files.write(Paths.get(senderPersistence.getAbsolutePath()), emailFormatted.getBytes(), StandardOpenOption.WRITE);


            reader.close();

            for (String address :
                    email.getAddresses()) {
                if(!address.equals(email.getSender())) {
                    File addressPersistence = new File("C:\\Users\\Utente\\Desktop\\mailserver-master-20230203T125909Z-001\\mailserver-master\\mailserver-master\\src\\main\\java\\org\\birritteri\\server\\files\\email_persistence\\"
                            + address + ".csv");

                    inputFilePath = Paths.get(addressPersistence.getAbsolutePath());
                    reader = Files.newBufferedReader(inputFilePath, ENCODING);
                    firstLine = reader.readLine();

                    if(firstLine != null && !firstLine.isBlank()) {
                        String newLineEmail = "\n" + emailFormatted;
                        Files.write(Paths.get(addressPersistence.getAbsolutePath()), newLineEmail.getBytes(), StandardOpenOption.APPEND);
                    } else
                        Files.write(Paths.get(addressPersistence.getAbsolutePath()), emailFormatted.getBytes(), StandardOpenOption.WRITE);

                    reader.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean rmvEmailFromPersistence(String emailId, String clientEmailAddress) {
        File clientPersistence = new File("C:\\Users\\Utente\\Desktop\\mailserver-master-20230203T125909Z-001\\mailserver-master\\mailserver-master\\src\\main\\java\\org\\birritteri\\server\\files\\email_persistence\\"
                + clientEmailAddress + ".csv");
        File tmpFile = new File("C:\\Users\\Utente\\Desktop\\mailserver-master-20230203T125909Z-001\\mailserver-master\\mailserver-master\\src\\main\\java\\org\\birritteri\\server\\files\\email_persistence\\"
                + clientEmailAddress + "_temp.csv");

        Charset ENCODING = StandardCharsets.UTF_8;
        Path inputFilePath = Paths.get(clientPersistence.getAbsolutePath());

        try{
            BufferedReader reader = Files.newBufferedReader(inputFilePath, ENCODING);
            BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile));

            String line;
            int lineCount = 0, c = 0;
            boolean find = false;
            while (!find && (line = reader.readLine()) != null) {
                find = emailId.equals(line.split("\\|")[0]);
                if(!find)
                    lineCount++;
            }

            reader.close();
            reader = Files.newBufferedReader(inputFilePath, ENCODING);
            while((line = reader.readLine()) != null) {
                if(c != lineCount) {
                    writer.write(line);
                    writer.newLine();
                }
                c++;
            }

            writer.close();
            reader.close();
            if(clientPersistence.delete())
                return tmpFile.renameTo(clientPersistence);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static ArrayList<Email> loadEmailFromPersistence(String emailAddress) throws IOException {
        File addressPersistence = new File("C:\\Users\\Utente\\Desktop\\mailserver-master-20230203T125909Z-001\\mailserver-master\\mailserver-master\\src\\main\\java\\org\\birritteri\\server\\files\\email_persistence\\"
                + emailAddress + ".csv");
        Path inputFilePath = Paths.get(addressPersistence.getAbsolutePath());
        Charset ENCODING = StandardCharsets.UTF_8;
        ArrayList<Email> emailList = new ArrayList<>();

        try (BufferedReader fileInputReader = Files.newBufferedReader(inputFilePath, ENCODING)) {
            String line;
            while ((line = fileInputReader.readLine()) != null) {
                if(!line.isEmpty()){
                String[] lineElements = line.split("\\|");
                ArrayList<String> addresses = new ArrayList<>(Arrays.asList(lineElements[3].split("; |;")));

                if(lineElements.length == 4)
                    emailList.add(new Email(lineElements[0], lineElements[1],
                            lineElements[2], addresses, "",""));
                else if(lineElements.length == 5)
                    emailList.add(new Email(lineElements[0], lineElements[1],
                            lineElements[2], addresses, lineElements[4],""));
                else
                    if(lineElements[4].isBlank())
                        emailList.add(new Email(lineElements[0], lineElements[1],
                                lineElements[2], addresses, "", lineElements[5]));
                    else
                        emailList.add(new Email(lineElements[0], lineElements[1],
                                lineElements[2], addresses, lineElements[4],
                                lineElements[5].replaceAll("~", "\n")));
            }}
        }

        return emailList;
    }

    public boolean shutdown() {
        for (String emailAddress :
                serverThreads.keySet()) {
            serverThreads.get(emailAddress).closeStreams();
            serverThreads.get(emailAddress).interrupt();
        }

        try{
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return serverSocket.isClosed();
    }
}