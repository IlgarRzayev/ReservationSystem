import java.io.*;
import java.net.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class ReservationSystem {
    private int[] seats = new int[5];
    private HashMap<Integer, Integer> reservations = new HashMap<>();
    private final Lock lock = new ReentrantLock();

    public String queryReservation(int clientId) {
        StringBuilder status = new StringBuilder();
        lock.lock();
        try {
            status.append("Client[").append(clientId).append("] looks for available seats. State of the seats are:\n");
            for (int j = 0; j < seats.length; j++) {
                status.append("Seat No ").append(j + 1).append(" : ").append(seats[j]).append("\n");
            }
        } finally {
            lock.unlock();
        }
        return status.toString();
    }

    public String makeReservation(int clientId, int seatNo) {
        StringBuilder status = new StringBuilder();
        lock.lock();
        try {
            status.append("Client[").append(clientId).append("] tries to book the. Seat No: ").append(seatNo).append("\n");
            if (seatNo < 1 || seatNo > seats.length) {
                status.append("Invalid seat number.");
            } else if (seats[seatNo - 1] == 0) {
                seats[seatNo - 1] = 1;
                reservations.put(clientId, seatNo);
                status.append("Client[").append(clientId).append("] booked seat number [").append(seatNo).append("] successfully");
            } else {
                status.append("Client[").append(clientId).append("] could not book seat number [").append(seatNo).append("] since it has been already booked.");
            }
        } finally {
            lock.unlock();
        }
        return status.toString();
    }

    public String cancelReservation(int clientId) {
        StringBuilder status = new StringBuilder();
        lock.lock();
        try {
            Integer seatNo = reservations.get(clientId);
            status.append("Client[").append(clientId).append("] tries for cancel");
            if (seatNo != null) {
                status.append(". Seat No: ").append(seatNo).append("\n");
                seats[seatNo - 1] = 0;
                reservations.remove(clientId);
                status.append("Client[").append(clientId).append("]'s reservation for seat number [").append(seatNo).append("] has been canceled.");
            } else {
                status.append("\nClient[").append(clientId).append("] does not have a reservation to cancel.");
            }
        } finally {
            lock.unlock();
        }
        return status.toString();
    }
}

class ClientHandler extends Thread {
    private Socket clientSocket;
    private ReservationSystem system;
    private Instant time;

    public ClientHandler(Socket socket, ReservationSystem system) {
        this.clientSocket = socket;
        this.system = system;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String request;
            while ((request = in.readLine()) != null) {
                String[] parts = request.split(":");
                String action = parts[0];
                int clientId = Integer.parseInt(parts[1]);
                String response = "";

                if (action.equalsIgnoreCase("queryReservation")) {
                    response = system.queryReservation(clientId);
                } else if (action.equalsIgnoreCase("makeReservation")) {
                    int seatNo = Integer.parseInt(parts[2]);
                    response = system.makeReservation(clientId, seatNo);
                } else if (action.equalsIgnoreCase("cancelReservation")) {
                    response = system.cancelReservation(clientId);
                } else {
                    response = "Invalid action.";
                }

                // Log with timestamp only if it's a new action
                if (!response.isEmpty()) {
                    time = Instant.now();
                    System.out.println("TimeStamp : " + time + "\t" + response);
                }

                // Send response to the client
                out.println(response);
                out.flush();

                // Sleep to simulate delay
                Thread.sleep(300);
            }
        } catch (IOException | InterruptedException e) {
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

public class Server {
    private static final int PORT = 5555;

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
