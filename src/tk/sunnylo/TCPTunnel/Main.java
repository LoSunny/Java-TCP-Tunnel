package tk.sunnylo.TCPTunnel;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        new Main();
    }

    public Main() {
        Scanner sc = new Scanner(System.in);
        File file = new File(System.getProperty("user.dir"));
        System.out.println("Done (0s)! For help, type \"help\" or \"?\"");
        // System.out.println("Server port: " + new serverProperties().getPort());
        Thread thread = null;

        while (true) {
            String str = sc.nextLine();
            if (str.equalsIgnoreCase("stop")) {
                if (thread != null) {
                    thread.stop();
                }
                break;
            } else if (str.toLowerCase().startsWith("cd")) {
                String[] strs = str.split(" ");
                if (strs.length > 1) {
                    if (str.split(" ")[1].equals("..")) {
                        file = file.getParentFile();
                    } else {
                        file = Paths.get(file.getAbsolutePath(), strs[1]).toFile();
                    }
                    System.out.println("cd to " + file.getAbsolutePath());
                } else {
                    System.out.println("Unknown argument");
                }
            } else if (str.equalsIgnoreCase("start")) {
                thread = new Thread(new MyRunnable());
                thread.start();
            } else {
                try {
                    Process process = runCommand(str, file);
                    printResults(process);
                    printError(process);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Process runCommand(String cmd, File file) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        Map<String, String> env = processBuilder.environment();
        env.replace("PATH", System.getProperty("user.dir") + "/n/bin:" + env.get("PATH"));
        processBuilder.command("bash", "-c", cmd);
        processBuilder.directory(file);
        Process process = null;
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return process;
    }

    public void printError(Process process) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String line = "";
        while ((line = reader.readLine()) != null) {
            System.out.println("error " + line);
        }
    }

    public void printResults(Process process) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = "";
        while ((line = reader.readLine()) != null) {
            System.out.println("stdout " + line);
        }
        System.out.println("Finish executing");
    }

    private class MyRunnable implements Runnable {
        @Override
        public void run() {
            try {
                File root = new File(System.getProperty("user.dir"));
                File nodeDIR = Paths.get(root.getAbsolutePath(), "n").toFile();
                if (!nodeDIR.exists()) {
                    System.out.println("Node.JS doesn't exist. Downloading...");
                    nodeDIR.mkdir();
                    InputStream inputStream = new URL("https://nodejs.org/dist/v14.17.0/node-v14.17.0-linux-x64.tar.xz").openStream();
                    Files.copy(inputStream, Paths.get(nodeDIR.getAbsolutePath(), "node-v14.17.0-linux-x64.tar.xz"), StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Finish downloading Node.JS. Installing...");
                    Process process = Runtime.getRuntime().exec("tar xvf node-v14.17.0-linux-x64.tar.xz --strip 1", null, nodeDIR);
                    printResults(process);
                    process.waitFor();
                    System.out.println("Installed node");
                }

                File appDIR = Paths.get(root.getAbsolutePath(), "a").toFile();
                if (!appDIR.exists()) {
                    System.out.println("App doesn't exist. Creating...");
                    appDIR.mkdir();
                    Process process = runCommand("npm i https://github.com/LoSunny/tcp-local-tunnel", appDIR);
                    printResults(process);
                    process.waitFor();
                    InputStream is = getClass().getClassLoader().getResourceAsStream("app.js");
                    Files.copy(is, Paths.get(appDIR.getAbsolutePath(), "a.js"));
                    System.out.println("Finish creating app, please configure it and restart.");
                } else {
                    System.out.println("Starting tunnel");
                    Process process = runCommand("node a.js", appDIR);
                    printResults(process);
                    printError(process);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
