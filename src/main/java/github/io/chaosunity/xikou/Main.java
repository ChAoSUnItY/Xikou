package github.io.chaosunity.xikou;

import github.io.chaosunity.xikou.gen.JvmGen;
import github.io.chaosunity.xikou.model.XkFile;
import github.io.chaosunity.xikou.parser.Parser;

import java.io.IOException;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException {
        Parser parser = new Parser(Paths.get("example/main.xk"));
        XkFile xkFile = parser.parseFile();
        
        System.out.println(xkFile);

        JvmGen gen = new JvmGen(Paths.get("output"), xkFile);
        gen.gen();
    }
}