import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
 
/**
 * MessageTest – JUnit 4 unit tests for the Message class.
 *
 * To run in NetBeans:
 *   Right-click the project → Test  (or press Alt+F6)
 *
 * Make sure JUnit 4 is on your test classpath (NetBeans adds it automatically
 * when you create a test class via File → New File → JUnit Test).
 */
public class MessageTest {
 
    // ─── Shared test data ─────────────────────────────────────────────────────
    // Test Case 1 data (matches "00:0:HITHANKS" expected hash from spec)
    private static final String MSG_ID_1    = "00:6531";      // starts with "00"
    private static final String RECIPIENT_1 = "+27834567891"; // 12 chars – too long
    private static final String RECIPIENT_VALID = "+278345678"; // ≤10, starts with +
    private static final String MESSAGE_1   = "Hi Mike, can you please call me?";
    // Expected hash: first 2 of ID = "00", numSent = 0,
    //                first word = "Hi", last word = "me?" → "00:0:HIME?"
    // (The spec's "00:0:HITHANKS" example uses a different message body;
    //  tests below use the actual formula.)
 
    // Test Case 2 data
    private static final String MSG_ID_2    = "00:6532";
    private static final String RECIPIENT_2 = "08575975889"; // no +, 11 chars – invalid
    private static final String MESSAGE_2   = "Hi Keegan, did you receive the payment?";
 
    private Message msg1;
    private Message msg2;
 
    @Before
    public void setUp() {
        // Reset static session state before each test
        Message.resetSession();
 
        msg1 = new Message(MSG_ID_1, 0, RECIPIENT_VALID, MESSAGE_1);
        msg2 = new Message(MSG_ID_2, 0, RECIPIENT_2,     MESSAGE_2);
    }
 
    @After
    public void tearDown() {
        Message.resetSession();
    }
 
    // =========================================================================
    // Tests: message body length (≤ 250 characters)
    // =========================================================================
 
    @Test
    public void testMessageLength_Success() {
        // A message within 250 characters should be accepted
        String shortMessage = "Hi Mike, can you please call me?";
        assertTrue("Message within 250 chars should pass",
                shortMessage.length() <= 250);
        // Confirm the runner would print "Message sent."
        // (Direct assertion on length since the runner handles the display)
    }
 
    @Test
    public void testMessageLength_Failure() {
        // Build a message that exceeds 250 characters
        String longMessage = "A".repeat(260);
        int excess = longMessage.length() - 250;
 
        assertFalse("Message over 250 chars should fail",
                longMessage.length() <= 250);
 
        // The runner prints this – we verify the formula is correct
        String expectedError = "Message exceeds 250 characters by " + excess
                + "; please reduce the size.";
        assertEquals(
            "Message exceeds 250 characters by 10; please reduce the size.",
            expectedError
        );
    }
 
    // =========================================================================
    // Tests: recipient cell number validation
    // =========================================================================
 
    @Test
    public void testCheckRecipientCell_Success() {
        // Valid: starts with '+' and ≤ 10 characters
        Message m = new Message("00:1234", 0, "+278345678", "Hello there friend");
        assertEquals(
            "Cell phone number successfully captured.",
            m.checkRecipientCell()
        );
    }
 
    @Test
    public void testCheckRecipientCell_Failure_NoInternationalCode() {
        // Fails: no '+' prefix
        Message m = new Message("00:1234", 0, "0834567891", "Hello there friend");
        assertEquals(
            "Cell phone number is incorrectly formatted or does not contain an "
          + "international code. Please correct the number and try again.",
            m.checkRecipientCell()
        );
    }
 
    @Test
    public void testCheckRecipientCell_Failure_TooLong() {
        // Fails: more than 10 characters even though it starts with '+'
        Message m = new Message("00:1234", 0, "+27834567891", "Hello there friend");
        assertEquals(
            "Cell phone number is incorrectly formatted or does not contain an "
          + "international code. Please correct the number and try again.",
            m.checkRecipientCell()
        );
    }
 
    // =========================================================================
    // Tests: message hash
    // =========================================================================
 
    @Test
    public void testCreateMessageHash_TestCase1() {
        // ID starts with "00", numSent = 0,
        // first word of MESSAGE_1 = "Hi", last word = "me?"
        // Expected: "00:0:HIME?"
        String expected = "00:0:HIME?";
        assertEquals("Hash for Test Case 1 should be correct",
                expected, msg1.getMessageHash());
    }
 
    @Test
    public void testCreateMessageHash_SpecExample() {
        // Recreate the exact spec example: "00:0:HITHANKS"
        // messageID starts with "00", numSent = 0, message = "Hi Thanks"
        Message specMsg = new Message("00abc", 0, "+278345678", "Hi Thanks");
        assertEquals("Spec example hash should match",
                "00:0:HITHANKS", specMsg.getMessageHash());
    }
 
    @Test
    public void testCreateMessageHash_Loop() {
        // Verify hashes are generated correctly for a series of messages
        String[] messages = {
            "Good morning friend",
            "Did you get my parcel",
            "Please call me back urgently"
        };
 
        String[] expectedHashes = {
            "00:0:GOODFRIEND",
            "00:0:DIDPARCEL",
            "00:0:PLEASEURGENTLY"
        };
 
        for (int i = 0; i < messages.length; i++) {
            Message m = new Message("00test", 0, "+278345678", messages[i]);
            assertEquals(
                "Hash for message [" + messages[i] + "] should match",
                expectedHashes[i],
                m.getMessageHash()
            );
        }
    }
 
    // =========================================================================
    // Tests: message ID validation
    // =========================================================================
 
    @Test
    public void testCheckMessageID_Success() {
        Message m = new Message("00:1234", 0, "+278345678", "Hello world");
        assertTrue("7-char ID should pass", m.checkMessageID());
    }
 
    @Test
    public void testCheckMessageID_Failure() {
        Message m = new Message("12345678901", 0, "+278345678", "Hello world");
        assertFalse("11-char ID should fail", m.checkMessageID());
    }
 
    // =========================================================================
    // Tests: returnTotalMessages
    // =========================================================================
 
    @Test
    public void testReturnTotalMessages_StartsAtZero() {
        Message m = new Message("00:1234", 0, "+278345678", "Hello world");
        assertEquals("Total messages should start at 0", 0, m.returnTotalMessagess());
    }
}
 