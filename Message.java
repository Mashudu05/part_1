import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
 
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
 
public class Message {
 
    // ─── Fields ───────────────────────────────────────────────────────────────
    private String messageID;
    private int numMessagesSent;
    private String recipient;
    private String message;
    private String messageHash;
 
    // Shared across all instances – accumulates sent messages for the session
    private static ArrayList<String[]> sentMessages = new ArrayList<>();
    private static int totalMessagesSent = 0;
 
    // ─── Constructor ──────────────────────────────────────────────────────────
    public Message(String messageID, int numMessagesSent, String recipient, String message) {
        this.messageID       = messageID;
        this.numMessagesSent = numMessagesSent;
        this.recipient       = recipient;
        this.message         = message;
        this.messageHash     = createMessageHash();
    }
 
    // ─── 1. checkMessageID() ──────────────────────────────────────────────────
    /**
     * Returns true if the message ID is no more than 10 characters long.
     */
    public boolean checkMessageID() {
        return messageID != null && messageID.length() <= 10;
    }
 
    // ─── 2. checkRecipientCell() ──────────────────────────────────────────────
    /**
     * Returns:
     *   "Cell phone number successfully captured."   – on success
     *   "Cell phone number is incorrectly formatted or does not contain an
     *    international code. Please correct the number and try again."         – on failure
     *
     * Rules: ≤ 10 characters AND starts with a '+' followed by a country code
     * (i.e. the very first character must be '+').
     */
    public String checkRecipientCell() {
        if (recipient != null
                && recipient.length() <= 10
                && recipient.startsWith("+")) {
            return "Cell phone number successfully captured.";
        }
        return "Cell phone number is incorrectly formatted or does not contain an "
             + "international code. Please correct the number and try again.";
    }
 
    // ─── 3. createMessageHash() ───────────────────────────────────────────────
    /**
     * Hash format:  XX:N:FIRSTLAST
     *   XX         – first two characters of the message ID
     *   N          – numMessagesSent
     *   FIRSTLAST  – first word + last word of the message, concatenated
     * Everything is converted to UPPERCASE.
     *
     * Example from spec:  "00:0:HITHANKS" (messageID starts with "00",
     *                      0 messages sent, first word "Hi", last word "Thanks")
     */
    public final String createMessageHash() {
        if (messageID == null || message == null || messageID.length() < 2) {
            return "";
        }
 
        String idPart   = messageID.substring(0, 2).toUpperCase();
        String numPart  = String.valueOf(numMessagesSent);
        String[] words  = message.trim().split("\\s+");
        String firstWord = words[0];
        String lastWord  = words[words.length - 1];
 
        return (idPart + ":" + numPart + ":" + firstWord + lastWord).toUpperCase();
    }
 
    // ─── 4. SentMessage() ─────────────────────────────────────────────────────
    /**
     * Lets the user choose what to do with the composed message:
     *   1) Send Message   → "Message successfully sent."
     *   2) Disregard      → "Press 0 to delete the message."
     *   3) Store Message  → "Message successfully stored." (also writes JSON)
     *
     * Returns a status string describing the outcome.
     */
    public String SentMessage() {
        Scanner input = new Scanner(System.in);
 
        System.out.println("\nWhat would you like to do with your message?");
        System.out.println("1) Send Message");
        System.out.println("2) Disregard Message");
        System.out.println("3) Store Message");
        System.out.print("Enter choice: ");
 
        String choice = input.nextLine().trim();
 
        switch (choice) {
            case "1":
                totalMessagesSent++;
                numMessagesSent = totalMessagesSent;
                messageHash = createMessageHash();          // recalculate with updated count
                sentMessages.add(new String[]{
                    messageID, messageHash, recipient, message
                });
                return "Message successfully sent.";
 
            case "2":
                return "Press 0 to delete the message.";
 
            case "3":
                storeMessage();
                return "Message successfully stored.";
 
            default:
                return "Invalid choice. Message was not processed.";
        }
    }
 
