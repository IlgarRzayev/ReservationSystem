import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class client_asenkron {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            while (true) {
                System.out.println("Enter your client ID:");
                int clientId = scanner.nextInt();
                scanner.nextLine(); // Yeni satır karakterini tüketir.

                boolean keepGoing = true;
                while (keepGoing) {
                    System.out.println("Select an option:\n1. Query Reservation\n2. Make Reservation\n3. Cancel Reservation");
                    int option = scanner.nextInt();
                    scanner.nextLine(); // Yeni satır karakterini tüketir.

                    if (option == 1) {
                        out.println("query:" + clientId);
                    } else if (option == 2) {
                        System.out.println("Enter seat number to reserve:");
                        int seatNo = scanner.nextInt();
                        scanner.nextLine(); 
                        out.println("reserve:" + clientId + ":" + seatNo);
                    } else if (option == 3) {
                        out.println("remove:" + clientId);
                    } else {
                        System.out.println("Invalid option. Try again.");
                        continue; // Geçersiz seçenek durumunda başa dön
                    }

                    String response;
                    while ((response = in.readLine()) != null && !response.isEmpty()) {
                        System.out.println(response);
                    }

                    System.out.println("Do you want to perform another action? (yes/no)");
                    String anotherAction = scanner.nextLine().trim().toLowerCase();
                    if (!anotherAction.equals("yes")) {
                        keepGoing = false; // Kullanıcı başka bir işlem yapmak istemiyorsa iç döngüden çık
                    }
                }

                System.out.println("Do you want to enter a new client ID? (yes/no)");
                String newClientId = scanner.nextLine().trim().toLowerCase();
                if (!newClientId.equals("yes")) {
                    break; // Kullanıcı yeni bir client ID girmek istemiyorsa dış döngüden çık
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
