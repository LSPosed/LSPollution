package org.lsposed.lspollution.testing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

/**
 * Used to execute shell cmd
 * <p>
 * Created by YangJing on 2019/04/11 .
 * Email: yangjing.yeoh@bytedance.com
 */
public class ProcessThread extends Thread {


    private final InputStream is;
    private final String printType;

    ProcessThread(InputStream is, String printType) {
        this.is = is;
        this.printType = printType;
    }

    public static boolean execute(String cmd) {
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            new ProcessThread(process.getInputStream(), "INFO").start();
            new ProcessThread(process.getErrorStream(), "ERR").start();
            int value = process.waitFor();
            return value == 0;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean execute(String cmd, Object... objects) {
        return execute(String.format(cmd, objects));
    }

    @Override
    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(printType + ">" + line);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
