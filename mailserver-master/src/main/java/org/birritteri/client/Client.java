package org.birritteri.client;

import org.birritteri.mail.Email;
import org.birritteri.mail.Inbox;
import org.birritteri.main.ClientController;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

public class Client {
    private final Inbox inbox;
    private Socket socket;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private boolean serverResponse;

    private Thread thread;

    /*
        TODO:
            - gestione email non mandata per server spento
     */

    public Client(String email_address, Socket socket) {
        this.inbox = new Inbox(email_address);
        this.socket = socket;
    }

    public String getEmailAddress() {
        return inbox.getEmailAddress();
    }

    public ArrayList<Email> getEmailList() {
        return inbox.getEmailList();
    }

    public boolean tryLogin() {
        try {
            openStreams();
            System.out.println("[" + getEmailAddress() + "] contatto il server...");

            objectOutputStream.writeBoolean(false);
            objectOutputStream.flush();

            objectOutputStream.writeUTF(getEmailAddress());
            objectOutputStream.flush();

            objectOutputStream.writeBoolean(false);
            objectOutputStream.flush();

            this.serverResponse = objectInputStream.readBoolean();
            if (serverResponse) {
                ArrayList<Email> emailList = (ArrayList<Email>) objectInputStream.readObject();
                if (!emailList.isEmpty())
                    this.inbox.setEmailList(emailList);
                return true;
            } else {
                return false;
            }
        } catch (ConnectException ce) {
            return false;
        } catch (IOException | ClassNotFoundException se) {
            se.printStackTrace();
            return false;
        }
    }

    public boolean signIn() {
        try {
            openStreams();

            System.out.println("[" + getEmailAddress() + "] contatto il server...");

            objectOutputStream.writeBoolean(false);
            objectOutputStream.flush();

            objectOutputStream.writeUTF(getEmailAddress());
            objectOutputStream.flush();

            objectOutputStream.writeBoolean(true);
            objectOutputStream.flush();

            this.serverResponse = objectInputStream.readBoolean();
            if (serverResponse) {
                ArrayList<Email> emailList = (ArrayList<Email>) objectInputStream.readObject();
                if (!emailList.isEmpty())
                    this.inbox.setEmailList(emailList);

                return true;
            } else
                return false;
        } catch (ConnectException ce) {
            return false;
        } catch (IOException | ClassNotFoundException se) {
            se.printStackTrace();
            return false;
        }
    }

    public synchronized boolean sendToServer(Email email) {
        Thread sender = new Thread(() -> {
            serverResponse = false;
            while (!serverResponse) {
                try {
                    objectOutputStream.writeObject(email);
                    objectOutputStream.flush();

                    serverResponse = objectInputStream.readBoolean();
                } catch (IOException se) {
                    newSocket();
                }
            }
        });

        sender.setDaemon(true);
        sender.start();

        return serverResponse;
    }


    public synchronized boolean removeFromServer(String id) {
        try {

            objectOutputStream.writeObject(id);
            objectOutputStream.flush();

            return objectInputStream.readBoolean();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Email newEmail(String sender, ArrayList<String> addressees, String object, String body) {
        return inbox.newEmail(sender, addressees, object, body);
    }

    public void addEmail(Email email) {
        if (!inbox.containsEmail(email))
            inbox.addEmail(email);
    }

    public void rmvEmail(Email emailToRemove) {
        inbox.rmvEmail(emailToRemove);
    }

    public void updateList(ArrayList<Email> newList) {
        inbox.updateList(newList);
    }

    public void newSocket() {
        boolean isConnected = false;
        while (!isConnected) {
            try {
                socket = new Socket("localhost", 60000);

                openStreams();
                objectOutputStream.writeBoolean(true);
                objectOutputStream.flush();

                objectOutputStream.writeUTF(getEmailAddress());
                objectOutputStream.flush();

                isConnected = socket.isConnected();
            } catch (ConnectException ignored) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void openStreams() {
        try {
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.flush();
            objectInputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeStreams() {
        try {
            if (objectOutputStream != null)
                objectOutputStream.close();
            if (objectInputStream != null)
                objectInputStream.close();
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public synchronized void createUpdateInboxThread() {
        thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(5000);

                    objectOutputStream.writeObject("updateinbox");
                    objectOutputStream.flush();

                    boolean serverResponse = objectInputStream.readBoolean();
                    if (serverResponse) {
                        ArrayList<Email> emailList = (ArrayList<Email>) objectInputStream.readObject();
                        //System.out.println(emailList);
                        if (!emailList.isEmpty()) {
                            this.inbox.setEmailList(emailList);

                        }
                    }

                } catch (SocketException e) {
                    // log the error
                    System.err.println("Server is down: " + e.getMessage());

                    // wait for a short period of time before attempting to reconnect
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }

                    // attempt to reconnect to the server
                    try {
                        // close the existing socket
                        socket.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }

                    try {
                        // create a new socket
                        socket = new Socket("localhost", 60000);
                        tryLogin();
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                } catch (IOException | ClassNotFoundException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }


}
