package org.scoula.finance.util;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class PythonExecutorUtil {

    // ---------- 요청별 작업폴더 ----------
    public static final class JobWorkspace {
        public final File root;  // ex) .../WEB-INF/classes/python/jobs/<uuid>

        private JobWorkspace(File root) { this.root = root; }

        public File resolve(String relative) { return new File(root, relative); }

        public void mkdirsFor(File f) {
            File p = f.getParentFile();
            if (p != null) p.mkdirs();
        }

        public void cleanupQuietly() {
            try { deleteRecursively(root); } catch (Exception ignore) {}
        }
    }

    public static JobWorkspace createJobWorkspace(File pyRoot) {
        File jobs = new File(pyRoot, "jobs");
        File ws = new File(jobs, UUID.randomUUID().toString());
        ws.mkdirs();
        return new JobWorkspace(ws);
    }

    // ---------- 실행 유틸 ----------
    public static void runPythonScript(String scriptAbsolutePath, File workingDir, String... args)
            throws IOException, InterruptedException {

        List<String> cmd = new ArrayList<>();
        cmd.add("python");
        cmd.add(scriptAbsolutePath);
        if (args != null) Collections.addAll(cmd, args);

        ProcessBuilder pb = new ProcessBuilder(cmd);
        if (workingDir != null) pb.directory(workingDir);      // ★ CWD = 작업폴더
        pb.redirectErrorStream(true);

        Process p = pb.start();

        try (BufferedReader r =
                     new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) System.out.println("[Python] " + line);
        }

        int code = p.waitFor();
        if (code != 0) throw new RuntimeException("python 실행 실패 (exit code: " + code + ")");
    }

    // WAR 언팩이면 getFile(); 아니면 임시복사 폴백
    public static File asFileOrTemp(ClassPathResource res) throws IOException {
        try {
            return res.getFile();
        } catch (Exception ignore) {
            File tmp = File.createTempFile("res-", "-" + new File(res.getPath()).getName());
            try (InputStream in = res.getInputStream(); OutputStream out = new FileOutputStream(tmp)) {
                in.transferTo(out);
            }
            tmp.deleteOnExit();
            return tmp;
        }
    }

    public static File getPyRootFrom(ClassPathResource res) throws IOException {
        File script = asFileOrTemp(res);
        return script.getParentFile().getParentFile(); // .../python
    }

    private static void deleteRecursively(File f) {
        if (f == null || !f.exists()) return;
        if (f.isDirectory()) {
            File[] children = f.listFiles();
            if (children != null) for (File c : children) deleteRecursively(c);
        }
        if (!f.delete()) f.deleteOnExit();
    }
}
