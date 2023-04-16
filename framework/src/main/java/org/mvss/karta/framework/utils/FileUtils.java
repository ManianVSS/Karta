package org.mvss.karta.framework.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.Random;

@SuppressWarnings("unused")
public class FileUtils {
    private static final Random random = new Random();

    public static byte[] getAllBytesInResourceFile(String resourceName) throws IOException {
        return readContentFromInputStream(Objects.requireNonNull(FileUtils.class.getResourceAsStream("/" + resourceName)));
    }

    public static String getAllTextInResourceFile(String resourceName) throws IOException {
        return readStringFromInputStream(FileUtils.class.getResourceAsStream("/" + resourceName));
    }

    public static byte[] readContentFromInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        byte[] buffer = new byte[65536];

        int bytesRead;

        while ((bytesRead = inputStream.read(buffer, 0, buffer.length)) > 0) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }

        return byteArrayOutputStream.toByteArray();
    }

    public static void writeStreamToFile(InputStream inputStream, String fileName) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
            byte[] buffer = new byte[65536];

            int bytesRead;

            while ((bytesRead = inputStream.read(buffer, 0, buffer.length)) > 0) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
        }
    }

    public static void createFileFromString(String fileName, String fileContent) throws Exception {

        File file = new File(fileName);
        if (file.exists()) {
            if (!file.delete()) {
                throw new IOException("Could not delete file " + fileName);
            }
        }
        if (!file.createNewFile()) {
            throw new IOException("Could not create new file " + fileName);
        }
        FileWriter writer = new FileWriter(file);
        writer.write(fileContent);
        writer.close();
    }

    public static String readStringFromInputStream(InputStream inputStream) throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }

    public static byte[] getAllBytesInFile(String fileName, boolean isResource) throws IOException {
        return isResource ? getAllBytesInResourceFile(fileName) : Files.readAllBytes(Paths.get(fileName));
    }

    public static String getAllTextInFile(String fileName, boolean isResource) throws IOException {
        return isResource ? getAllTextInResourceFile(fileName) : new String(Files.readAllBytes(Paths.get(fileName)));
    }

    public static void writeAllTextToFile(String text, String fileName) throws IOException {
        writeAllTextToFile(text, Paths.get(fileName));
    }

    public static void writeAllTextToFile(String text, Path path) throws IOException {
        Files.write(path, text.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static void writeAllBytesToFile(byte[] contents, String fileName) throws IOException {
        writeAllBytesToFile(contents, Paths.get(fileName));
    }

    public static void writeAllBytesToFile(byte[] contents, Path path) throws IOException {
        Files.write(path, contents, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static void createRandomFile(String fileName, long fileSize, int BLOCK_SIZE) throws IOException {
        try (OutputStream os = Files.newOutputStream(Paths.get(fileName))) {
            long bytesToWrite = fileSize;

            byte[] writeBuffer = new byte[BLOCK_SIZE];

            while (bytesToWrite >= BLOCK_SIZE) {
                random.nextBytes(writeBuffer);
                os.write(writeBuffer);
                bytesToWrite -= BLOCK_SIZE;
            }
            if (bytesToWrite > 0) {
                writeBuffer = new byte[(int) bytesToWrite];
                random.nextBytes(writeBuffer);
                os.write(writeBuffer);
            }
        }
    }

    public static void removeContentsFromFile(String fileName) throws IOException {
        org.apache.commons.io.FileUtils.writeByteArrayToFile(new File(fileName), new byte[0]);
    }
}
