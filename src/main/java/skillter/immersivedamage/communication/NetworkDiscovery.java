package skillter.immersivedamage.communication;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import skillter.immersivedamage.communication.packet.DiscoveryPacket;
import skillter.immersivedamage.util.Multithreading;

public class NetworkDiscovery {

    public static final int DISCOVERY_PORT = 4446;
    private static final int DISCOVERY_INTERVAL_SECONDS = 5;

    private static final List<DiscoveredDevice> discoveredDevices = new ArrayList<>();
    private static ScheduledExecutorService scheduler;
    private static DatagramSocket socket;  // single socket for send/receive
    private static volatile boolean running = false;

    public static class DiscoveredDevice {
        public String ip;
        public int port;
        public String deviceModel;
        public String deviceId;
        public long lastSeen;

        public DiscoveredDevice(String ip, int port, String deviceModel, String deviceId) {
            this.ip = ip;
            this.port = port;
            this.deviceModel = deviceModel;
            this.deviceId = deviceId;
            this.lastSeen = System.currentTimeMillis();
        }

        @Override
        public String toString() {
            return deviceModel + " (" + ip + ")";
        }
    }

    public static void start() {
        if (running) return;
        running = true;

        try {
            socket = new DatagramSocket();  // ephemeral port for both send and receive
            socket.setBroadcast(true);
            socket.setSoTimeout(1000);
    
        } catch (SocketException ex) {

            running = false;
            return;
        }

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(NetworkDiscovery::broadcastDiscovery, 0, DISCOVERY_INTERVAL_SECONDS, TimeUnit.SECONDS);

        Multithreading.runAsync(NetworkDiscovery::listenForResponses);
    }

    public static void stop() {
        running = false;
        if (scheduler != null) {
            scheduler.shutdown();
            scheduler = null;
        }
        if (socket != null) {
            socket.close();
            socket = null;
        }
        synchronized (discoveredDevices) {
            discoveredDevices.clear();
        }
    }

    private static void broadcastDiscovery() {
        if (socket == null || socket.isClosed()) return;

        try {
            DiscoveryPacket query = new DiscoveryPacket(DiscoveryPacket.TYPE_QUERY, null, null, null, 0);

            ByteArrayOutputStream byteStream = new ByteArrayOutputStream(5000);
            ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(byteStream));
            os.flush();
            os.writeObject(query);
            os.flush();
            os.close();

            byte[] sendData = byteStream.toByteArray();

            // broadcast to 255.255.255.255
            DatagramPacket packet = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), DISCOVERY_PORT);
            socket.send(packet);

        } catch (IOException ex) {
    
        }
    }

    private static void listenForResponses() {
        byte[] buffer = new byte[5000];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        while (running) {
            try {
                socket.receive(packet);
                InetAddress senderAddr = packet.getAddress();

                ByteArrayInputStream byteStream = new ByteArrayInputStream(packet.getData());
                ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(byteStream));
                Object obj = is.readObject();
                is.close();

                if (obj instanceof DiscoveryPacket) {
                    DiscoveryPacket dp = (DiscoveryPacket) obj;
                    if (DiscoveryPacket.TYPE_RESPONSE.equals(dp.type)) {
                        handleDiscoveryResponse(dp, senderAddr);
                    }
                }
            } catch (SocketTimeoutException e) {
                // normal - just continue loop
            } catch (IOException | ClassNotFoundException ex) {
                if (running) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private static void handleDiscoveryResponse(DiscoveryPacket dp, InetAddress senderAddr) {
        // prefer explicitly reported IP, fallback to senderAddr if null
        String ip = (dp.ip != null && !dp.ip.isEmpty()) ? dp.ip : senderAddr.getHostAddress();



        synchronized (discoveredDevices) {
            // remove existing entry for this deviceId
            discoveredDevices.removeIf(d -> d.deviceId != null && d.deviceId.equals(dp.deviceId));

            // add new/updated entry
            discoveredDevices.add(new DiscoveredDevice(ip, dp.serverPort, dp.deviceModel, dp.deviceId));
        }
    }

    public static List<DiscoveredDevice> getDiscoveredDevices() {
        synchronized (discoveredDevices) {
            return new ArrayList<>(discoveredDevices);
        }
    }

    public static DiscoveredDevice getBestDevice() {
        synchronized (discoveredDevices) {
            if (discoveredDevices.isEmpty()) {
                return null;
            }
            // return most recently seen device
            return discoveredDevices.get(discoveredDevices.size() - 1);
        }
    }

    public static boolean hasDiscoveredDevice() {
        synchronized (discoveredDevices) {
            return !discoveredDevices.isEmpty();
        }
    }

    public static int getDeviceCount() {
        synchronized (discoveredDevices) {
            return discoveredDevices.size();
        }
    }
}
