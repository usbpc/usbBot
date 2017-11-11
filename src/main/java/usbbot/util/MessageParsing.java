package usbbot.util;

/**
 * Created by usbpc on 02.05.2017.
 */
public class MessageParsing {
    public static long getUserID(String id) {
        long userID;
        if (id.matches("\\d{18,19}+")) {
            userID = Long.valueOf(id);
        } else if (id.matches("<@!??\\d{18,19}?>")) {
            userID = Long.valueOf(id.substring((id.indexOf('!') != -1 ? id.indexOf('!') : id.indexOf('@')) + 1, id.indexOf('>')));
        } else {
            return -1;
        }
        return userID;
    }

    public static long getGroupID(String id) {
        long longId;
        if (id.matches("\\d{18,19}+")) {
            longId = Long.valueOf(id);
        } else if (id.matches("<@&\\d{18,19}?>"))  {
            longId = Long.valueOf(id.substring(3, id.length() - 1));
        } else {
            return -1;
        }
        return longId;
    }
}
