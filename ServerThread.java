import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ServerThread implements Runnable {

    private DatagramSocket serverSocket;
    private DatagramPacket receivePacket;


    public ServerThread(DatagramSocket serverSocket, DatagramPacket receivePacket) {
        this.serverSocket = serverSocket;
        this.receivePacket = receivePacket;
    }

    //for tests
    public ServerThread() {
    }

    @Override
    public void run() {
        byte[] clientMessage = receivePacket.getData();
       /* final DataInputStream stream = new DataInputStream(
                new ByteArrayInputStream(clientMessage)
        );*/
        try {
            //read msg info
           /* String teamName = stream.readUTF(); // not interesting
            String msgType = stream.readUTF();
            System.out.println(msgType);
            String hash = stream.readUTF();
            String originalLength = stream.readUTF();
            String originalStringStart = stream.readUTF();
            String originalStringEnd = stream.readUTF();*/

           String[] clientMsg = Message.convertByte(clientMessage);
            String teamName = clientMsg[0];
            System.out.println(teamName);
            String msgType = clientMsg[1];
            System.out.println(msgType);
            String hash = clientMsg[2];
            System.out.println(hash);
            String originalLength = clientMsg[3];
            System.out.println(originalLength);
            String originalStringStart = clientMsg[4];
            String originalStringEnd = clientMsg[5];

            InetAddress IPAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();
            //discover
            if (msgType.equals("1")) {
                //sends offer
                Message msg = new Message("2");
                byte[] sendData = msg.toByte();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                serverSocket.send(sendPacket);
                System.out.println("server sent offer");
                //request
            } else if (msgType.equals("3")) {
                String hashSolve = tryDeHash(originalStringStart,originalStringEnd,hash);
                //sends ack
                if (hashSolve != null) {
                    //ack
                    Message msg = new Message(hash,originalLength,hashSolve);
                    byte[] sendData = msg.toByte();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                    serverSocket.send(sendPacket);
                    System.out.println("server sent ack");
                    //sends nak
                } else {
                    //nak
                    Message msg = new Message(hash,originalLength);
                    byte[] sendData = msg.toByte();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                    serverSocket.send(sendPacket);
                    System.out.println("server sent nak");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String hash(String toHash) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] messageDigest = md.digest(toHash.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            StringBuilder hashText = new StringBuilder(no.toString(16));
            while (hashText.length() < 32){
                hashText.insert(0, "0");
            }
            return hashText.toString();
        }
        catch (NoSuchAlgorithmException e){
            throw new RuntimeException(e);
        }
    }

    public String tryDeHash(String startRange, String endRange, String originalHash){
        int start = convertStringToInt(startRange);
        int end = convertStringToInt(endRange);
        int length = startRange.length();
        long startTime = System.currentTimeMillis();
            for (int i=start; i <= end && (System.currentTimeMillis() - startTime)/ 1000 < 20; i++) {
                String currentString = convertIntToString(i, length);
                String hash = hash(currentString);
                if (originalHash.equals(hash)) {
                    return currentString;
                }
            }
        return null;
    }

    private int convertStringToInt(String toConvert) {
        char[] charArray = toConvert.toCharArray();
        int num = 0;
        for(char c : charArray){
            if(c < 'a' || c > 'z'){
                throw new RuntimeException();
            }
            num *= 26;
            num += c - 'a';
        }
        return num;
    }

    private String convertIntToString(int toConvert, int length) {
        StringBuilder s = new StringBuilder(length);
        while (toConvert > 0 ){
            int c = toConvert % 26;
            s.insert(0, (char) (c + 'a'));
            toConvert /= 26;
            length --;
        }
        while (length > 0){
            s.insert(0, 'a');
            length--;
        }
        return s.toString();
    }

}
