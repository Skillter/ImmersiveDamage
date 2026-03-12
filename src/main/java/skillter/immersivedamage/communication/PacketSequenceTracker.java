package skillter.immersivedamage.communication;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PacketSequenceTracker {

    private static final int MAX_TRACKED_SEQUENCES = 1000;
    private final Set<Long> receivedSequences = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public boolean isDuplicate(long sequenceNumber) {
        return receivedSequences.contains(sequenceNumber);
    }

    public void markReceived(long sequenceNumber) {
        receivedSequences.add(sequenceNumber);
        cleanupIfNeeded();
    }

    private void cleanupIfNeeded() {
        if (receivedSequences.size() > MAX_TRACKED_SEQUENCES) {
            receivedSequences.clear();
        }
    }
}
