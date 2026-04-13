1import java.util.*;
import java.io.*;

public class part_1 {
    public static void main(String[] args) throws Exception {
        Scanner in = new Scanner(System.in);
        FileWriter fw = new FileWriter("users.txt", true);
        BufferedReader br = new BufferedReader(new FileReader("users.txt"));

        System.out.println("1. Register\n2. Login");
        int choice = in.nextInt(); in.nextLine();

        if (choice == 1) {
            System.out.print("Username: ");
            String u = in.nextLine();
            System.out.print("Password: ");
            String p = in.nextLine();
            System.out.print("Cellphone: ");
            String c = in.nextLine();
            fw.write(u + "," + p + "," + c + "\n");
            fw.close();
            System.out.println("Registered!");
        } else {
            System.out.print("Username: ");
            String u = in.nextLine();
            System.out.print("Password: ");
            String p = in.nextLine();
            String line;
            boolean found = false;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data[0].equals(u) && data[1].equals(p)) found = true;
            }
            System.out.println(found ? "Login success!" : "Login failed.");
        }
    }
}
