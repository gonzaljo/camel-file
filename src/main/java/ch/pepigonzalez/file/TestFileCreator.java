package ch.pepigonzalez.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TestFileCreator {

    public static String[] names = {"Pepi", "Miguel", "Manuela"};

    public static void main(String[] args) {
        int namePos = 0;
        File file = new File("data/Testfile.csv");

        try (FileOutputStream fos = new FileOutputStream(file)) {

            for(int x=1; x < 100; x++) {
                int count =(int)(Math.random()*6)+1;
                for(int i=1; i<=count; i++) {
                    StringBuffer buffer = new StringBuffer();
                    buffer.append(x).append(",")
                            .append(i).append(",")
                            .append(names[namePos%names.length]).append(System.lineSeparator());
                    fos.write(buffer.toString().getBytes(StandardCharsets.UTF_8));
                }
                namePos++;
            }

            fos.flush();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
