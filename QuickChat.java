import java.util.Scanner;
 
/**
 * QuickChat – Main entry point for Part 2.
 *
 * Flow:
 *   1. User logs in via LoginTest.login()
 *   2. Welcome screen is shown
 *   3. Menu loop:
 *        a) Send Messages
 *        b) Show recently sent messages  (Coming Soon)
 *        c) Quit
 */
public class QuickChat {
 
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
 
        // ── Step 1: Login ──────────────────────────────────────────────────────
        System.out.println("========================================");
        System.out.println("         Welcome to QuickChat           ");
        System.out.println("========================================");
 
        String username = LoginTest.login(input);
 
        if (username == null) {
            System.out.println("Login failed. Exiting.");
            input.close();
            return;
        }
 
        // ── Step 2: Welcome screen ─────────────────────────────────────────────
        Welcome.show(username);
 
        // ── Step 3: Menu loop ──────────────────────────────────────────────────
        boolean running = true;
        while (running) {
            System.out.println("\n--- QuickChat Menu ---");
            System.out.println("a) Send Messages");
            System.out.println("b) Show recently sent messages");
            System.out.println("c) Quit");
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
 
                default:
                    System.out.println("Invalid option. Please choose a, b, or c.");
            }
        }
 
        // ── Final summary ──────────────────────────────────────────────────────
        // Create a dummy Message just to call the static print/count methods
        Message session = new Message("00", 0, "+0000000000", "session end");
        System.out.println(session.printMessages());
        System.out.println("Total messages sent this session: "
                + session.returnTotalMessagess());
        System.out.println("Thank you for using QuickChat. Goodbye!");
        input.close();
    }
 
    // ── Send-message sub-flow ──────────────────────────────────────────────────
    private static void sendMessageFlow(Scanner input) {
 
        // Ask how many messages the user wants to send
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
 
            // ── Message ID ────────────────────────────────────────────────────
            String messageID = "";
            boolean validID = false;
            while (!validID) {
                System.out.print("Enter Message ID (max 10 characters): ");
                messageID = input.nextLine().trim();
 
                // Use a temporary Message object just to validate the ID
                Message temp = new Message(messageID, 0, "+0000000000", "test");
                if (temp.checkMessageID()) {
                    System.out.println("Message ID generated: " + messageID);
                    validID = true;
                } else {
                    System.out.println("Message ID must not exceed 10 characters. Try again.");
                }
            }
 
            // ── Recipient cell number ─────────────────────────────────────────
            String recipient = "";
            boolean validCell = false;
            while (!validCell) {
                System.out.print("Enter recipient cell number (max 10 chars, must start with +): ");
                recipient = input.nextLine().trim();
 
                Message temp = new Message(messageID, 0, recipient, "test");
                String cellCheck = temp.checkRecipientCell();
                System.out.println(cellCheck);
                if (cellCheck.startsWith("Cell phone number successfully")) {
                    validCell = true;
                }
            }
 
            // ── Message body ──────────────────────────────────────────────────
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
                    System.out.println("Please enter a message of less than 250 characters.");
                    System.out.println("Message exceeds 250 characters by " + over
                            + "; please reduce the size.");
                }
            }
 
            // ── Build Message object ──────────────────────────────────────────
            // numMessagesSent starts at current total before this message is sent
            Message msg = new Message(messageID, 0, recipient, messageBody);
 
            System.out.println("Message Hash: " + msg.getMessageHash());
 
            // ── Let user decide: send / disregard / store ─────────────────────
            String result = msg.SentMessage();
            System.out.println(result);
 
            // ── Show full message details after action ────────────────────────
            System.out.println("\n--- Message Details ---");
            System.out.println("Message ID   : " + msg.getMessageID());
            System.out.println("Message Hash : " + msg.getMessageHash());
            System.out.println("Recipient    : " + msg.getRecipient());
            System.out.println("Message      : " + msg.getMessage());
            System.out.println("-----------------------");
        }
    }
}
 

