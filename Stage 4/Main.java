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
            System.out.println("sizes: " + encodedArray.size() + "  " + array.size());
            writeArray(encodedArray, "encoded.txt");

        }
        catch (Exception e) {
            System.out.println("oooops " + e);
        }
        return encodedArray;
    }

    public static void printBinView(ArrayList<Byte> array, String view) {
        System.out.print(view);
        for(int i = 0; i < array.size(); i++){
            String s2 = String.format("%8s", Integer.toBinaryString(array.get(i) & 0xFF)).replace(' ', '0');
            System.out.print(s2);
            if (i != array.size() - 1)
                System.out.print(" ");
            else
                System.out.println();
        }
    }

    public static ArrayList<Byte> encodeArray(ArrayList<Byte> array) {

        ArrayList<Byte> result = new ArrayList<>();

        byte encoded = 0;
        int k = 0;
        byte xor = 0;
        for(int i = 0; i < array.size(); i++) {
            byte b = array.get(i);
            for(int j = 0; j < 8; j++) {
                byte mask = (byte) (1 << 7 - j);
                if((mask & b) == 0) {
                    k += 2;
                }
                else {
                    encoded = (byte) (encoded | (1 << (7 - k)));
                    k++;
                    encoded = (byte) (encoded | (1 << (7 - k)));
                    k ++;
                    xor = (byte) (xor ^ 1);
                }
                if (k == 6) {
                    k = 0;
                    if ((xor & 1) != 0) {
                        encoded = (byte) (encoded | 1);
                        encoded = (byte) (encoded | 2);
                    }
                    result.add(encoded);
                    encoded = 0;
                    xor = 0;
                }
            }

        }
        if (k > 0) {
            if ((xor & 1) != 0) {
                encoded = (byte) (encoded | 1);
                encoded = (byte) (encoded | 2);
            }
            result.add(encoded);
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
        int bitCount = 0;
        boolean[] bits = new boolean[3];

        try (PrintWriter outputStream = new PrintWriter(outputFile, StandardCharsets.UTF_8)) {

            for(int i = 0; i < encoded.size(); i++) {
                byte b = encoded.get(i);
                boolean b8 = (b & (1 << 7)) > 0;
                boolean b6 = (b & (1 << 5)) > 0;
                boolean b4 = (b & (1 << 3)) > 0;
                boolean b2 = (b & (1 << 1)) > 0;

                boolean b7 = (b & (1 << 6)) > 0;
                boolean b5 = (b & (1 << 4)) > 0;
                boolean b3 = (b & (1 << 2)) > 0;
                boolean b1 = (b & 1) > 0;

                if((b8 ^ b6 ^ b4) == b2) {
                    bits = new boolean[] {b8, b6, b4};
                }
                else if ((b7 ^ b5 ^ b3) == b1) {
                    bits = new boolean[] {b7, b5, b3};
                }

                for(int j = 0; j < 3; j++) {
                    byte set = (byte) (bits[j]? 1 : 0);
                    decoded = (byte) (decoded | (set << (7 - bitCount)));
                    bitCount++;
                    if(bitCount == 8) {
                        result.append((char) decoded);
                        bitCount = 0;
                        decoded = 0;
                    }
                }
            }
            outputStream.write(result.toString());
        }
        catch (Exception e) {
            System.out.println("Whoops!" + e);
        }
    }

    public static ArrayList<Byte> send(ArrayList<Byte> encodedArray) {

        Random random = new Random();
        for (int i = 0; i < encodedArray.size(); i++) {
            byte newByte = (byte) (encodedArray.get(i) ^ 1 << random.nextInt(8));
            encodedArray.set(i, newByte);
        }
        writeArray(encodedArray, "received.txt");
        return encodedArray;
    }
}
