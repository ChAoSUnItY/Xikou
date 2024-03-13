package github.io.chaosunity.xikou.api;

import github.io.chaosunity.xikou.ast.XkFile;
import github.io.chaosunity.xikou.gen.JvmGen;
import github.io.chaosunity.xikou.parser.Parser;
import github.io.chaosunity.xikou.resolver.Resolver;
import java.io.IOException;
import java.nio.file.Path;

public final class XikouApi {

  public static void compileFiles(Path outputFolder, Path... filePaths) {
    int fileCount = filePaths.length;
    XkFile[] files = new XkFile[fileCount];

    for (int i = 0; i < fileCount; i++) {
      try {
        Parser parser = new Parser(filePaths[i]);
        files[i] = parser.parseFile();
      } catch (IOException ignored) {
      }
    }

    Resolver resolver = new Resolver(files);
    files = resolver.resolve();

    JvmGen jvmGen = new JvmGen(outputFolder, files);
    jvmGen.gen();
  }
}
