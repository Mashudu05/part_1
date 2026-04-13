import java.io.*;
import java.util.Scanner;

public class LoginTest {
    private static File userFile = new File("users.txt");

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        String username = login(input);
        if (username != null) {
            Welcome.show(username);
        }
        input.close();
    }

    public static String login(Scanner input) {
        System.out.print("Username: ");
        String username = input.nextLine().trim();

        System.out.print("Password: ");
        String password = input.nextLine().trim();

        try (BufferedReader reader = new BufferedReader(new FileReader(userFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] userData = line.trim().split(",");
                if (userData.length >= 2) {
                    String savedUsername = userData[0].trim();
                    String savedPassword = userData[1].trim();

                    if (savedUsername.equals(username) && savedPassword.equals(password)) {
                        System.out.println("Login successful.");
                        return username;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Login error.");
        }

        System.out.println("Incorrect username or password.");
        return null;
    }
}
