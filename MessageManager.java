
import java.util.ArrayList;

/**
 * MessageManager – Part 3
 *
 * Manages five runtime arrays:
 *   1. sentMessages        – messages the user chose to Send
 *   2. disregardedMessages – messages the user chose to Disregard
 *   3. storedMessages      – messages loaded from messages.json
 *   4. messageHashes       – one hash per sent/stored message
 *   5. messageIDs          – one ID per sent/stored message
 *
 * NOTE ON TEST DATA MESSAGE 4:
 *   The assignment spec states that a valid recipient cell number must start
 *   with an international code (i.e. begin with '+').  However, Test Data
 *   Message 4 supplies "0838884567" which has NO '+' prefix.
 *   Per the specification, this number FAILS validation and Message 4 is
 *   therefore treated as INVALID / not added to the sent array.
 *   The unit tests reflect this behaviour.
 */
public class MessageManager {

    // ─── The five arrays ──────────────────────────────────────────────────────
    private ArrayList<String[]> sentMessages        = new ArrayList<>();
    private ArrayList<String[]> disregardedMessages = new ArrayList<>();
    private ArrayList<String[]> storedMessages      = new ArrayList<>();
    private ArrayList<String>   messageHashes       = new ArrayList<>();
    private ArrayList<String>   messageIDs          = new ArrayList<>();

    // Each String[] row = { messageID, messageHash, recipient, messageBody }

    // ─── Add helpers called by QuickChat after the user makes a choice ────────

    public void addSentMessage(String[] msgRow) {
        sentMessages.add(msgRow);
        messageHashes.add(msgRow[1]);
        messageIDs.add(msgRow[0]);
    }

    public void addDisregardedMessage(String[] msgRow) {
        disregardedMessages.add(msgRow);
    }

    public void addStoredMessage(String[] msgRow) {
        storedMessages.add(msgRow);
        if (!messageHashes.contains(msgRow[1])) messageHashes.add(msgRow[1]);
        if (!messageIDs.contains(msgRow[0]))    messageIDs.add(msgRow[0]);
    }

    /** Load stored messages from messages.json into the storedMessages array. */
   public void loadStoredMessages() {
    storedMessages.clear();
    java.io.File file = new java.io.File("messages.json");
    if (!file.exists() || file.length() == 0) {
        System.out.println("No stored messages found (messages.json missing or empty).");
        return;
    }
    try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(file))) {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line.trim());

        String content = sb.toString().trim();
        // Strip outer [ ]
        if (content.startsWith("[")) content = content.substring(1);
        if (content.endsWith("]"))   content = content.substring(0, content.length() - 1);

        // Split by },{ to get individual objects
        String[] objects = content.split("\\},\\{");
        for (String obj : objects) {
            obj = obj.replace("{", "").replace("}", "");
            String id       = extractValue(obj, "messageID");
            String hash     = extractValue(obj, "messageHash");
            String recipient= extractValue(obj, "recipient");
            String message  = extractValue(obj, "message");
            if (id != null && hash != null && recipient != null && message != null) {
                String[] row = { id, hash, recipient, message };
                storedMessages.add(row);
                if (!messageHashes.contains(hash)) messageHashes.add(hash);
                if (!messageIDs.contains(id))      messageIDs.add(id);
            }
        }
        System.out.println("Stored messages loaded: " + storedMessages.size());
    } catch (java.io.IOException e) {
        System.out.println("Error reading messages.json: " + e.getMessage());
    }
}

