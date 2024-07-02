import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class client_asenkron {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String args[]){
        try(Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)
            ) {
            System.out.println("Enter your client ID:");
            int clientId = scanner.nextInt();
            scanner.nextLine(); // Yeni satır karakterini tüketir.

            while(true){
                System.out.println("Select an option:\n1. Query Reservation\n2. Make Reservation\n3. Cancel Reservation");
                int option = scanner.nextInt();
                scanner.nextLine(); // Yeni satır karakterini tüketir.

                if(option == 1){
                    out.println("query:" + clientId);
                    
                    String response;
                    while((response = in.readLine()) != null && !response.isEmpty()){
                        System.out.println(response); 
                    }
                    
                } else if(option == 2){
                    System.out.println("Enter seat number to reserve");
                    int seatNo = scanner.nextInt();
                    scanner.nextLine(); // Yeni satır karakterini tüketir.
                    out.println("reserve:" + clientId + ":" + seatNo);
                    
                    String response;
                    while((response = in.readLine()) != null && !response.isEmpty()){
                        System.out.println(response);                        
                    }
                    
                } else if(option == 3){
                    out.println("remove:" + clientId);
                    String response = in.readLine();
                    System.out.println(response);
                } else {
                    System.out.println("Invalid option. Try again");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
