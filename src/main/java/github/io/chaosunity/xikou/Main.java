package github.io.chaosunity.xikou;

import github.io.chaosunity.xikou.ast.XkFile;
import github.io.chaosunity.xikou.gen.JvmGen;
import github.io.chaosunity.xikou.parser.Parser;
import github.io.chaosunity.xikou.resolver.Resolver;
import java.io.IOException;
import java.nio.file.Paths;

public class Main {

  public static void main(String[] args) throws IOException {
    Parser parser = new Parser(Paths.get("example/test.xk"));
    XkFile parsedFile = parser.parseFile();
    XkFile resolvedFile = new Resolver(parsedFile).resolve();
    JvmGen gen = new JvmGen(Paths.get("output"), resolvedFile);
    gen.gen();
  }
}