    // ─── 5. printMessages() ───────────────────────────────────────────────────
    /**
     * Returns a formatted string containing all messages sent during this session.
     * Order per spec: Message ID, Message Hash, Recipient, Message.
     */
    public String printMessages() {
        if (sentMessages.isEmpty()) {
            return "No messages have been sent yet.";
        }
 
        StringBuilder sb = new StringBuilder();
        sb.append("\n===== All Sent Messages =====\n");
        int index = 1;
        for (String[] msg : sentMessages) {
            sb.append("--- Message ").append(index++).append(" ---\n");
            sb.append("Message ID   : ").append(msg[0]).append("\n");
            sb.append("Message Hash : ").append(msg[1]).append("\n");
            sb.append("Recipient    : ").append(msg[2]).append("\n");
            sb.append("Message      : ").append(msg[3]).append("\n");
        }
        sb.append("=============================\n");
        return sb.toString();
    }
 
    // ─── 6. returnTotalMessages() ─────────────────────────────────────────────
    /**
     * Returns the total number of messages successfully sent during this session.
     */
    public int returnTotalMessagess() {
        return totalMessagesSent;
    }
 
    // ─── 7. storeMessage() ────────────────────────────────────────────────────
    /**
     * Appends this message to "messages.json" as a JSON object.
     * Uses org.json.simple – add json-simple-1.1.1.jar to your classpath,
     * OR replace with Gson / Jackson if preferred.
     *
     * If you do not have a JSON library available, a plain-text fallback
     * is included (see comment below).
     */
    @SuppressWarnings("unchecked")
    public void storeMessage() {
        // ── JSON approach (requires json-simple on classpath) ──────────────────
        try {
            JSONObject obj = new JSONObject();
            obj.put("messageID",   messageID);
            obj.put("messageHash", messageHash);
            obj.put("recipient",   recipient);
            obj.put("message",     message);
 
            // Read existing array, append, rewrite – simple single-file storage
            JSONArray arr = new JSONArray();
 
            java.io.File file = new java.io.File("messages.json");
            if (file.exists() && file.length() > 0) {
                try (java.io.FileReader fr = new java.io.FileReader(file)) {
                    Object parsed = new org.json.simple.parser.JSONParser().parse(fr);
                    if (parsed instanceof JSONArray) {
                        arr = (JSONArray) parsed;
                    }
                } catch (Exception ignored) {}
            }
 
            arr.add(obj);
 
            try (FileWriter fw = new FileWriter("messages.json")) {
                fw.write(arr.toJSONString());
            }
 
            System.out.println("Message stored to messages.json.");
 
        } catch (IOException e) {
            System.out.println("Error storing message: " + e.getMessage());
        }
 
        /* ── Plain-text fallback (no external library needed) ────────────────
         * Uncomment the block below and comment out the JSON block above
         * if you cannot add json-simple to your NetBeans project.
         *
         * try (FileWriter fw = new FileWriter("messages.txt", true)) {
         *     fw.write("{\"messageID\":\"" + messageID + "\","
         *            + "\"messageHash\":\"" + messageHash + "\","
         *            + "\"recipient\":\"" + recipient + "\","
         *            + "\"message\":\"" + message + "\"}\n");
         *     System.out.println("Message stored to messages.txt.");
         * } catch (IOException e) {
         *     System.out.println("Error storing message: " + e.getMessage());
         * }
         * ─────────────────────────────────────────────────────────────────── */
    }
 
    // ─── Getters (used by the runner and tests) ───────────────────────────────
    public String getMessageID()   { return messageID; }
    public String getRecipient()   { return recipient; }
    public String getMessage()     { return message; }
    public String getMessageHash() { return messageHash; }
    public int    getNumSent()     { return numMessagesSent; }
 
    // Allow tests / runner to reset static state between runs
    public static void resetSession() {
        sentMessages.clear();
        totalMessagesSent = 0;
    }
}
