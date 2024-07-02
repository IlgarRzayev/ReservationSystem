import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.NoSuchElementException;

class ReservationSystem {
    private int[] seats = new int[5]; 
    private HashMap<Integer, Integer> reservations = new HashMap<>();

    public synchronized String queryReservation(int i) {
        StringBuilder status = new StringBuilder();
        status.append("Reader[").append(i).append("] looks for available seats. State of the seats are:\n");
        for (int j = 0; j < seats.length; j++) {
            status.append("Seat No ").append(j + 1).append(" : ").append(seats[j]).append("\n");
        }
        return status.toString();
    }

    public synchronized String makeReservation(int i, int seatNo) {
        if (seatNo < 1 || seatNo > seats.length) {
            return "Invalid seat number.";
        }
        if (seats[seatNo - 1] == 0) {
            seats[seatNo - 1] = 1;
            reservations.put(i, seatNo);
            return "Writer[" + i + "] booked seat number [" + seatNo + "] successfully";
        } else {
            return "Writer[" + i + "] could not book seat number [" + seatNo + "] since it has been already booked.";
        }
    }
}

class ClientHandler extends Thread {
    private Socket clientSocket;
    private ReservationSystem system;

    public ClientHandler(Socket socket, ReservationSystem system) {
        this.clientSocket = socket;
        this.system = system;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String request = in.readLine();
            String[] parts = request.split(":");
            String action = parts[0];
            int clientId = Integer.parseInt(parts[1]);

            if (action.equals("query")) {
                String response = system.queryReservation(clientId);
                out.println(response);
            } else if (action.equals("reserve")) {
                int seatNo = Integer.parseInt(parts[2]);
                String response = system.makeReservation(clientId, seatNo);
                out.println(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

public class server_asenkron {
    private static final int PORT = 12345;

    public static void main(String[] args) {
        ReservationSystem system = new ReservationSystem();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Reservation system server started...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());
                ClientHandler clientHandler = new ClientHandler(clientSocket, system);
                clientHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
