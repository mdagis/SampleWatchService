package com.agis.samplewatchservice;

import com.agis.samplewatchservice.derby.JavaDBDemo;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import static java.nio.file.StandardWatchEventKinds.*;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainWatch {

    private static BufferedWriter writer;

    private static void watchDirectoryPath(Path path) {
        // Sanity check - Check if path is a folder
        try {
            Boolean isFolder = (Boolean) Files.getAttribute(path,
                    "basic:isDirectory", NOFOLLOW_LINKS);
            if (!isFolder) {
                throw new IllegalArgumentException("Path: " + path + " is not a folder");
            }
        } catch (IOException ioe) {
            // Folder does not exists
            ioe.printStackTrace();
        }

        System.out.println("Watching path: " + path);

        // We obtain the file system of the Path
        FileSystem fs = path.getFileSystem();

        // We create the new WatchService using the new try() block
        try (WatchService service = fs.newWatchService()) {

            // We register the path to the service
            // We watch for creation events


            // Use the line bellow to register folder without subfolders
            //path.register(service, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);


            // Register all subfolders of path
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                        throws IOException {
                    dir.register(service, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                    return FileVisitResult.CONTINUE;
                }
            });


            // Start the infinite polling loop
            WatchKey key = null;
            while (true) {
                key = service.take();

                // Dequeueing events
                Kind<?> kind = null;
                for (WatchEvent<?> watchEvent : key.pollEvents()) {
                    // Get the type of the event
                    kind = watchEvent.kind();
                    String outputResult = "";
                    if (OVERFLOW == kind) {
                        continue; //loop
                    } else if (ENTRY_CREATE == kind) {
                        // A new Path was created
                        Path newPath = ((WatchEvent<Path>) watchEvent).context();
                        // Output
                        outputResult = new Date().toString() + "New path created: " + newPath.toAbsolutePath().toString();
                        System.out.println(outputResult);
                    } else if (ENTRY_DELETE == kind) {
                        // A new Path was created
                        Path newPath = ((WatchEvent<Path>) watchEvent).context();
                        // Output
                        outputResult = "File deleted: " + newPath;
                        System.out.println(outputResult);
                    } else if (ENTRY_MODIFY == kind) {
                        // A new Path was created
                        Path newPath = ((WatchEvent<Path>) watchEvent).context();
                        // Output
                        outputResult = "File updated: " + newPath;
                        System.out.println(outputResult);
                    }
                    writeToFile(outputResult);
                }

                if (!key.reset()) {
                    break; //loop
                }
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (InterruptedException ie) {
            System.out.println("aaa");
            ie.printStackTrace();
        }

    }

    private static void writeToFile(String lineToWrite) {
        try {
            writer.write(lineToWrite + String.format("%n"));
            writer.flush();
        } catch (IOException ex) {
            Logger.getLogger(MainWatch.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        
        //JavaDBDemo.v();
        exitHook();
        writer = new BufferedWriter(new FileWriter("c:\\tmp\\result.txt", true));
        // Folder we are going to watch
        //Path folder = Paths.get(System.getProperty("user.home"));
        Path folder = Paths.get("c:\\temp");
        watchDirectoryPath(folder);
    }

    private static void exitHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    System.out.println("exit now");
                    writer.close();
                } catch (IOException ex) {
                    System.out.println("could not close file");
                }
            }
        });
    }
}
