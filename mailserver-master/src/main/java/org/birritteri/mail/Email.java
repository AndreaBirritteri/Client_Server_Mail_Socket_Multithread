package org.birritteri.mail;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Pattern;

public class Email implements Serializable {
    @Serial
    private static final long serialVersionUID = 5950169519310163575L;
    private final ArrayList<String> addresses;
    private final String sender, object, body, date, id;

    public Email(String id, String sender, ArrayList<String> addressees, String object, String body) {
        this.id = id;
        this.sender = sender;
        this.addresses = addressees;
        this.object = object;
        this.body = body;
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        this.date = dateTime.format(formatter);
    }

    public Email(String id, String date, String sender, ArrayList<String> addressees, String object, String body) {
        this.id = id;
        this.sender = sender;
        this.addresses = addressees;
        this.object = object;
        this.body = body;
        this.date = date;
    }

    public String getSender() {
        return sender;
    }

    public ArrayList<String> getAddresses() {
        return addresses;
    }

    public String getObject() {
        return object;
    }

    public String getBody() {
        return body;
    }

    public String getDate() {
        return date;
    }

    public String getId() {
        return id;
    }

    public String printAddresses() {
        StringBuilder res = new StringBuilder();
        for (String s : addresses) {
            res.append(s).append("; ");
        }
        return res.substring(0, res.length() - 2);
    }

    public String printAddresses(String emailToExclude) {
        StringBuilder res = new StringBuilder();
        for (String s : addresses) {
            if(!s.equals(emailToExclude))
                res.append(s).append("; ");
        }
        return res.substring(0, res.length() - 2);
    }

    public String emailFormatted(boolean toBeFormatted) {
        String result = "["+ this.date +"]"
                + "\nFrom: "+ this.sender
                + "\nObject: "+this.object
                + "\nBody: ";

        if(toBeFormatted) {
            String oneLineBody = body.trim().replaceAll("\n", " ").replaceAll(" +", " ");
            if (oneLineBody.length() >= 40) {
                result += oneLineBody.substring(0, 39);
            } else {
                result += oneLineBody;
            }
        } else
            result += body;

        return result;
    }

    public String csvEmailFormatter() {
        return this.id
                + "|" + this.date
                + "|" + this.sender
                + "|" + this.printAddresses()
                + "|" + this.object
                + "|" + this.body.trim().replaceAll("\n", "~");
    }

    public static boolean emailValidation(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "unito.it";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return true;
        return !pat.matcher(email).matches();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return Objects.equals(getId(), email.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public String toString() {
        return "Data: " + date + "\n From: " + sender + "\n To: " + printAddresses() + "\n Object: " + object
                + "\n Body: " + body + "\n\n";
    }



}
