import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Message {

    private static final String TEAM_NAME = "$&*87$#MeltdownAndSpectre$!5#@?5";

    private String type;
    private String hash;
    private String originalLength;
    private String originalStringStart;
    private String originalStringEnd;


    /**
     * Request message
     *
     * @param hash                hashCode to encrypt
     * @param originalLength      input length
     * @param originalStringStart String lower bound of the search
     * @param originalStringEnd   String upper Bound of the search
     * @throws IOException
     */
    public Message(String hash, String originalLength, String originalStringStart, String originalStringEnd) throws IOException {
        if (hash.length() != 40 || originalStringStart.length() > 256 || originalStringStart.length() < 1 || originalStringEnd.length() > 256 || originalStringEnd.length() < 1 || Integer.parseInt(originalLength)<1 || Integer.parseInt(originalLength)>256)
            throw new IOException("invalid input: Hash size=40, OriginalStringStart=1-256, OriginalStringEnd=1-256.");
        this.type = "3";
        this.hash = hash;
        this.originalLength = originalLength;
        this.originalStringStart = originalStringStart;
        this.originalStringEnd = originalStringEnd;
    }

    /**
     * Offer & discover message
     *
     * @param type of message - offer 2 or discover 1
     * @throws IOException
     */
    public Message(String type) throws IOException {
        if (!type.equals("2") && !type.equals("1"))
            throw new IOException("invalid input: Only offer and discover messages with type 1 or 2");
        this.type = type;
        this.hash = "";
        this.originalLength = "";
        this.originalStringStart = "";
        this.originalStringEnd = "";
    }

    /**
     * Acknowledge message
     *
     * @param hash                hashCode to encrypt
     * @param originalLength      input length
     * @param originalStringStart String lower bound of the search
     */
    public Message(String hash, String originalLength, String originalStringStart) throws IOException {
        if (hash.length() != 40 || originalStringStart.length() > 256 || originalStringStart.length() < 1 || Integer.parseInt(originalLength)<1 || Integer.parseInt(originalLength)>256)
            throw new IOException("invalid input: Hash size=40, OriginalStringStart=1-256, OriginalStringEnd=1-256.");
        this.type = "4";
        this.hash = hash;
        this.originalLength = originalLength;
        this.originalStringStart = originalStringStart;
        this.originalStringEnd = "";
    }

    /**
     * Negative acknowledge message
     *
     * @param hash           hashCode to encrypt
     * @param originalLength input length
     */
    //negative acknowledge
    public Message(String hash, String originalLength) throws IOException {
        if (hash.length() != 40 || Integer.parseInt(originalLength)<1 || Integer.parseInt(originalLength)>256)
            throw new IOException("invalid input: Hash size=40, OriginalStringStart=1-256, OriginalStringEnd=1-256.");
        this.type = "5";
        this.hash = hash;
        this.originalLength = originalLength;
        this.originalStringStart = "";
        this.originalStringEnd = "";
    }

/*
    public byte[] getBytes() {
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(data);
        try {
            stream.writeUTF(String.valueOf(TEAM_NAME));
            stream.writeUTF(String.valueOf(type));
            stream.writeUTF(String.valueOf(hash));
            stream.writeUTF(String.valueOf(originalLength));
            stream.writeUTF(String.valueOf(originalStringStart));
            stream.writeUTF(String.valueOf(originalStringEnd));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return data.toByteArray();
    }*/


    public byte[] toByte() {

        byte[] byteArr = new byte[586];

        int i = 0; // from 0 to 31
        for (char c : TEAM_NAME.toCharArray()) {
            byteArr[i++] = (byte) c;
        }

        switch (type) {
            case "1":
                byteArr[i++] = 1;
                break;
            case "2":
                byteArr[i++] = 2;
                break;
            case "3":
                byteArr[i++] = 3;
                break;
            case "4":
                byteArr[i++] = 4;
                break;
            case "5":
                byteArr[i++] = 5;
                break;
            default:
                break;

        }

        if (type.equals("3") || type.equals("4") || type.equals("5")) {
            // from 33 to 72
            for (char c : hash.toCharArray()) {
                byteArr[i++] = (byte) c;
            }

            byteArr[i++] = (byte) (Integer.parseInt(originalLength));
            int length = Integer.parseInt(originalLength);

            if (type.equals("3") || type.equals("4")) {
                //from 74 - ?r
                for (; i < 74 + length; i++) {
                    byteArr[i] = (byte) originalStringStart.toCharArray()[i - 74];
                }
                if (type.equals("3")) {
                    for (; i < 74 + length * 2; i++) {
                        byteArr[i] = (byte) originalStringEnd.toCharArray()[i - 74 - length];
                    }
                }
            }
        }
        return byteArr;
    }

    public static String[] convertByte(byte[] message) {
        String teamName, hash, originalStartLength, originalEndLength;
        byte type, originalLength=0;
        hash = originalStartLength = originalEndLength = "";
        byte[] temp = new byte[32];
        int i;
        String[] totalMsg = new String[6];
        for (i = 0; i < 32; i++) {
            temp[i] = message[i];
        }
        teamName = new String(temp);

        temp = new byte[]{message[i++]};
        type = temp[0];

        if (type != 1 && type != 2) {
            temp = new byte[40];
            for (; i < 73; i++) {
                temp[i - 33] = message[i];
            }
            hash = new String(temp);

            temp = new byte[]{message[i++]};

            originalLength = temp[0];
            temp = new byte[originalLength];
            for (; i < 74 + temp.length; i++) {
                temp[i - 74] = message[i];
            }
            originalStartLength = new String(temp);


            temp = new byte[originalLength];
            for (; i < 74 + temp.length * 2; i++) {
                temp[i - 74 - temp.length] = message[i];
            }
            originalEndLength = new String(temp);
        }
        totalMsg[0] = teamName;
        totalMsg[1] = ""+type;
        totalMsg[2] = hash;
        totalMsg[3] = ""+ originalLength;
        totalMsg[4] = originalStartLength;
        totalMsg[5] = originalEndLength;

        return totalMsg;
    }

}
