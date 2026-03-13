package skillter.immersivedamage.communication.packet;

import java.io.Serializable;

public class DiscoveryPacket extends Packet implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String TYPE_QUERY = "DISCOVER";
    public static final String TYPE_RESPONSE = "DISCOVER_RESPONSE";

    public String type;
    public String deviceId;
    public String deviceModel;
    public String ip;
    public int serverPort;

    public DiscoveryPacket() {
    }

    public DiscoveryPacket(String type, String deviceId, String deviceModel, String ip, int serverPort) {
        this.type = type;
        this.deviceId = deviceId;
        this.deviceModel = deviceModel;
        this.ip = ip;
        this.serverPort = serverPort;
    }

    @Override
    public String toString() {
        return "DiscoveryPacket{" +
                "type='" + type + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", deviceModel='" + deviceModel + '\'' +
                ", ip='" + ip + '\'' +
                ", serverPort=" + serverPort +
                '}';
    }
}
