package skillter.immersivedamage.communication.client;

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
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import skillter.immersivedamage.communication.packet.AckPacket;
import skillter.immersivedamage.communication.packet.DamagePacket;
import skillter.immersivedamage.communication.packet.Packet;
import skillter.immersivedamage.util.Multithreading;

public class UDPClient {

    private static final int ACK_TIMEOUT_MS = 500;
    private static final int MAX_RETRIES = 5;
    private static final int MAX_PENDING_PACKETS = 100;

    private DatagramSocket sendSoc;
    private final AtomicLong sequenceCounter = new AtomicLong(0);
    private final Map<Long, PendingPacket> pendingPackets = new ConcurrentHashMap<>();
    private final ScheduledExecutorService retryScheduler = Executors.newSingleThreadScheduledExecutor();
    private Thread ackListenerThread;
    private volatile boolean running = false;

    private static class PendingPacket {
        final byte[] data;
        final InetAddress address;
        final int port;
        final long sequenceNumber;
        final long timestamp;
        int retryCount = 0;
        ScheduledFuture<?> retryTask;

        PendingPacket(byte[] data, InetAddress address, int port, long sequenceNumber) {
            this.data = data;
            this.address = address;
            this.port = port;
            this.sequenceNumber = sequenceNumber;
            this.timestamp = System.currentTimeMillis();
        }
    }

    public void startAckListener() {
        if (running) return;
        running = true;

        ackListenerThread = new Thread(this::listenForAcks);
        ackListenerThread.setDaemon(true);
        ackListenerThread.setName("UDP-Ack-Listener");
        ackListenerThread.start();
    }

    public void stopAckListener() {
        running = false;
        if (ackListenerThread != null) {
            ackListenerThread.interrupt();
        }
        retryScheduler.shutdown();
        pendingPackets.clear();
    }

    private void listenForAcks() {
        byte[] buffer = new byte[5000];

        while (running) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                sendSoc.setSoTimeout(1000);
                sendSoc.receive(packet);

                // deserialize the ACK we got back
                ByteArrayInputStream byteStream = new ByteArrayInputStream(packet.getData());
                ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(byteStream));
                Object obj = is.readObject();
                is.close();

                if (obj instanceof AckPacket) {
                    AckPacket ack = (AckPacket) obj;
                    handleAck(ack.sequenceNumber);
                }
            } catch (SocketTimeoutException e) {
                // no data came in, that's fine
            } catch (Exception ex) {
                if (running) {
                    System.out.println("Error in ACK listener: " + ex.getMessage());
                }
            }
        }
    }

    private void handleAck(long sequenceNumber) {
        PendingPacket pending = pendingPackets.remove(sequenceNumber);
        if (pending != null) {
            if (pending.retryTask != null) {
                pending.retryTask.cancel(false);
            }
            long rtt = System.currentTimeMillis() - pending.timestamp;
            System.out.println("ACK received for seq=" + sequenceNumber + ", RTT=" + rtt + "ms");
        }
    }

    public void sendPacketReliable(String ip, int port, DamagePacket packet) {
        if (!running) {
            startAckListener();
        }

        // give it a sequence number so we can track ACKs
        packet.sequenceNumber = sequenceCounter.incrementAndGet();
        packet.timestamp = System.currentTimeMillis();

        handleSendPacketReliable(ip, port, packet);
    }

    public void sendPacketReliableAsync(String ip, int port, DamagePacket packet) {
        Multithreading.runAsync(() -> sendPacketReliable(ip, port, packet));
    }

    private void handleSendPacketReliable(String ip, int port, DamagePacket packet) {
        InetAddress serverIP = null;

        try {
            serverIP = InetAddress.getByName(ip);
        } catch (UnknownHostException ex) {
            System.out.println("Bad server address in UDPClient, " + ip + " caused an unknown host exception " + ex);
            return;
        }

        // clean up old entries if we have too many pending
        if (pendingPackets.size() > MAX_PENDING_PACKETS) {
            cleanupOldPackets();
        }

        // serialize the packet
        byte[] sendBuf = serializePacket(packet);
        if (sendBuf == null) return;

        // keep track of this so we can resend if needed
        PendingPacket pending = new PendingPacket(sendBuf, serverIP, port, packet.sequenceNumber);
        pendingPackets.put(packet.sequenceNumber, pending);

        // send it
        send(pending.data, serverIP, port);
        System.out.println("Sending reliable packet seq=" + packet.sequenceNumber + " to " + ip + ":" + port);

        // schedule retries in case it gets lost
        pending.retryTask = retryScheduler.scheduleAtFixedRate(() -> {
            PendingPacket p = pendingPackets.get(packet.sequenceNumber);
            if (p == null) return;

            p.retryCount++;
            if (p.retryCount > MAX_RETRIES) {
                System.out.println("Max retries reached for seq=" + packet.sequenceNumber + ", giving up");
                pendingPackets.remove(packet.sequenceNumber);
                if (p.retryTask != null) {
                    p.retryTask.cancel(false);
                }
                return;
            }

            System.out.println("Retry " + p.retryCount + " for seq=" + packet.sequenceNumber);
            send(p.data, p.address, p.port);
        }, ACK_TIMEOUT_MS, ACK_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    }

    private void cleanupOldPackets() {
        long now = System.currentTimeMillis();
        long maxAge = ACK_TIMEOUT_MS * MAX_RETRIES + 1000;
        Iterator<Map.Entry<Long, PendingPacket>> iterator = pendingPackets.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, PendingPacket> entry = iterator.next();
            PendingPacket p = entry.getValue();
            if ((now - p.timestamp) > maxAge) {
                iterator.remove();
            }
        }
    }

    private byte[] serializePacket(Packet packet) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(5000);
        try {
            ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(byteStream));
            os.flush();
            os.writeObject(packet);
            os.flush();
            os.close();
        } catch (IOException ex) {
            System.out.println("Error serializing object for transmission.");
            ex.printStackTrace();
            return null;
        }
        return byteStream.toByteArray();
    }

    private void send(byte[] data, InetAddress destAddr, int destPort) {
        DatagramPacket pkt = new DatagramPacket(data, data.length, destAddr, destPort);
        try {
            sendSoc.send(pkt);
        } catch (IOException ex) {
            System.out.println("Error transmitting packet over network.");
            ex.printStackTrace();
        }
    }

    public UDPClient() {
        try {
            sendSoc = new DatagramSocket();
        } catch (SocketException ex) {
            System.out.println("Error creating socket for sending data.");
            ex.printStackTrace();
        }
    }

    // older non-reliable methods, kept for backward compat
    public void sendPacket(String ip, int port, Packet packet) {
        handleSendPacket(ip, port, packet);
    }

    public void sendPacketAsync(String ip, int port, Packet packet) {
        Multithreading.runAsync(() -> handleSendPacket(ip, port, packet));
    }

    private void handleSendPacket(String ip, int port, Packet packet) {
        InetAddress serverIP = null;

        try {
            serverIP = InetAddress.getByName(ip);
        } catch (UnknownHostException ex) {
            System.out.println("Bad server address in UDPClient, " + ip + " caused an unknown host exception " + ex);
            return;
        }

        System.out.println("Sending packet to " + ip + ":" + port + " " + packet.toString());
        byte[] sendBuf = serializePacket(packet);
        if (sendBuf != null) {
            send(sendBuf, serverIP, port);
        }
    }
}
