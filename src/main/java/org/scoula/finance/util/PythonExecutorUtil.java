package org.scoula.finance.util;

import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class PythonExecutorUtil {

    public static void runPythonScript(String scriptPath, String... args) throws IOException, InterruptedException {

        ProcessBuilder builder = new ProcessBuilder();
        builder.command(buildCommand(scriptPath, args));
        builder.redirectErrorStream(true);

        Process process = builder.start();

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while((line = reader.readLine()) != null) {
                System.out.println("[Python} " + line);
            }
        }

        int exitCode = process.waitFor();
        if(exitCode != 0) {
            throw new RuntimeException("python 실행 실패 (exit code: " + exitCode + ")");
        }
    }

    private static List<String> buildCommand(String scriptPath, String[] args){
        List<String> command = new ArrayList<>();
        command.add("python");
        command.add(scriptPath);
        for(String arg : args){
            command.add(arg);
        }
        return command;
    }
}
