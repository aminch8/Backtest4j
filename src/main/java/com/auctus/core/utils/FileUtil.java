package com.auctus.core.utils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.stream.Collectors;

public class FileUtil {


    public static void writeToFile(String data,String fileName)  {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
            writer.write(data);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
