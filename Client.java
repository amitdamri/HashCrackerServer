import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.*;

public class Client {

    private static int CORES = Runtime.getRuntime().availableProcessors();
    private ExecutorService threadPool;
    private DatagramSocket socket;
    public static int nackCounter = 0;
    public static boolean wasACK = false;

    public Client() {
        threadPool = Executors.newFixedThreadPool(CORES);
    }

    public void startClient() {
        wasACK = false;

        try {
            String[] input = getUserInput();

            // Send discover message
            broadcast(InetAddress.getByName("255.255.255.255"));

            ArrayList<InetAddress> servers = new ArrayList<>();

            // Get offers from servers
            long t = System.currentTimeMillis();
            long end = t + 1000;
            socket.setSoTimeout(2000);
            while (System.currentTimeMillis() < end) {
                byte[] buf = new byte[1024];
                DatagramPacket dp = new DatagramPacket(buf, buf.length);
                try {
                    socket.receive(dp);
                    if (checkIsOffer(dp.getData())) {
                        servers.add(dp.getAddress());
                    }
                }
                catch (IOException e) { /*no more packets to receive*/ }
            }

            socket = new DatagramSocket(); // re-new socket

            if (servers.size() == 0)
                throw new IOException();

            // Send request
            String[] domains = divideToDomains(Integer.parseInt(input[1]), servers.size());
            for (int i = 0; i < servers.size(); i += 2) {
                Message msg = new Message(input[0], input[1], domains[i], domains[i + 1]);
                DatagramPacket dp = new DatagramPacket(msg.toByte(), msg.toByte().length, servers.get(i), 3117);
                ClientThread clientThread = new ClientThread(socket, dp);
                threadPool.execute(clientThread);
            }
            threadPool.shutdown();
            threadPool.awaitTermination(15, TimeUnit.SECONDS);

            if (nackCounter == servers.size() || !wasACK) // all servers sent NACK
                System.out.println("Unfortunately, none of the servers couldn't crack given hash");


        } catch (IOException e) {
            System.err.println("None of the servers responded");
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            System.err.println("Error occurred while waiting for termination");
        }

    }


    // HELPER FUNCTIONS //

    private Message getDiscoverMsg() {

        try {
            String type = "1";
            return new Message(type);
        } catch (IOException e) {
            System.err.println("Error occurred while preparing a discover message");
        }

        return null;

    }

    private String[] getUserInput() {

        String[] in = new String[2];
        System.out.println("Welcome to $&*87$#MeltdownAndSpectre$!5#@?5. Please enter the hash:");
        in[0] = new Scanner(System.in).next();
        while (!in[0].matches("^[a-f0-9]*$") || in[0].length()!=40) {
            System.out.println("Enter valid hash, please.");
            in[0] = new Scanner(System.in).next();
        }

        System.out.println("Please enter the input string length:");
        in[1] = new Scanner(System.in).next();
        while (!in[1].matches("^\\d{1,3}$")|| Integer.parseInt(in[1])>256 || Integer.parseInt(in[1]) <= 0) {
            System.out.println("Enter valid length, please.");
            in[1] = new Scanner(System.in).next();
        }

        return in;

    }

    private boolean checkIsOffer(byte[] buff) {
        /*try {
            final DataInputStream stream2 = new DataInputStream(
                    new ByteArrayInputStream(buff)
            );
            //read msg info
            String teamName = stream2.readUTF(); // not interesting
            String msgType = stream2.readUTF();
            return msgType.equals("2");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;*/

        String[] msg = Message.convertByte(buff);
        return (msg[1].equals("2"));

    }

    private BigInteger convertStringToInt(String toConvert) {

        char[] charArray = toConvert.toCharArray();
        BigInteger num = new BigInteger("0");
        for (char c : charArray) {
            if (c < 'a' || c > 'z') {
                throw new RuntimeException();
            }
            num = num.multiply(new BigInteger("26"));
            int x = c - 'a';
            num = num.add(new BigInteger(Integer.toString(x)));
        }
        return num;
    }

    private String converxtIntToString(BigInteger toConvert, int length) {
        StringBuilder s = new StringBuilder(length);
        while (toConvert.compareTo(new BigInteger("0")) > 0) {
            BigInteger c = toConvert.mod(new BigInteger("26"));
            s.insert(0, (char) (c.intValue() + 'a'));
            toConvert = toConvert.divide(new BigInteger("26"));
            length--;
        }
        while (length > 0) {
            s.insert(0, 'a');
            length--;
        }
        return s.toString();
    }

   /* public String[] divideToDomains(int stringLength, int numOfServers) {
        String[] domains = new String[numOfServers * 2];

        StringBuilder first = new StringBuilder(); //aaa
        StringBuilder last = new StringBuilder(); //zzz

        for (int i = 0; i < stringLength; i++) {
            first.append("a"); //aaa
            last.append("z"); //zzz
        }

        int total = convertStringToInt(last.toString());
        int perServer = (int) Math.floor(((double) total) / ((double) numOfServers));

        domains[0] = first.toString(); //aaa
        domains[domains.length - 1] = last.toString(); //zzz
        int summer = 0;

        for (int i = 1; i <= domains.length - 2; i += 2) {
            summer += perServer;
            domains[i] = converxtIntToString(summer, stringLength); //end domain of server
            summer++;
            domains[i + 1] = converxtIntToString(summer, stringLength); //start domain of next server
        }

        return domains;
    }*/


    public  String [] divideToDomains (int stringLength, int numOfServers){
        String [] domains = new String[numOfServers * 2];

        StringBuilder first = new StringBuilder(); //aaa
        StringBuilder last = new StringBuilder(); //zzz

        for(int i = 0; i < stringLength; i++){
            first.append("a"); //aaa
            last.append("z"); //zzz
        }

        BigInteger total = convertStringToInt(last.toString());
        BigInteger perServer = total.divide(BigInteger.valueOf(numOfServers));

        domains[0] = first.toString(); //aaa
        domains[domains.length -1 ] = last.toString(); //zzz
        BigInteger summer = new BigInteger("0");

        for(int i = 1; i <= domains.length -2; i += 2){
            summer =  summer.add(perServer);
            domains[i] = converxtIntToString(summer, stringLength); //end domain of server
            summer = summer.add(BigInteger.valueOf(1));//++;
            domains[i + 1] = converxtIntToString(summer, stringLength); //start domain of next server
        }

        return domains;
    }

    private void broadcast(InetAddress address) throws IOException {
        //address = InetAddress.getByName("localhost"); //remove
        socket = new DatagramSocket();
        socket.setBroadcast(true);
        Message discoverMsg = getDiscoverMsg();
        byte[] discoverMsgBytes = discoverMsg.toByte();
        DatagramPacket packet = new DatagramPacket(discoverMsgBytes, discoverMsgBytes.length, address, 3117);
        socket.send(packet);
    }
}
