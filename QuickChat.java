import java.util.Scanner;

/**
 * QuickChat – Main entry point (updated for Part 3).
 *
 * Menu:
 *   a) Send Messages
 *   b) Show recently sent messages  (Coming Soon)
 *   c) Quit
 *   d) Stored Messages              (NEW – Part 3)
 */
public class QuickChat {

    // Shared manager – lives for the whole session
    private static MessageManager manager = new MessageManager();

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        System.out.println("========================================");
        System.out.println("         Welcome to QuickChat           ");
        System.out.println("========================================");

        String username = LoginTest.login(input);
        if (username == null) {
            System.out.println("Login failed. Exiting.");
            input.close();
            return;
        }

        Welcome.show(username);

        // Load any previously stored messages from JSON at startup
        manager.loadStoredMessages();

        boolean running = true;
        while (running) {
            System.out.println("\n--- QuickChat Menu ---");
            System.out.println("a) Send Messages");
            System.out.println("b) Show recently sent messages");
            System.out.println("c) Quit");
            System.out.println("d) Stored Messages");
            System.out.print("Choose an option: ");

            String choice = input.nextLine().trim().toLowerCase();

            switch (choice) {
                case "a":
                    sendMessageFlow(input);
                    break;
                case "b":
                    System.out.println("Coming Soon.");
                    break;
                case "c":
                    running = false;
                    break;
                case "d":
                    storedMessagesMenu(input);
                    break;
                default:
                    System.out.println("Invalid option. Please choose a, b, c, or d.");
            }
        }

        // Final session summary
        Message session = new Message("00", 0, "+0000000000", "session end");
        System.out.println(session.printMessages());
        System.out.println("Total messages sent this session: " + session.returnTotalMessagess());
        System.out.println("Thank you for using QuickChat. Goodbye!");
        input.close();
    }

    // ─── Send message flow ────────────────────────────────────────────────────
    private static void sendMessageFlow(Scanner input) {
        int numToSend = 0;
        while (numToSend <= 0) {
            System.out.print("\nHow many messages do you want to send? ");
            try {
                numToSend = Integer.parseInt(input.nextLine().trim());
                if (numToSend <= 0) System.out.println("Please enter a positive number.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }

        for (int i = 0; i < numToSend; i++) {
            System.out.println("\n=== Message " + (i + 1) + " of " + numToSend + " ===");

            // Message ID
            String messageID = "";
            boolean validID = false;
            while (!validID) {
                System.out.print("Enter Message ID (max 10 characters): ");
                messageID = input.nextLine().trim();
                Message temp = new Message(messageID, 0, "+0000000000", "test");
                if (temp.checkMessageID()) {
                    System.out.println("Message ID generated: " + messageID);
                    validID = true;
                } else {
                    System.out.println("Message ID must not exceed 10 characters. Try again.");
                }
            }

            // Recipient
            String recipient = "";
            boolean validCell = false;
            while (!validCell) {
                System.out.print("Enter recipient cell number (max 10 chars, must start with +): ");
                recipient = input.nextLine().trim();
                Message temp = new Message(messageID, 0, recipient, "test");
                String cellCheck = temp.checkRecipientCell();
                System.out.println(cellCheck);
                if (cellCheck.startsWith("Cell phone number successfully")) validCell = true;
            }

            // Message body
            String messageBody = "";
            boolean validMessage = false;
            while (!validMessage) {
                System.out.print("Enter your message (max 250 characters): ");
                messageBody = input.nextLine().trim();
                if (messageBody.length() <= 250) {
                    System.out.println("Message sent.");
                    validMessage = true;
                } else {
                    int over = messageBody.length() - 250;
                    System.out.println("Message exceeds 250 characters by " + over + "; please reduce the size.");
                }
            }

            // Build message object
            Message msg = new Message(messageID, 0, recipient, messageBody);
            System.out.println("Message Hash: " + msg.getMessageHash());

            // Let user decide
            String result = msg.SentMessage();
            System.out.println(result);

            // Add to the correct array in manager
            String[] row = { msg.getMessageID(), msg.getMessageHash(), msg.getRecipient(), msg.getMessage() };
            if (result.equals("Message successfully sent.")) {
                manager.addSentMessage(row);
            } else if (result.equals("Press 0 to delete the message.")) {
                manager.addDisregardedMessage(row);
           } else if (result.equals("Message successfully stored.")) {
    manager.loadStoredMessages();
}

            // Show details
            System.out.println("\n--- Message Details ---");
            System.out.println("Message ID   : " + msg.getMessageID());
            System.out.println("Message Hash : " + msg.getMessageHash());
            System.out.println("Recipient    : " + msg.getRecipient());
            System.out.println("Message      : " + msg.getMessage());
            System.out.println("-----------------------");
        }
    }

    // ─── Stored Messages sub-menu (Part 3) ───────────────────────────────────
    private static void storedMessagesMenu(Scanner input) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Stored Messages Menu ---");
            System.out.println("a) Display sender and recipient of all stored messages");
            System.out.println("b) Display the longest stored message");
            System.out.println("c) Search for a message ID");
            System.out.println("d) Search messages for a particular recipient");
            System.out.println("e) Delete a message using its hash");
            System.out.println("f) Display full report");
            System.out.println("x) Back to main menu");
            System.out.print("Choose an option: ");

            String choice = input.nextLine().trim().toLowerCase();

            switch (choice) {
                case "a":
                    System.out.println(manager.displayStoredSenderRecipient());
                    break;
                case "b":
                    System.out.println(manager.displayLongestMessage());
                    break;
                case "c":
                    System.out.print("Enter Message ID to search: ");
                    String id = input.nextLine().trim();
                    System.out.println(manager.searchByMessageID(id));
                    break;
                case "d":
                    System.out.print("Enter recipient number to search: ");
                    String recipient = input.nextLine().trim();
                    System.out.println(manager.searchByRecipient(recipient));
                    break;
                case "e":
                    System.out.print("Enter message hash to delete: ");
                    String hash = input.nextLine().trim();
                    System.out.println(manager.deleteByHash(hash));
                    break;
                case "f":
                    System.out.println(manager.displayReport());
                    break;
                case "x":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    // ─── Getter so tests can access the shared manager ────────────────────────
    public static MessageManager getManager() { return manager; }
}