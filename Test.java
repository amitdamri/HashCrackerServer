import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.IntToDoubleFunction;

public class Test {

    public static void main(String[] args) throws IOException {
        /*ServerThread s = new ServerThread();
        String st = "9017347a610d1436c1aaf52764e6578e8fc1a083";
        System.out.println(s.tryDeHash("aaaaa","zzzzz",st));*/

        // execute one client and multiple servers
        Server server = new Server();
        server.startServer();
      /* ServerThread s = new ServerThread();
        System.out.println(s.tryDeHash("aaaaa","zzzzz","7f5bb03cf507c861269be561971108be8f37d832"));*/

    }


}
