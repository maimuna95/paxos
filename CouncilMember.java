import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * The CouncilMember class represents a member of a council participating in an election process.
 * Each member listens on a specific port, receives election-related messages, and votes for candidates.
 */
class CouncilMember implements Runnable {
    private final String name;
    private final int port;
    private final boolean delayed;
    private final boolean unreliable;
    private static final Map<String, Integer> votes = new ConcurrentHashMap<>();
    private static final List<String> logEntries = Collections.synchronizedList(new ArrayList<>());
    private final Set<String> votedCandidates = new HashSet<>();
    private ServerSocket serverSocket;
    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * Constructs a CouncilMember with specified parameters.
     *
     * @param name       the name of the council member.
     * @param port       the port the member listens on.
     * @param delayed    whether the member has delayed responses.
     * @param unreliable whether the member drops messages.
     * @throws IOException if server socket initialization fails.
     */
    public CouncilMember(String name, int port, boolean delayed, boolean unreliable) throws IOException {
        this.name = name;
        this.port = port;
        this.delayed = delayed;
        this.unreliable = unreliable;
        this.serverSocket = new ServerSocket(port);
        System.out.println("CouncilMember " + name + " started and listening on port " + port);
    }

    /**
     * Returns the election log entries.
     *
     * @return a list of log entries.
     */
    public static List<String> getLogEntries() {
        return logEntries;
    }

    /**
     * Starts an election process by proposing a candidate.
     *
     * @param candidate the candidate being proposed.
     * @param members   the list of council members participating in the election.
     */
    public void startElection(String candidate, List<CouncilMember> members) {
        System.out.println(name + " is proposing candidate: " + candidate);
        logEntries.add("[ACTION] " + name + " proposed " + candidate + " for president.");
        for (CouncilMember member : members) {
            sendMessage(member, "PROPOSE " + candidate);
        }
    }

    /**
     * Receives and processes a message from another council member.
     *
     * @param message the received message.
     */
    public void receiveMessage(String message) {
        executorService.submit(() -> {
            try {
                if (delayed && name.equals("M2")) Thread.sleep(3000);

                System.out.println(name + " received message: " + message);
                String[] parts = message.split(" ");
                String type = parts[0];
                String candidate = parts[1];

                if (type.equals("PROPOSE") && !votedCandidates.contains(candidate)) {
                    vote(candidate);
                }

                if (type.equals("ACCEPT")) {
                    logEntries.add("[RESULT] President elected: " + candidate);
                    System.out.println("[RESULT] President elected: " + candidate);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Casts a vote for a candidate.
     *
     * @param candidate the candidate being voted for.
     */
    private void vote(String candidate) {
        votedCandidates.add(candidate);
        votes.put(candidate, votes.getOrDefault(candidate, 0) + 1);
        logEntries.add("[VOTE] " + name + " voted for " + candidate + ".");
        System.out.println(name + " voted for " + candidate);

        if (votes.get(candidate) >= (votes.size() / 2) + 1) {
            broadcastMessage("ACCEPT " + candidate);
        }
    }

    /**
     * Sends a message to another council member.
     *
     * @param member  the recipient council member.
     * @param message the message to send.
     */
    private void sendMessage(CouncilMember member, String message) {
        executorService.submit(() -> {
            try {
                Socket socket = new Socket("localhost", member.port);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                if (!unreliable || Math.random() > 0.5) {
                    out.println(message);
                }
                socket.close();
            } catch (IOException e) {
                System.out.println("Failed to send message to " + member.name + ": " + e.getMessage());
            }
        });
    }

    /**
     * Broadcasts a message to all council members.
     *
     * @param message the message to broadcast.
     */
    private void broadcastMessage(String message) {
        logEntries.add("[BROADCAST] " + message);
        System.out.println("Broadcasting: " + message);
        for (String log : logEntries) {
            System.out.println(log);
        }
    }

    /**
     * The run method implements the server-side logic for receiving messages.
     */
    @Override
    public void run() {
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String message = in.readLine();
                if (message != null) {
                    receiveMessage(message);
                }
                socket.close();
            }
        } catch (IOException e) {
            System.out.println("Error in " + name + " server: " + e.getMessage());
        }
    }
}
