import org.junit.Test;
import org.junit.Assert;

import java.util.*;

/**
 * The CouncilElectionTest class contains unit tests to verify the election logic.
 */
public class CouncilElectionTest {

    /**
     * Tests the election process when all members respond immediately.
     */
    @Test
    public void testAllMembersRespondImmediately() throws Exception {
        List<CouncilMember> members = new ArrayList<>();
        for (int i = 1; i <= 9; i++) {
            members.add(new CouncilMember("M" + i, 5000 + i, false, false));
        }
        for (CouncilMember member : members) {
            new Thread(member).start();
        }
        Thread.sleep(1000);
        members.get(0).startElection("M1", members);
        Thread.sleep(3000);
        Assert.assertTrue(CouncilMember.getLogEntries().contains("[RESULT] President elected: M1"));
    }

    /**
     * Tests the election process with a single candidate proposing and winning.
     */
    @Test
    public void testSingleCandidateProposesAndWins() throws Exception {
        List<CouncilMember> members = new ArrayList<>();
        for (int i = 1; i <= 9; i++) {
            members.add(new CouncilMember("M" + i, 5000 + i, false, false));
        }
        for (CouncilMember member : members) {
            new Thread(member).start();
        }
        Thread.sleep(1000);
        members.get(0).startElection("M1", members);
        Thread.sleep(3000);
        List<String> logs = CouncilMember.getLogEntries();
        Assert.assertTrue(logs.contains("[ACTION] M1 proposed M1 for president."));
        Assert.assertTrue(logs.contains("[RESULT] President elected: M1"));
    }

    /**
     * Tests the election process when multiple candidates propose themselves.
     */
    @Test
    public void testMultipleCandidatesProposeAndWinnerIsElected() throws Exception {
        List<CouncilMember> members = new ArrayList<>();
        for (int i = 1; i <= 9; i++) {
            members.add(new CouncilMember("M" + i, 5000 + i, false, false));
        }
        for (CouncilMember member : members) {
            new Thread(member).start();
        }
        Thread.sleep(1000);
        members.get(0).startElection("M1", members);
        members.get(1).startElection("M2", members);
        Thread.sleep(3000);
        List<String> logs = CouncilMember.getLogEntries();
        Assert.assertTrue(logs.contains("[ACTION] M1 proposed M1 for president."));
        Assert.assertTrue(logs.contains("[ACTION] M2 proposed M2 for president."));
        Assert.assertTrue(logs.stream().anyMatch(log -> log.contains("[RESULT] President elected: ")));
    }
}
