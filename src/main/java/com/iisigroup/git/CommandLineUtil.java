package com.iisigroup.git;

import com.iisigroup.git.model.CommandResult;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author AndyChen
 * @version <ul>
 * <li>2019-06-21 AndyChen,new
 * </ul>
 * @since 2019-06-21
 */
public class CommandLineUtil {

    private static final Logger logger = Logger.getLogger(CommandLineUtil.class);

    public static CommandResult commonCommand(File executeFolder, String[] commands) {
        CommandResult commandResult = new CommandResult();
        try {
            // use Process builder
            ProcessBuilder processBuilder = new ProcessBuilder(commands);
            processBuilder.directory(executeFolder);
            List<String> commandList = Arrays.asList(commands);
            logger.info("Command exec code: " + commandList);

            Process process = processBuilder.start();
            commandResult.setCommandStr(commandList.toString());

            CountDownLatch cdl = new CountDownLatch(2);
            CommandOutput commandOutput = new CommandOutput(process.getInputStream(), commandResult.getOutputMsg(), cdl);
            CommandOutput commandOutputError = new CommandOutput(process.getErrorStream(), commandResult.getErrorMsg(), cdl);

            executorService.submit(commandOutput);
            executorService.submit(commandOutputError);
            int resultCode = process.waitFor();
            logger.info("Command return code: " + resultCode);

            cdl.await(60, TimeUnit.SECONDS);
            commandResult.setResultCode(resultCode);
            if(!"".equals(commandResult.getOutputMsg().toString().trim()))
                logger.info("Success message: " + commandResult.getOutputMsg().toString());
            if(!"".equals(commandResult.getErrorMsg().toString().trim()))
                logger.info("Error message: " + commandResult.getErrorMsg().toString());

        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
        return commandResult;
    }

    private static ExecutorService executorService = Executors.newFixedThreadPool(2, r -> {
        Thread t = Executors.defaultThreadFactory().newThread(r);
        t.setDaemon(true);
        return t;
    });


    private static class CommandOutput implements Runnable {

        private InputStream inputStream;
        private StringBuffer commandResult;
        private CountDownLatch cdl;

        CommandOutput(InputStream is, StringBuffer commandResult, CountDownLatch cdl) {
            this.inputStream = is;
            this.commandResult = commandResult;
            this.cdl = cdl;
        }

        public void run() {
            final String charset = "UTF-8";
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset))) {

                StringBuilder stringBuffer = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuffer.append(line).append(System.lineSeparator());
                }
                commandResult.append(stringBuffer);

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                cdl.countDown();
            }
        }
    }
}