private String extractValue(String obj, String key) {
    String search = "\"" + key + "\":\"";
    int start = obj.indexOf(search);
    if (start == -1) return null;
    start += search.length();
    int end = obj.indexOf("\"", start);
    if (end == -1) return null;
    return obj.substring(start, end);
} 

    // ─── a) Display sender and recipient of all stored messages ───────────────
    public String displayStoredSenderRecipient() {
        if (storedMessages.isEmpty()) return "No stored messages found.";
        StringBuilder sb = new StringBuilder();
      sb.append("\n=== Stored Messages - Sender & Recipient ===\n");
        int i = 1;
        for (String[] msg : storedMessages) {
            sb.append(i++).append(". ")
              .append("ID: ").append(msg[0])
              .append(" | Recipient: ").append(msg[2])
              .append("\n");
        }
        return sb.toString();
    }

    // ─── b) Display the longest message (across sent + stored) ───────────────
    public String displayLongestMessage() {
        ArrayList<String[]> all = new ArrayList<>();
        all.addAll(sentMessages);
        all.addAll(storedMessages);
        if (all.isEmpty()) return "No messages available.";
        String[] longest = all.get(0);
        for (String[] msg : all) {
            if (msg[3].length() > longest[3].length()) longest = msg;
        }
        return "Longest message: \"" + longest[3] + "\"";
    }

    // ─── c) Search by message ID ──────────────────────────────────────────────
    public String searchByMessageID(String id) {
        ArrayList<String[]> all = new ArrayList<>();
        all.addAll(sentMessages);
        all.addAll(storedMessages);
        StringBuilder sb = new StringBuilder();
        boolean found = false;
        for (String[] msg : all) {
            if (msg[0].equalsIgnoreCase(id)) {
                sb.append("Recipient : ").append(msg[2]).append("\n");
                sb.append("Message   : ").append(msg[3]).append("\n");
                found = true;
            }
        }
        return found ? sb.toString() : "No message found with ID: " + id;
    }

    // ─── d) Search all messages for a particular recipient ───────────────────
    public String searchByRecipient(String recipient) {
        ArrayList<String[]> all = new ArrayList<>();
        all.addAll(sentMessages);
        all.addAll(storedMessages);
        StringBuilder sb = new StringBuilder();
        boolean found = false;
        int i = 1;
        for (String[] msg : all) {
            if (msg[2].equalsIgnoreCase(recipient)) {
                sb.append(i++).append(". \"").append(msg[3]).append("\"\n");
                found = true;
            }
        }
        return found
            ? "Messages for " + recipient + ":\n" + sb
            : "No messages found for recipient: " + recipient;
    }

    // ─── e) Delete a message using its hash ──────────────────────────────────
    public String deleteByHash(String hash) {
        for (int i = 0; i < sentMessages.size(); i++) {
            if (sentMessages.get(i)[1].equalsIgnoreCase(hash)) {
                String body = sentMessages.get(i)[3];
                sentMessages.remove(i);
                messageHashes.remove(hash);
                return "Message: \"" + body + "\" successfully deleted.";
            }
        }
        for (int i = 0; i < storedMessages.size(); i++) {
            if (storedMessages.get(i)[1].equalsIgnoreCase(hash)) {
                String body = storedMessages.get(i)[3];
                storedMessages.remove(i);
                messageHashes.remove(hash);
                return "Message: \"" + body + "\" successfully deleted.";
            }
        }
        return "No message found with hash: " + hash;
    }

    // ─── f) Display full report ───────────────────────────────────────────────
    public String displayReport() {
        ArrayList<String[]> all = new ArrayList<>();
        all.addAll(sentMessages);
        all.addAll(storedMessages);
        if (all.isEmpty()) return "No messages to report.";
        StringBuilder sb = new StringBuilder();
        sb.append("\n========== MESSAGE REPORT ==========\n");
        int i = 1;
        for (String[] msg : all) {
            sb.append("--- Message ").append(i++).append(" ---\n");
            sb.append("Message Hash : ").append(msg[1]).append("\n");
            sb.append("Recipient    : ").append(msg[2]).append("\n");
            sb.append("Message      : ").append(msg[3]).append("\n");
        }
        sb.append("=====================================\n");
        return sb.toString();
    }

    // ─── Getters (used by unit tests) ─────────────────────────────────────────
    public ArrayList<String[]> getSentMessages()        { return sentMessages; }
    public ArrayList<String[]> getDisregardedMessages() { return disregardedMessages; }
    public ArrayList<String[]> getStoredMessages()      { return storedMessages; }
    public ArrayList<String>   getMessageHashes()       { return messageHashes; }
    public ArrayList<String>   getMessageIDs()          { return messageIDs; }
}