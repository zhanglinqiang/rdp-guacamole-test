import org.apache.guacamole.GuacamoleConnectionClosedException;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.GuacamoleResourceNotFoundException;
import org.apache.guacamole.io.GuacamoleReader;
import org.apache.guacamole.net.GuacamoleSocket;
import org.apache.guacamole.net.GuacamoleTunnel;
import org.apache.guacamole.net.InetGuacamoleSocket;
import org.apache.guacamole.net.SimpleGuacamoleTunnel;
import org.apache.guacamole.protocol.ConfiguredGuacamoleSocket;
import org.apache.guacamole.protocol.GuacamoleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zhang.KeyboardInputUtil;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Timer;
import java.util.TimerTask;

public class LocalTest {
    static Logger logger = LoggerFactory.getLogger(LocalTest.class);
    static KeyboardInputUtil keyboardInputUtil = new KeyboardInputUtil();
    public static void main(String[] args) throws Exception {
        for (int i= '0' ; i<= '9'; i++){
            char c = (char)i;
            System.out.println(c+" "+i);
        }
        if(1==1)
            return;

        // Create our configuration
        GuacamoleConfiguration config = new GuacamoleConfiguration();
        config.setProtocol("rdp");
        config.setParameter("port", "3389");
        /*config.setParameter("hostname", "10.66.33.98");
        config.setParameter("username", "administrator");
        config.setParameter("password", "Talent123");*/
        config.setParameter("hostname", "2001::43");
        config.setParameter("username", "leen");
        config.setParameter("password", "pwd@123");
        /*config.setParameter("hostname", "10.7.165.4");
        config.setParameter("username", "administrator");
        config.setParameter("password", "Talent123");*/
        config.setParameter("ignore-cert", "true");
        config.setParameter("disable-glyph-caching", "true");
        config.setParameter("disable-copy", "false");
        config.setParameter("disable-paste", "false");
        //暂时没发现具体用处
//        config.setParameter("console", "true");
        //需注册表放开策略
//        config.setParameter("initial-program", "C:\\Windows\\System32\\cmd.exe");
//        config.setParameter("en-us-qwerty", "en-us-qwerty");

        // Connect to guacd - everything is hard-coded here.
        GuacamoleSocket socket = new ConfiguredGuacamoleSocket(
                new InetGuacamoleSocket("localhost", 4822),
                config
        );

        // Return a new tunnel which uses the connected socket
        SimpleGuacamoleTunnel guacamoleTunnel = new SimpleGuacamoleTunnel(socket);


        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    keyboardInputUtil.sendWinAndR(guacamoleTunnel);
                    Thread.sleep(1000);
                    keyboardInputUtil.sendCtrlAndSpace(guacamoleTunnel);
                    Thread.sleep(1000);
                    keyboardInputUtil.sendCmd(guacamoleTunnel);
                    Thread.sleep(1000);
                    keyboardInputUtil.sendEnter(guacamoleTunnel);
                    Thread.sleep(1000);
                    keyboardInputUtil.sendIpconfig(guacamoleTunnel);
                    Thread.sleep(1000);
                    keyboardInputUtil.sendAppend(guacamoleTunnel);
                    Thread.sleep(1000);
                    keyboardInputUtil.sendClip(guacamoleTunnel);
                    Thread.sleep(1000);
                    keyboardInputUtil.sendEnter(guacamoleTunnel);
                    Thread.sleep(1000);
                    keyboardInputUtil.sendExit(guacamoleTunnel);
                    Thread.sleep(1000);
                    keyboardInputUtil.sendEnter(guacamoleTunnel);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 10000);

        Thread thread = new Thread(()->{
          while (true){
              try {
                  Thread.sleep(1);
                  doRead(guacamoleTunnel);
              } catch (Exception e) {
                  e.printStackTrace();
              }
          }
        });
        thread.start();
    }

    public static void doRead(GuacamoleTunnel tunnel) throws GuacamoleException {
        if (!tunnel.isOpen()) {
            throw new GuacamoleResourceNotFoundException("Tunnel is closed.");
        } else {
            GuacamoleReader reader = tunnel.acquireReader();

            try {
//                response.setContentType("application/octet-stream");
//                response.setHeader("Cache-Control", "no-cache");
//                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8"));

                try {
                    char[] message = reader.read();
                    if (message == null) {
                        throw new GuacamoleConnectionClosedException("Tunnel reached end of stream.");
                    }
                    String trim = new String(message).trim();
                    if(trim.contains("4.sync")){
                        int start = trim.indexOf("4.sync");
                        int end = trim.indexOf(";", start);
                        String sync = trim.substring(start, end > 0 ? end : trim.length()) + ";";
                        keyboardInputUtil.sendData(tunnel, sync);
                    }
//                    System.out.println(trim);
//                    do {
//                        out.write(message, 0, message.length);
//                        if (!reader.available()) {
//                            out.flush();
//                            response.flushBuffer();
//                        }
//                    } while(!tunnel.hasQueuedReaderThreads() && tunnel.isOpen() && (message = reader.read()) != null);

                    if (message == null) {
//                        this.deregisterTunnel(tunnel);
                        tunnel.close();
                    }

//                    out.write("0.;");
//                    out.flush();
//                    response.flushBuffer();
                } catch (GuacamoleConnectionClosedException var20) {
//                    this.deregisterTunnel(tunnel);
                    tunnel.close();
//                    out.write("0.;");
//                    out.flush();
//                    response.flushBuffer();
                } catch (GuacamoleException var21) {
//                    this.deregisterTunnel(tunnel);
                    tunnel.close();
                    throw var21;
                } finally {
//                    out.close();
                }
            } finally {
                tunnel.releaseReader();
            }

        }
    }
}
