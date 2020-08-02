import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class Server {

    private static final int UDP_PORT = 3117;
    private ExecutorService threadPool;
    private DatagramSocket serverSocket;
    private boolean running = false;

    public Server() {
        threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }


    public void startServer() {
        try {
            this.serverSocket = new DatagramSocket(UDP_PORT);
            byte[] receiveData = new byte[1024];
            running = true;
            while (running) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                try {
                    serverSocket.receive(receivePacket);
                    System.out.println("Server received packet");
                    //sends Offer or Ack/Nak msg
                    ServerThread thread = new ServerThread(serverSocket, receivePacket);
                    threadPool.execute(thread);
               //received socket error
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //server socket error
        } catch (SocketException e) {
            e.printStackTrace();
            stopServer();
        }
    }

    public void stopServer(){
        threadPool.shutdown();
        try{
            threadPool.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            threadPool.shutdownNow();
        }
        running = false;
    }
}
