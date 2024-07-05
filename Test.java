import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Test implements Runnable {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 5555;
    private String clientName;
    private String option;
    private int clientId;
    private int seat;

    public Test(String clientName, String option, int clientId, int seat) {
        this.clientName = clientName;
        this.option = option;
        this.clientId = clientId;
        this.seat = seat;
    }

    public static void main(String[] args) {
        Thread client1 = new Thread(new Test("Client1", "makeReservation", 1, 1));
        Thread client5 = new Thread(new Test("Client5", "queryReservation", 5, 0));
        Thread client2 = new Thread(new Test("Client2", "makeReservation", 2, 1));
        Thread client3 = new Thread(new Test("Client3", "makeReservation", 3, 2));
        
        Thread client6 = new Thread(new Test("Client6", "queryReservation", 6, 0));
        
        Thread client7 = new Thread(new Test("Client7", "cancelReservation", 7, 0));
        Thread client8 = new Thread(new Test("Client8", "cancelReservation", 8, 1));
        
        client1.start();
        client5.start();
        client2.start();
        client3.start();
        
        client6.start();
        
        client7.start();
        client8.start();
    }

    @Override
    public void run() {
        sendOperation(clientName, option, clientId, seat);
    }

    private void sendOperation(String clientName, String option, int clientId, int seat) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            // Server'a isteği gönder
            out.println(option + ":" + clientId + (option.equalsIgnoreCase("makeReservation") ? ":" + seat : ""));

            // Server'dan gelen cevabı oku ve yazdır
            String response;
            while ((response = in.readLine()) != null && !response.isEmpty()) {
                // Burada server'dan gelen bilgileri istemci tarafında yazdırmamamız gerek
                // System.out.println(clientName + " - Server response: " + response); 
            }

        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + SERVER_ADDRESS);
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
