package skillter.immersivedamage.communication.packet;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class AckPacket extends Packet implements Serializable {

    public long sequenceNumber;
    public long originalTimestamp;

    public AckPacket(long sequenceNumber, long originalTimestamp) {
        this.sequenceNumber = sequenceNumber;
        this.originalTimestamp = originalTimestamp;
    }

    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", "ACK");
            jsonObject.put("sequenceNumber", sequenceNumber);
            jsonObject.put("originalTimestamp", originalTimestamp);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return jsonObject.toString();
    }
}
