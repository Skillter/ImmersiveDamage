package skillter.immersivedamage.communication;

import skillter.immersivedamage.communication.client.UDPClient;

public class UDPManager {

    public static UDPClient udpClient = null;

    public static void init() {
        if (udpClient == null) {
            udpClient = new UDPClient();
        }
    }

    public static void shutdown() {
        if (udpClient != null) {
            udpClient.stopAckListener();
        }
    }
}
