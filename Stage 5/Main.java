package correcter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner input = new Scanner(System.in);
        System.out.println("Write a mode: ");
        String choice = input.nextLine();
        ArrayList<Byte> encoded = null;
        ArrayList<Byte> sent = null;
        switch(choice){
            case "encode":
                encode();
                break;
            case "send":
                encoded = encode();
                send(encoded);
                break;
            case "decode":
                encoded = encode();
                sent = send(encoded);
                decode(sent);
                break;
            default:
                System.out.println("wrong input!");
                break;
        }
        input.close();
    }

    public static ArrayList<Byte> encode() {

        File input = new File("send.txt");
        System.out.println("send.txt:");
        ArrayList<Byte> array = new ArrayList<>();
        ArrayList<Byte> encodedArray = null;

        try (InputStream inputStream = new FileInputStream(input)) {

            int i = inputStream.read();

            while (i != -1) {
                array.add((byte) i);
                i = inputStream.read();
            }

            encodedArray = encodeArray(array);
            writeArray(encodedArray, "encoded.txt");
        }
        catch (Exception e) {
            System.out.println("oooops " + e);
        }
        return encodedArray;
    }

    public static ArrayList<Byte> encodeArray(ArrayList<Byte> array) {

        ArrayList<Byte> result = new ArrayList<>();

        byte encoded = 0;
        int[] ones = new int[8];

        for(int i = 0; i < array.size(); i++) {
            byte b = array.get(i);
            for(int j = 0; j < 8; j++) {
                byte mask = (byte) (1 << j);
                ones[7-j] = (b & mask) > 0 ? 1 : 0;
            }
            encoded = (byte) (encoded | (ones[0] << 5));
            encoded = (byte) (encoded | (ones[1] << 3));
            encoded = (byte) (encoded | (ones[2] << 2));
            encoded = (byte) (encoded | (ones[3] << 1));

            encoded = (byte) (encoded | (((ones[0] + ones[1] + ones[3]) % 2) << 7));
            encoded = (byte) (encoded | (((ones[0] + ones[2] + ones[3]) % 2) << 6));
            encoded = (byte) (encoded | (((ones[1] + ones[2] + ones[3]) % 2) << 4));

            result.add(encoded);
            encoded = 0;

            encoded = (byte) (encoded | (ones[4] << 5));
            encoded = (byte) (encoded | (ones[5] << 3));
            encoded = (byte) (encoded | (ones[6] << 2));
            encoded = (byte) (encoded | (ones[7] << 1));

            encoded = (byte) (encoded | (((ones[4] + ones[5] + ones[7]) % 2) << 7));
            encoded = (byte) (encoded | (((ones[4] + ones[6] + ones[7]) % 2) << 6));
            encoded = (byte) (encoded | (((ones[5] + ones[6] + ones[7]) % 2) << 4));

            result.add(encoded);
            encoded = 0;

        }

        return result;
    }

    public static void writeArray(ArrayList<Byte> array, String filename) {

        File outputFile = new File(filename);

        try (OutputStream outputStream = new FileOutputStream(outputFile, false)) {

            for (int i = 0; i < array.size(); i++) {
                int z = array.get(i);
                System.out.print((char) z);
                outputStream.write((int) array.get(i));
            }
        } catch (Exception e) {
            System.out.println("Whoops!" + e);
        }
    }

    public static void decode(ArrayList<Byte> encoded) {

        File outputFile = new File("decoded.txt");
        StringBuilder result = new StringBuilder();
        byte decoded = 0;
        int[] ones = new int[8];

        byte b;
        byte v;
        for (int i = 0; i < encoded.size(); i = i + 2) { //fix errors first
            b = encoded.get(i);
            v = encoded.get(i + 1);
            b = correct(b);
            v = correct(v);

            decoded = (byte) (decoded | bit(b, 5) << 7);
            decoded = (byte) (decoded | bit(b, 3) << 6);
            decoded = (byte) (decoded | bit(b, 2) << 5);
            decoded = (byte) (decoded | bit(b, 1) << 4);

            decoded = (byte) (decoded | bit(v, 5) << 3);
            decoded = (byte) (decoded | bit(v, 3) << 2);
            decoded = (byte) (decoded | bit(v, 2) << 1);
            decoded = (byte) (decoded | bit(v, 1) << 0);

            result.append((char) decoded);
            decoded = 0;
        }
        
        try (PrintWriter outputStream = new PrintWriter(outputFile, StandardCharsets.UTF_8)) {
            outputStream.write(result.toString());
        } catch (Exception e) {
            System.out.println("Whoops!" + e);
        }
    }

    public static Byte correct (Byte b){

        int a = bit(b, 1) ^ bit(b, 3) ^ bit(b, 5) ^ bit(b, 7);
        int d = bit(b, 5) ^ bit(b, 2) ^ bit(b, 1) ^ bit(b, 6);
        int c = bit(b, 1) ^ bit(b, 2) ^ bit(b, 3) ^ bit(b, 4);


        b = (byte) (b ^ (1 << ((8 - a - 2 * d - 4 * c) % 8))); //error correction
        return b;
    }

    public static int bit(byte b, int position) {
        return (b & (1 << position)) > 0? 1 : 0;
    }
    public static ArrayList<Byte> send(ArrayList<Byte> encodedArray) {

        Random random = new Random();
        ArrayList<Byte> result = new ArrayList<>();
        for (int i = 0; i < encodedArray.size(); i++) {
            byte newByte = (byte) (encodedArray.get(i) ^ (1 << random.nextInt(8)));
            result.add(newByte);
        }
        writeArray(result, "received.txt");
        return result;
    }
}
