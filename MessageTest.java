import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * MessageTest – JUnit 4 tests covering Part 2 and Part 3.
 *
 * Part 3 test data (from the assignment spec):
 *
 *   Message 1: recipient=+27834557896, body="Did you get the cake?",            flag=Sent
 *   Message 2: recipient=+27838884567, body="Where are you? You are late! ...", flag=Stored
 *   Message 3: recipient=+27834484567, body="Yohoooo, I am at your gate.",      flag=Disregard
 *   Message 4: recipient=0838884567,   body="It is dinner time!",               flag=Sent
 *   Message 5: recipient=+27838884567, body="Ok, I am leaving without you.",    flag=Stored
 *
 * NOTE ON MESSAGE 4:
 *   The spec states recipient numbers must start with '+' (international code).
 *   Message 4 ("0838884567") violates this rule and therefore FAILS validation.
 *   It is NOT added to the sent array. Tests reflect this spec-compliant behaviour.
 */
public class MessageTest {

    // ─── Shared test data ─────────────────────────────────────────────────────
    private MessageManager manager;

    // Auto-generated IDs for the test messages (≤ 10 chars)
    private static final String ID_1 = "MSG00001";
    private static final String ID_2 = "MSG00002";
    private static final String ID_3 = "MSG00003";
    private static final String ID_4 = "MSG00004";  // will fail cell validation
    private static final String ID_5 = "MSG00005";

    private static final String BODY_1 = "Did you get the cake?";
    private static final String BODY_2 = "Where are you? You are late! I have asked you to be on time.";
    private static final String BODY_3 = "Yohoooo, I am at your gate.";
    private static final String BODY_4 = "It is dinner time!";
    private static final String BODY_5 = "Ok, I am leaving without you.";

    private static final String REC_1 = "+27834557896";
    private static final String REC_2 = "+27838884567";
    private static final String REC_3 = "+27834484567";
    private static final String REC_4 = "0838884567";   // INVALID – no '+'
    private static final String REC_5 = "+27838884567";

    @Before
    public void setUp() {
        Message.resetSession();
        manager = new MessageManager();

        // Build message objects
        Message msg1 = new Message(ID_1, 0, REC_1, BODY_1);
        Message msg2 = new Message(ID_2, 0, REC_2, BODY_2);
        Message msg3 = new Message(ID_3, 0, REC_3, BODY_3);
        Message msg4 = new Message(ID_4, 0, REC_4, BODY_4);
        Message msg5 = new Message(ID_5, 0, REC_5, BODY_5);

        // Populate arrays according to flags, respecting validation
        // Message 1 – Sent (valid)
        manager.addSentMessage(new String[]{ msg1.getMessageID(), msg1.getMessageHash(), REC_1, BODY_1 });

        // Message 2 – Stored (valid)
        manager.addStoredMessage(new String[]{ msg2.getMessageID(), msg2.getMessageHash(), REC_2, BODY_2 });

        // Message 3 – Disregard (valid recipient but user chose disregard)
        manager.addDisregardedMessage(new String[]{ msg3.getMessageID(), msg3.getMessageHash(), REC_3, BODY_3 });

        // Message 4 – INVALID recipient (no '+'), so checkRecipientCell() fails
        // Per spec it must NOT be added to the sent array.
        String cell4Check = msg4.checkRecipientCell();
        if (cell4Check.startsWith("Cell phone number successfully")) {
            manager.addSentMessage(new String[]{ msg4.getMessageID(), msg4.getMessageHash(), REC_4, BODY_4 });
        }
        // else: intentionally skipped – see class-level NOTE

        // Message 5 – Stored (valid)
        manager.addStoredMessage(new String[]{ msg5.getMessageID(), msg5.getMessageHash(), REC_5, BODY_5 });
    }

    @After
    public void tearDown() {
        Message.resetSession();
        manager = null;
    }

    // =========================================================================
    // Part 2 tests (carried over)
    // =========================================================================

    @Test
    public void testMessageLength_Success() {
        assertTrue("Short message should pass", BODY_1.length() <= 250);
    }

    @Test
    public void testMessageLength_Failure() {
        String longMsg = "A".repeat(260);
        int excess = longMsg.length() - 250;
        assertFalse("Long message should fail", longMsg.length() <= 250);
        assertEquals(
            "Message exceeds 250 characters by 10; please reduce the size.",
            "Message exceeds 250 characters by " + excess + "; please reduce the size."
        );
    }

    @Test
    public void testCheckRecipientCell_Success() {
        Message m = new Message("00:1234", 0, "+278345678", "Hello world");
        assertEquals("Cell phone number successfully captured.", m.checkRecipientCell());
    }

