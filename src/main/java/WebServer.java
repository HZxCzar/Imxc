import com.sun.net.httpserver.*;
import Compiler.Src.IncrementalCompile;
import java.io.*;
import java.nio.file.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;

public class WebServer {
    static final String SRC_DIR = "src/test/in";
    static final String FINAL_OUT = "test.s";

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // 上传代码文件(POST /upload?path=a/b/main.mx)
        server.createContext("/upload", (exchange) -> {
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1); return;
            }
            String query = exchange.getRequestURI().getQuery();
            String relPath = null;
            if (query != null) {
                for (String p : query.split("&")) {
                    if (p.startsWith("path=")) {
                        relPath = URLDecoder.decode(p.substring(5), "UTF-8");
                    }
                }
            }
            // 参数检查
            if (relPath == null || relPath.contains("..") || relPath.startsWith("/") || relPath.matches(".*[<>|:*?\"].*")) {
                exchange.sendResponseHeaders(400, -1); return;
            }
            Path outPath = Paths.get(SRC_DIR, relPath);
            Files.createDirectories(outPath.getParent());
            try (OutputStream os = Files.newOutputStream(outPath);
                InputStream is = exchange.getRequestBody()) {
                byte[] buf = new byte[4096];
                int len;
                while ((len = is.read(buf)) != -1) {
                    os.write(buf, 0, len);
                }
            }
            String resp = "Upload OK: " + outPath;
            exchange.sendResponseHeaders(200, resp.getBytes().length);
            exchange.getResponseBody().write(resp.getBytes());
            exchange.close();
        });

        // /compile 调用
        server.createContext("/compile", (exchange) -> {
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1); return;
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            PrintStream oldOut = System.out;
            PrintStream oldErr = System.err;
            String resp;
            int code = 200;
            try {
                System.setOut(ps);
                System.setErr(ps);
                IncrementalCompile.main(new String[0]);
                resp = baos.toString("UTF-8");
                if (resp.isEmpty()) {
                    resp = "编译成功, 但无输出信息。";
                }
            } catch(Exception e) {
                code = 500;
                // 输出异常和捕获日志
                resp = "编译异常:\n" + baos.toString("UTF-8") + "\n" + e.toString();
            } finally {
                System.setOut(oldOut);
                System.setErr(oldErr);
                ps.close();
            }
            byte[] respBytes = resp.getBytes("UTF-8");
            exchange.sendResponseHeaders(code, respBytes.length);
            exchange.getResponseBody().write(respBytes);
            exchange.close();
        });

        // 下载汇编文件(GET /download)
        server.createContext("/download", (exchange) -> {
            if (!"GET".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1); return;
            }
            Path asmPath = Paths.get(FINAL_OUT);
            if(!Files.exists(asmPath)) {
                String resp = "no asm output (please compile first)";
                exchange.sendResponseHeaders(404, resp.getBytes().length);
                exchange.getResponseBody().write(resp.getBytes());
            } else {
                byte[] data = Files.readAllBytes(asmPath);
                exchange.getResponseHeaders().set("Content-Type", "application/octet-stream");
                exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"test.s\"");
                exchange.sendResponseHeaders(200, data.length);
                exchange.getResponseBody().write(data);
            }
            exchange.close();
        });

        System.out.println("服务已启动, 请访问: http://localhost:8080/ (上传/upload?filename= , 编译/compile , 下载/download)");
        server.setExecutor(null); // 单线程
        server.start();
    }
}