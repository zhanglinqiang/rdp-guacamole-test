//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.apache.guacamole.io;

import java.io.IOException;
import java.io.Reader;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedList;

import org.apache.guacamole.GuacamoleConnectionClosedException;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.GuacamoleServerException;
import org.apache.guacamole.GuacamoleUpstreamTimeoutException;
import org.apache.guacamole.protocol.GuacamoleInstruction;

public class ReaderGuacamoleReader implements GuacamoleReader {
    private Reader input;
    private int parseStart;
    private char[] buffer = new char[20480];
    private int usedLength = 0;

    public ReaderGuacamoleReader(Reader input) {
        this.input = input;
    }

    public boolean available() throws GuacamoleException {
        try {
            return this.input.ready() || this.usedLength != 0;
        } catch (IOException var2) {
            throw new GuacamoleServerException(var2);
        }
    }

    int clipStreamIndex = -1;
    public char[] read() throws GuacamoleException {
        try {
            while(true) {
                int elementLength = 0;
                int i = this.parseStart;

                while(i < this.usedLength) {
                    char readChar = this.buffer[i++];
                    if (readChar >= '0' && readChar <= '9') {
                        elementLength = elementLength * 10 + readChar - 48;
                    } else {
                        if (readChar != '.') {
                            throw new GuacamoleServerException("Non-numeric character in element length.");
                        }

                        if (i + elementLength >= this.usedLength) {
                            break;
                        }

                        char terminator = this.buffer[i + elementLength];
                        i += elementLength + 1;
                        elementLength = 0;
                        this.parseStart = i;
                        if (terminator == ';') {
                            char[] instruction = new char[i];
                            System.arraycopy(this.buffer, 0, instruction, 0, i);
                            this.usedLength -= i;
                            this.parseStart = 0;
                            System.arraycopy(this.buffer, i, this.buffer, 0, this.usedLength);
                            return instruction;
                        }

                        if (terminator != ',') {
                            throw new GuacamoleServerException("Element terminator of instruction was not ';' nor ','");
                        }
                    }
                }

                if (this.usedLength > this.buffer.length / 2) {
                    char[] biggerBuffer = new char[this.buffer.length * 2];
                    System.arraycopy(this.buffer, 0, biggerBuffer, 0, this.usedLength);
                    this.buffer = biggerBuffer;
                }

                int numRead = this.input.read(this.buffer, this.usedLength, this.buffer.length - this.usedLength);
                if (numRead == -1) {
                    return null;
                }

                String str = new String(buffer);
                if(str.contains("clipboard")){
                    int start = str.indexOf("clipboard");
                    int end = str.indexOf(";", start);
                    try {
                        String substring = str.substring(start, end);
                        clipStreamIndex = Integer.parseInt(substring.split(",")[1].split("\\.")[1]);
                        System.out.println(substring);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    System.out.println(buffer);
                }

                if(str.contains("4.blob")){
                    int start = str.indexOf("4.blob");
                    int end = str.indexOf(";", start);
                    String substring = str.substring(start, end < 0 ? str.length() : end);
                    int streamIndex = Integer.parseInt(substring.split(",")[1].split("\\.")[1]);
                    if(clipStreamIndex != -1){
                        try {
                            if(streamIndex == clipStreamIndex){
                                String base64 = substring.split(",")[2].split("\\.")[1];
                                String string = new String(Base64.getDecoder().decode(base64.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
                                System.out.println(string);
                                System.out.println("***************************************************************");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }else{
                        System.out.println("stream index: " + streamIndex);
                    }
//                                System.out.println(buffer);
                }

                this.usedLength += numRead;
            }
        } catch (SocketTimeoutException var6) {
            throw new GuacamoleUpstreamTimeoutException("Connection to guacd timed out.", var6);
        } catch (SocketException var7) {
            throw new GuacamoleConnectionClosedException("Connection to guacd is closed.", var7);
        } catch (IOException var8) {
            throw new GuacamoleServerException(var8);
        }
    }

    public GuacamoleInstruction readInstruction() throws GuacamoleException {
        char[] instructionBuffer = this.read();
        if (instructionBuffer == null) {
            return null;
        } else {
            int elementStart = 0;
            LinkedList elements = new LinkedList();

            while(elementStart < instructionBuffer.length) {
                int lengthEnd = -1;

                int length;
                for(length = elementStart; length < instructionBuffer.length; ++length) {
                    if (instructionBuffer[length] == '.') {
                        lengthEnd = length;
                        break;
                    }
                }

                if (lengthEnd == -1) {
                    throw new GuacamoleServerException("Read returned incomplete instruction.");
                }

                length = Integer.parseInt(new String(instructionBuffer, elementStart, lengthEnd - elementStart));
                elementStart = lengthEnd + 1;
                String element = new String(instructionBuffer, elementStart, length);
                elements.addLast(element);
                elementStart += length;
                char terminator = instructionBuffer[elementStart];
                ++elementStart;
                if (terminator == ';') {
                    break;
                }
            }

            String opcode = (String)elements.removeFirst();
            GuacamoleInstruction instruction = new GuacamoleInstruction(opcode, (String[])elements.toArray(new String[elements.size()]));
            return instruction;
        }
    }
}
