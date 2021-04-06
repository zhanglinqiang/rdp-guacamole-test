package zhang;

import model.KeyboardChar;
import org.apache.commons.lang3.StringUtils;
import org.apache.guacamole.GuacamoleConnectionClosedException;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.GuacamoleServerException;
import org.apache.guacamole.io.GuacamoleWriter;
import org.apache.guacamole.net.GuacamoleTunnel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class KeyboardInputUtil {
    static Map<String, String> keyMap = new HashMap<>();
    static {
        InputStream keymapStream = KeyboardInputUtil.class.getClassLoader().getResourceAsStream("keymap");
        if(keymapStream != null){
            try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(keymapStream)))){
                String line = null;
                while ((line = bufferedReader.readLine()) != null){
                    String[] split = line.split(" ");
                    KeyboardChar keyboardChar = new KeyboardChar(split[0].charAt(0), false, Integer.parseInt(split[2]));
//                    KeyboardChar keyboardChar = new KeyboardChar(split[0].charAt(0), false, Integer.parseInt(split[2]));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {

    }

    private Logger logger = LoggerFactory.getLogger(getClass());

    public void sendCmd(GuacamoleTunnel tunnel) throws GuacamoleException {
        pressKey(tunnel, 99, 109, 100);//cmd
    }

    public void sendSpace(GuacamoleTunnel tunnel) throws GuacamoleException {
        pressKey(tunnel, 32);//space
    }

    public void sendExit(GuacamoleTunnel tunnel) throws GuacamoleException {
        pressKey(tunnel, 101, 120, 105, 116);//exit
    }

    public void sendIpconfig(GuacamoleTunnel tunnel) throws GuacamoleException {
        pressKey(tunnel, 105, 112, 99, 111, 110, 102, 105, 103);//ipconfig
    }

    public void sendAppend(GuacamoleTunnel tunnel) throws GuacamoleException {
        sendCombinationKeys(tunnel, 65505, 124);//|
    }

    public void sendClip(GuacamoleTunnel tunnel) throws GuacamoleException {
        sendCombinationKeys(tunnel, 99, 108, 105, 112);//clip
    }

    public void sendCommand(GuacamoleTunnel tunnel, String command) throws GuacamoleException {
        for (char c : command.toCharArray()) {

        }
    }

    public void sendWinAndR(GuacamoleTunnel tunnel) throws GuacamoleException {
        sendCombinationKeys(tunnel, 65515, 114);
    }

    public void sendCtrlAndSpace(GuacamoleTunnel tunnel) throws GuacamoleException {
        sendCombinationKeys(tunnel, 65507, 32);
    }

    public void sendEnter(GuacamoleTunnel tunnel) throws GuacamoleException {
        pressKey(tunnel, 65293);//enter
    }

    public void sendCombinationKeys(GuacamoleTunnel tunnel, int... keysymArray) throws GuacamoleException {
        //keydown
        for (int i = 0; i < keysymArray.length; i++) {
            sendData(tunnel, keyDown(keysymArray[i]));
        }

        //keyup
        for (int i = keysymArray.length - 1; i >= 0 ; i--) {
            sendData(tunnel, keyUp(keysymArray[i]));
        }
    }

    public void sendData(GuacamoleTunnel tunnel, String data) throws GuacamoleException {
        try {
            // Get writer from tunnel
            GuacamoleWriter writer = tunnel.acquireWriter();
            // Get input reader for HTTP stream
            Reader input = new InputStreamReader(
                    new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)), "UTF-8");
            // Transfer data from input stream to tunnel output, ensuring
            // input is always closed
            try {
                // Buffer
                int length;
                char[] buffer = new char[8192];
                // Transfer data using buffer
                while (tunnel.isOpen() &&
                        (length = input.read(buffer, 0, buffer.length)) != -1)
                    writer.write(buffer, 0, length);
            }
            // Close input stream in all cases
            finally {
                input.close();
            }
        } catch (GuacamoleConnectionClosedException e) {
            logger.debug("Connection to guacd closed.", e);
        } catch (IOException e) {
            tunnel.close();
            throw new GuacamoleServerException("I/O Error sending data to server: " + e.getMessage(), e);
        } finally {
            tunnel.releaseWriter();
        }
    }

    /**
     * @param keysymArray 键盘按键keysym码数组
     */
    public void pressKey(GuacamoleTunnel tunnel, int... keysymArray) throws GuacamoleException {
        for (int keysym : keysymArray) {
            sendData(tunnel, keyDown(keysym));
            sendData(tunnel, keyUp(keysym));
        }
    }

    public String keyDown(int keysym) {
        String[] keys = {
                getElement("key"), getElement(String.valueOf(keysym)), getElement("1"),
        };
        return StringUtils.join(keys, ",") + ";";
    }

    public String keyUp(int keysym) {
        String[] keys = {
                getElement("key"), getElement(String.valueOf(keysym)), getElement("0"),
        };
        return StringUtils.join(keys, ",") + ";";
    }

    private String getElement(String value) {
        return value.length() + "." + value;
    }
}
