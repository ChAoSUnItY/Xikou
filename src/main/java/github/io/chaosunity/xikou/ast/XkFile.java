package github.io.chaosunity.xikou.ast;

import java.nio.file.Path;

public class XkFile {

  public final Path absoluteFilePath;
  public final PackageRef packageRef;
  public final int declCount;
  public final BoundableDecl[] decls;

  public XkFile(Path absoluteFilePath, PackageRef packageRef, int declCount,
      BoundableDecl[] decls) {
    this.absoluteFilePath = absoluteFilePath;
    this.packageRef = packageRef;
    this.declCount = declCount;
    this.decls = decls;
  }
}
