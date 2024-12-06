import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class FileClient {
    private static final String CLIENT_DIR = "client_files/";

    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 5000);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println(in.readLine());
            out.println("auth");
            System.out.println(in.readLine());
            String password = scanner.nextLine();
            out.println(password);

            if (!in.readLine().equals("Authenticated")) {
                System.out.println("Authentication Failed");
                return;
            }

            while (true) {
                System.out.println("Enter command (get/put/crc/quit):");
                String command = scanner.nextLine();
                out.println(command);

                if (command.equals("quit")) {
                    System.out.println(in.readLine());
                    break;
                } else if (command.startsWith("get")) {
                    String filename = command.split(" ")[1];
                    receiveFile(filename, in);
                } else if (command.startsWith("put")) {
                    String filename = command.split(" ")[1];
                    sendFile(filename, out);
                } else if (command.startsWith("crc")) {
                    System.out.println("Enter data:");
                    String data = scanner.nextLine();
                    out.println(data);
                    System.out.println("Server CRC: " + in.readLine());
                }
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void sendFile(String filename, PrintWriter out) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(CLIENT_DIR + filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                out.println(line);
            }
            out.println("EOF");
        }
    }

    private static void receiveFile(String filename, BufferedReader in) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CLIENT_DIR + filename))) {
            String line;
            while (!(line = in.readLine()).equals("EOF")) {
                writer.write(line);
                writer.newLine();
            }
        }
    }
}