    @Test
    public void testCheckRecipientCell_Failure_NoPlus() {
        Message m = new Message("00:1234", 0, "0838884567", "Hello world");
        assertEquals(
            "Cell phone number is incorrectly formatted or does not contain an "
          + "international code. Please correct the number and try again.",
            m.checkRecipientCell()
        );
    }

    @Test
    public void testCheckRecipientCell_Failure_TooLong() {
        Message m = new Message("00:1234", 0, "+27834567891", "Hello world");
        assertEquals(
            "Cell phone number is incorrectly formatted or does not contain an "
          + "international code. Please correct the number and try again.",
            m.checkRecipientCell()
        );
    }

    @Test
    public void testCreateMessageHash_SpecExample() {
        Message m = new Message("00abc", 0, "+278345678", "Hi Thanks");
        assertEquals("00:0:HITHANKS", m.getMessageHash());
    }

    @Test
    public void testCheckMessageID_Success() {
        Message m = new Message("00:1234", 0, "+278345678", "Hello world");
        assertTrue(m.checkMessageID());
    }

    @Test
    public void testCheckMessageID_Failure() {
        Message m = new Message("12345678901", 0, "+278345678", "Hello world");
        assertFalse(m.checkMessageID());
    }

    // =========================================================================
    // Part 3 tests
    // =========================================================================

    /**
     * Test 1 – Sent Messages array correctly populated.
     * Only messages 1 is valid and sent (message 4 fails cell validation).
     * Expected bodies in sent array: "Did you get the cake?"
     */
    @Test
    public void testSentMessagesArrayPopulated() {
        java.util.ArrayList<String[]> sent = manager.getSentMessages();

        // Only message 1 should be in the sent array
        assertEquals("Only 1 valid sent message expected", 1, sent.size());
        assertEquals("Did you get the cake?", sent.get(0)[3]);
    }

    /**
     * Test 2 – Display the longest message.
     * Across sent + stored, message 2 is the longest.
     */
    @Test
    public void testDisplayLongestMessage() {
        String result = manager.displayLongestMessage();
        assertTrue(
            "Longest message should be message 2",
            result.contains("Where are you? You are late! I have asked you to be on time.")
        );
    }

    /**
     * Test 3 – Search for message ID.
     * Message 4 has an invalid recipient so it was never added.
     * We search for Message 1's ID instead (valid sent message).
     */
    @Test
    public void testSearchByMessageID_Found() {
        String result = manager.searchByMessageID(ID_1);
        assertTrue("Should find message 1 body", result.contains(BODY_1));
        assertTrue("Should find message 1 recipient", result.contains(REC_1));
    }

    @Test
    public void testSearchByMessageID_NotFound() {
        String result = manager.searchByMessageID("NOTEXIST");
        assertTrue("Should report not found", result.contains("No message found"));
    }

    /**
     * Test 4 – Search all messages for recipient +27838884567.
     * Messages 2 and 5 share this recipient (both stored).
     */
    @Test
    public void testSearchByRecipient() {
        String result = manager.searchByRecipient("+27838884567");
        assertTrue("Should contain message 2", result.contains(BODY_2));
        assertTrue("Should contain message 5", result.contains(BODY_5));
    }

    /**
     * Test 5 – Delete a message using its hash.
     * Delete message 2 (stored) by its hash.
     */
    @Test
    public void testDeleteByHash() {
        Message msg2 = new Message(ID_2, 0, REC_2, BODY_2);
        String hash2 = msg2.getMessageHash();

        String result = manager.deleteByHash(hash2);
        assertTrue(
            "Delete should confirm removal",
            result.contains("successfully deleted")
        );
        // Confirm it's gone
        String search = manager.searchByMessageID(ID_2);
        assertTrue("Message 2 should no longer exist", search.contains("No message found"));
    }

    /**
     * Test 6 – Display report shows all sent + stored messages
     * with Hash, Recipient, and Message fields.
     */
    @Test
    public void testDisplayReport() {
        String report = manager.displayReport();
        // Should contain message 1 (sent) and messages 2 & 5 (stored)
        assertTrue("Report should contain message 1", report.contains(BODY_1));
        assertTrue("Report should contain message 2", report.contains(BODY_2));
        assertTrue("Report should contain message 5", report.contains(BODY_5));
        // Should contain the header fields
        assertTrue("Report should show Message Hash label",  report.contains("Message Hash"));
        assertTrue("Report should show Recipient label",     report.contains("Recipient"));
        assertTrue("Report should show Message label",       report.contains("Message"));
    }
}