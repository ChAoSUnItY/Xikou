package github.io.chaosunity.xikou;

import github.io.chaosunity.xikou.api.XikouApi;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

  public static void main(String[] args) {
    Path[] filePaths = new Path[args.length - 1];
    Path outputFolder;

    for (int i = 0; i < args.length - 1; i++) {
      filePaths[i] = Paths.get(args[i]);
    }

    outputFolder = Paths.get(args[args.length - 1]);

    XikouApi.compileFiles(outputFolder, filePaths);
  }
}
