package skillter.immersivedamage.communication.packet;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class DamagePacket extends Packet implements Serializable {

    public long sequenceNumber;
    public long timestamp;
    public int duration;
    public int strength;

    public DamagePacket(long sequenceNumber, int duration, int strength) {
        this.sequenceNumber = sequenceNumber;
        this.timestamp = System.currentTimeMillis();
        this.duration = duration;
        this.strength = strength;
    }

    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("type", "DAMAGE");
            jsonObject.put("sequenceNumber", sequenceNumber);
            jsonObject.put("timestamp", timestamp);
            jsonObject.put("duration", duration);
            jsonObject.put("strength", strength);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return jsonObject.toString();
    }

}
