import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ClientThread implements Runnable {

    private DatagramSocket clientSocket;
    private DatagramPacket packet;

    public ClientThread(DatagramSocket serverSocket, DatagramPacket packet) {
        this.clientSocket = serverSocket;
        this.packet = packet;
    }


    @Override
    public void run() {

        try {
            clientSocket.send(packet); // send request message
            byte[] buffer = new byte[1024];
            packet = new DatagramPacket(buffer, buffer.length);
            clientSocket.receive(packet); // wait for ack / nack
            /*final DataInputStream stream2 = new DataInputStream( // parse server's answer
                    new ByteArrayInputStream(packet.getData())
            );
            // read msg info
            String teamName = stream2.readUTF(); // not interesting
            String msgType = stream2.readUTF();
            String hash = stream2.readUTF(); // not interesting
            String length = stream2.readUTF(); // not interesting
            String solve = stream2.readUTF();*/

            String[] serverAns = Message.convertByte(buffer);
            // read msg info
            String teamName = serverAns[0];
            String msgType = serverAns[1];
            String hash = serverAns[2];
            String length = serverAns[3];
            String solve = serverAns[4];

            if (msgType.equals("4")) { // ACK
                System.out.println("The input string is " + solve);
                Client.wasACK = true;
            }
            else if (msgType.equals("5")) { // NACK
                Client.nackCounter++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
