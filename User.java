import java.io.*;
import java.util.Scanner;

public class User {
    private static File userFile = new File("users.txt");

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        createFileIfMissing();

        System.out.println("1. Register\n2. Login");
        int choice = input.nextInt();
        input.nextLine(); // clear newline

        if (choice == 1) {
            register(input);
        } else if (choice == 2) {
            String username = login(input);
            if (username != null) {
                Welcome.show(username);
            }
        } else {
            System.out.println("Invalid choice.");
        }

        input.close();
    }

    public static void createFileIfMissing() {
        try {
            userFile.createNewFile();
        } catch (IOException e) {
            System.out.println("File error.");
        }
    }

    public static void register(Scanner input) {
        System.out.print("Enter username: ");
        String username = input.nextLine().trim();

        System.out.print("Enter password: ");
        String password = input.nextLine().trim();

        System.out.print("Enter cellphone number: ");
        String cellphone = input.nextLine().trim();

        if (!cellphone.matches("^0\\d{9}$")) {
            System.out.println("Invalid SA cellphone number.");
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(userFile, true))) {
            writer.write(username + "," + password + "," + cellphone);
            writer.newLine();
            System.out.println("Registration successful!");
        } catch (IOException e) {
            System.out.println("Could not write to file.");
        }
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
