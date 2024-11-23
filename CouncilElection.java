import java.util.*;

/**
 * The CouncilElection class simulates a distributed election process among council members.
 * It initializes council members, starts their threads, and coordinates the election process.
 */
public class CouncilElection {
    public static void main(String[] args) throws Exception {
        // Define council members
        CouncilMember m1 = new CouncilMember("M1", 5001, false, false); // Always active
        CouncilMember m2 = new CouncilMember("M2", 5002, true, true);   // Delayed response
        CouncilMember m3 = new CouncilMember("M3", 5003, false, false);
        CouncilMember m4 = new CouncilMember("M4", 5004, false, false);
        CouncilMember m5 = new CouncilMember("M5", 5005, false, false);

        List<CouncilMember> members = Arrays.asList(m1, m2, m3, m4, m5);

        // Start members
        for (CouncilMember member : members) {
            new Thread(member).start();
        }

        // Simulate election process
        Thread.sleep(2000); // Allow time for servers to initialize
        m1.startElection("M1", members);
        m2.startElection("M2", members);

        Thread.sleep(5000); // Wait for votes to be processed

        // Display final log
        System.out.println("\n=== ELECTION LOG ===");
        for (String log : CouncilMember.getLogEntries()) {
            System.out.println(log);
        }
    }
}
