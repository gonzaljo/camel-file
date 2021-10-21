package ch.pepigonzalez.file;

import org.apache.camel.Converter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@Converter
public final class ArryConverter {

    @Converter
    public static InputStream convert(ArrayList<String> users)  {

        StringBuffer buffer = new StringBuffer();

        for (String user: users) {
            buffer.append(user).append(System.lineSeparator());
        }

        return new ByteArrayInputStream(buffer.toString().getBytes(StandardCharsets.UTF_8));
    }
}
