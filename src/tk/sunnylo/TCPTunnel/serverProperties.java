package tk.sunnylo.TCPTunnel;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class serverProperties {
    String result = "";
    InputStream inputStream;

    public String getPort() {
        try {
            Properties prop = new Properties();
            String propFileName = "server.properties";

            inputStream = new FileInputStream(propFileName);
            prop.load(inputStream);
            result = prop.getProperty("server-port");
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
