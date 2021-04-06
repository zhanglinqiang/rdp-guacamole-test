package zhang;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.guacamole.GuacamoleConnectionClosedException;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.GuacamoleServerException;
import org.apache.guacamole.io.GuacamoleReader;
import org.apache.guacamole.io.GuacamoleWriter;
import org.apache.guacamole.net.GuacamoleSocket;
import org.apache.guacamole.net.GuacamoleTunnel;
import org.apache.guacamole.net.InetGuacamoleSocket;
import org.apache.guacamole.net.SimpleGuacamoleTunnel;
import org.apache.guacamole.protocol.ConfiguredGuacamoleSocket;
import org.apache.guacamole.protocol.GuacamoleConfiguration;
import org.apache.guacamole.servlet.GuacamoleHTTPTunnelServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;

public class TutorialGuacamoleTunnelServlet
        extends GuacamoleHTTPTunnelServlet {

    private GuacamoleTunnel guacamoleTunnel;
    private Timer timer = new Timer();
    private KeyboardInputUtil keyboardInputUtil = new KeyboardInputUtil();
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected GuacamoleTunnel doConnect(HttpServletRequest request)
            throws GuacamoleException {

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
        SimpleGuacamoleTunnel simpleGuacamoleTunnel = new SimpleGuacamoleTunnel(socket);
        guacamoleTunnel = simpleGuacamoleTunnel;

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
                    logger.error(e.getMessage(), e);
                }
            }
        }, 10000, 10 * 1000);
        return simpleGuacamoleTunnel;

    }

    protected void doWrite(HttpServletRequest request, HttpServletResponse response, String tunnelUUID) throws GuacamoleException {
        GuacamoleTunnel tunnel = this.getTunnel(tunnelUUID);
        response.setContentType("application/octet-stream");
        response.setHeader("Cache-Control", "no-cache");
        response.setContentLength(0);

        try {
            GuacamoleWriter writer = tunnel.acquireWriter();
            InputStreamReader input = new InputStreamReader(request.getInputStream(), "UTF-8");

            try {
                char[] buffer = new char[8192];

                int length;
                while(tunnel.isOpen() && (length = input.read(buffer, 0, buffer.length)) != -1) {
                    writer.write(buffer, 0, length);
                }

                if(new String(buffer).trim().length() > 0){
                    File file = new File("/home/leen/test.txt");
                    try(BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8))){
                        bufferedWriter.write(new String(buffer).trim());
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                    }
                }
            } finally {
                input.close();
            }
        } catch (GuacamoleConnectionClosedException var20) {
            this.logger.debug("Connection to guacd closed.", var20);
        } catch (IOException var21) {
            this.deregisterTunnel(tunnel);
            tunnel.close();
            throw new GuacamoleServerException("I/O Error sending data to server: " + var21.getMessage(), var21);
        } finally {
            tunnel.releaseWriter();
        }

    }
}
