package github.io.chaosunity.xikou.api;

import github.io.chaosunity.xikou.ast.XkFile;
import github.io.chaosunity.xikou.gen.JvmGen;
import github.io.chaosunity.xikou.parser.Parser;
import github.io.chaosunity.xikou.resolver.Resolver;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.stream.Stream;

public final class XikouApi {

  public static void compileFiles(Path outputFolder, Path... filePaths) {
    int fileCount = 0;
    XkFile[] files = new XkFile[filePaths.length];

    for (int i = 0; i < filePaths.length; i++) {
      if (Files.isDirectory(filePaths[i])) {
        try (Stream fileStream = Files.walk(filePaths[i])) {
          Iterator fileIter = fileStream.iterator();

          while (fileIter.hasNext()) {
            Path filePath = (Path) fileIter.next();

            if (Files.isDirectory(filePath)) {
              continue;
            }

            if (fileCount >= files.length) {
              XkFile[] newArr = new XkFile[fileCount * 2];
              System.arraycopy(files, 0, newArr, 0, fileCount);
              files = newArr;
            }

            files[fileCount++] = parseFile(filePath);
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      } else {
        try {
          if (fileCount >= files.length) {
            XkFile[] newArr = new XkFile[fileCount * 2];
            System.arraycopy(files, 0, newArr, 0, fileCount);
            files = newArr;
          }

          files[fileCount++] = parseFile(filePaths[i]);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    Resolver resolver = new Resolver(files);
    files = resolver.resolve();

    JvmGen jvmGen = new JvmGen(outputFolder, files);
    jvmGen.gen();
  }

  private static XkFile parseFile(Path filePath) throws IOException {
    byte[] fileBytes = Files.readAllBytes(filePath);
    String source = new String(fileBytes, StandardCharsets.UTF_8);
    Parser parser = new Parser(filePath, source);
    return parser.parseFile();
  }
}
