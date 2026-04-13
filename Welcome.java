public class Welcome {
    public static void show(String username) {
        System.out.println("====================================");
        System.out.println(" Welcome, " + username + "!");
        System.out.println(" You have successfully logged in.");
        System.out.println("====================================");
    }

    public static void main(String[] args) {
        // Test the welcome screen with a sample username
        show("_Mash");
    }
}
