package org.birritteri.server;

import javafx.scene.layout.VBox;
import org.birritteri.mail.Email;
import org.birritteri.mail.Inbox;
import org.birritteri.main.ServerController;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

public class ServerThread extends Thread {
    private String emailAddress;
    private final VBox serverVBox;
    private final Socket socket;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;

    public ServerThread(Socket socket, VBox serverVBox) {
        this.socket = socket;
        this.serverVBox = serverVBox;
    }

    @Override
    public void run() {
        try {
            if (loginSignIn()) {
                Server.addServerThread(this);

                while (!socket.isClosed()) {
                    synchronized (objectInputStream){
                    try {
                        Object request = objectInputStream.readObject();

                        if (request instanceof Email) {
                            Email emailToForward = (Email) request;
                            if (Server.areInInbox(emailToForward.getAddresses())) {
                                objectOutputStream.writeBoolean(true);
                                objectOutputStream.flush();

                                String log = emailToForward.getSender() + " send an email to " +
                                        emailToForward.printAddresses();
                                ServerController.addLog(log, serverVBox);
                                Server.addEmailToPersistence(emailToForward);
                                //forwardEmail(emailToForward, emailToForward.getAddresses());
                            } else {
                                objectOutputStream.writeBoolean(true);
                                objectOutputStream.flush();

                                String log = emailToForward.getSender() +
                                        " cannot send email, because one or more emails are wrong.";
                                ServerController.addLog(log, serverVBox);
                                ArrayList<String> c = new ArrayList<>();
                                c.add(emailAddress);
                                Email email = new Email(emailToForward.getId(),"mailer-daemon@unito.it",c,"Delivery Status Notification (Failure)",emailToForward.getBody());
                                Server.addEmailToPersistence(email);
                            }
                        } else if (request instanceof String) {
                            String req = (String) request;
                            if (req.equals("updateinbox")) {
                                // Perform the action for "update inbox" request
                                objectOutputStream.writeBoolean(true);
                                objectOutputStream.flush();
                                ArrayList<Email> inboxEmails = Server.loadEmailFromPersistence(emailAddress);
                                objectOutputStream.writeObject(inboxEmails);
                                objectOutputStream.flush();
                            }else{
                                Server.rmvEmailFromPersistence(req, emailAddress);
                                objectOutputStream.writeBoolean(true);
                                objectOutputStream.flush();
                            }
                        }

                    } catch (EOFException ignored) {
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (SocketException socketException) {
                        System.out.println("Socket closed.");
                        String log = this.emailAddress + " disconnected.";
                        ServerController.addLog(log, serverVBox);
                        closeStreams();
                    }
                }}
            }
        } catch (IOException | ServerException e) {
            e.printStackTrace();
        }
    }


    public boolean loginSignIn() throws IOException, ServerException {
        String log;
        openStreams();

        boolean logged = objectInputStream.readBoolean();
        if(!logged) {
            String emailAddress = objectInputStream.readUTF();
            Inbox inbox = new Inbox(emailAddress);
            boolean isOnServer = Server.isInInbox(inbox);

            if (objectInputStream.readBoolean()) {
                if (isOnServer) {
                    log = emailAddress + " already exists.";
                    ServerController.addLog(log, serverVBox);
                } else {
                    if (Server.addInbox(inbox)) {
                        isOnServer = Server.isInInbox(inbox);
                        log = emailAddress + " added.";
                        ServerController.addLog(log, serverVBox);
                    } else throw new ServerException("Cannot create the inbox.");
                }
            }

            if (isOnServer) {
                this.emailAddress = emailAddress;
                objectOutputStream.writeBoolean(true);
                objectOutputStream.flush();
                System.out.println(getEmailAddress());
                ArrayList<Email> emailList = Server.loadEmailFromPersistence(this.emailAddress);

                objectOutputStream.writeObject(emailList);
                objectOutputStream.flush();

                log = emailAddress + " logged in.";
                ServerController.addLog(log, serverVBox);
            } else {
                log = emailAddress + " doesn't exist.";
                ServerController.addLog(log, serverVBox);
                objectOutputStream.writeBoolean(false);
                objectOutputStream.flush();
            }

            return isOnServer;
        } else {
            this.emailAddress = objectInputStream.readUTF();
            log = emailAddress + " already logged.";
            ServerController.addLog(log, serverVBox);
            return true;
        }
    }

    /*public void updateClient(Email emailToForward) throws IOException {
        objectOutputStream.writeObject(emailToForward);
        objectOutputStream.flush();
    }

    public void forwardEmail(Email emailToForward, ArrayList<String> addressees) throws IOException, InterruptedException {
        for (String clientAddress :
                addressees) {
            if(!clientAddress.equals(emailAddress)) {
                Server.getServerThread(clientAddress).wait();
                Server.getServerThread(clientAddress).updateClient(emailToForward);
                Server.getServerThread(clientAddress).notify();
            }
        }
    }*/

    public Socket getSocket() {
        return socket;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void openStreams() throws IOException {
        objectInputStream = new ObjectInputStream(socket.getInputStream());
        objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        objectOutputStream.flush();
    }

    public void closeStreams() {
        try {
            if (objectOutputStream != null)
                objectOutputStream.close();
            if (objectInputStream != null)
                objectInputStream.close();
            if (socket != null)
                socket.close();
            this.interrupt();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
