import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class FileServer {
    private static final String SERVER_DIR = "server_files/";
    private static final String CRC_DIVISOR = "1101";

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            System.out.println("Server started...");

            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    new Thread(new ClientHandler(socket)).start();
                } catch (IOException e) {
                    System.err.println("Error handling client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                // Authentication phase
                out.println("auth");
                out.println("Enter password:");
                String password = in.readLine();
                if (!"12345".equals(password)) {
                    out.println("Authentication Failed");
                    return;
                }
                out.println("Authenticated");

                // Handle client commands
                String clientCommand;
                while ((clientCommand = in.readLine()) != null) {
                    if (clientCommand.startsWith("get")) {
                        String filename = clientCommand.split(" ")[1];
                        File file = new File(SERVER_DIR + filename);
                        if (file.exists()) {
                            out.println("Sending file...");
                            sendFile(file, out);
                        } else {
                            out.println("File not found.");
                        }
                    } else if (clientCommand.startsWith("put")) {
                        String filename = clientCommand.split(" ")[1];
                        receiveFile(filename, in);
                        out.println("File uploaded.");
                    } else if (clientCommand.startsWith("crc")) {
                        String data = in.readLine();
                        String crc = CRCUtils.calculateCRC(data, CRC_DIVISOR);
                        out.println("CRC: " + crc);
                    } else if ("quit".equals(clientCommand)) {
                        out.println("Connection closed.");
                        break;
                    } else {
                        out.println("Invalid command.");
                    }
                }
            } catch (IOException e) {
                System.err.println("Error handling client: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Error closing client socket: " + e.getMessage());
                }
            }
        }

        private void sendFile(File file, PrintWriter out) throws IOException {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    out.println(line);
                }
                out.println("EOF");
            }
        }

        private void receiveFile(String filename, BufferedReader in) throws IOException {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(SERVER_DIR + filename))) {
                String line;
                while (!(line = in.readLine()).equals("EOF")) {
                    writer.write(line);
                    writer.newLine();
                }
            }
        }
    }
}
