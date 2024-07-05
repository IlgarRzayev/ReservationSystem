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
        System.out.println(status.toString());
        return status.toString();
    }

    public String makeReservation(int clientId, int seatNo) {
        lock.lock();
        try {
            if (seatNo < 1 || seatNo > seats.length) {
                return "Invalid seat number.";
            }
            if (seats[seatNo - 1] == 0) {
                seats[seatNo - 1] = 1;
                reservations.put(clientId, seatNo);
                return "Client[" + clientId + "] booked seat number [" + seatNo + "] successfully";
            } else {
                return "Client[" + clientId + "] could not book seat number [" + seatNo + "] since it has been already booked.";
            }
        } finally {
            lock.unlock();
        }
    }

    public String cancelReservation(int clientId) {
        lock.lock();
        try {
            Integer seatNo = reservations.remove(clientId);
            if (seatNo != null) {
                seats[seatNo - 1] = 0;
                return "Client[" + clientId + "]'s reservation for seat number [" + seatNo + "] has been canceled.";
            } else {
                return "Client[" + clientId + "] does not have a reservation to cancel.";
            }
        } finally {
            lock.unlock();
        }
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
                Server.sleepUntil(300);
                if (action.equalsIgnoreCase("queryReservation")) {
                    response = system.queryReservation(clientId);
                    time = Instant.now();
                    System.out.println(
                            "TimeStamp : " + time + String.valueOf(System.nanoTime()) + "\t" + response);
                } else if (action.equalsIgnoreCase("makeReservation")) {
                    int seatNo = Integer.parseInt(parts[2]);
                    System.out.printf("Client[%d] tries for makeReservation\n", clientId);
                    response = system.makeReservation(clientId, seatNo);
                    time = Instant.now();
                    System.out.println(
                            "TimeStamp : " + time + String.valueOf(System.nanoTime()) + "\t" + response);
                } else if (action.equalsIgnoreCase("cancelReservation")) {
                    System.out.printf("Client[%d] tries for cancelReservation\n", clientId);
                    response = system.cancelReservation(clientId);
                    time = Instant.now();
                    System.out.println(
                            "TimeStamp : " + time + String.valueOf(System.nanoTime()) + "\t" + response);
                } else {
                    response = "Invalid action.";
                }
                out.println(response);
                out.flush();
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

public class Server {
    private static final int PORT = 5555;

    public static void sleepUntil(long milliSeconds) {
        Instant nanoTime = Instant.ofEpochMilli(System.currentTimeMillis()).plusMillis(milliSeconds)
                .plusNanos(-System.currentTimeMillis());
        System.out.println("NanoTime : " + nanoTime);
        while (System.currentTimeMillis() <= nanoTime.toEpochMilli()) {
            while (Instant.ofEpochMilli(System.currentTimeMillis()).getNano() <= nanoTime.getNano()) {
                ;
            }

        }
        return;
    }

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
