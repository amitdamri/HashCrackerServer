import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.*;

public class Test2 {

    public static void main(String[] args) throws IOException {
        /*Message g= new Message("7f5bb03cf507c861269be561971108be8f37d832","5","aaaaa","zzzzz");
        byte[] s = g.toByte();
        String[] f = Message.convertByte(s);
        System.out.println(f[0]);
        System.out.println(f[1]);
        System.out.println(f[2]);
        System.out.println(f[3]);
        System.out.println(f[4]);
        System.out.println(f[5]);*/
        Client client = new Client();
        client.startClient();
    }
}
