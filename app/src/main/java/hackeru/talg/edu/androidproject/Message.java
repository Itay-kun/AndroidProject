package hackeru.talg.edu.androidproject;

public class Message {
    public String phoneNumber;
    public String messageContent;
    public String date;
    public String time;

    public Message () {
    }

    public Message (String phoneNumber, String messageContent, String date, String time) {
        this.phoneNumber = phoneNumber;
        this.messageContent = messageContent;
        this.date = date;
        this.time = time;
    }
}
