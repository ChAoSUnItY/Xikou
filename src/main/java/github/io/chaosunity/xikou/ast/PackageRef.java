package github.io.chaosunity.xikou.ast;

public final class PackageRef {

  public final String qualifiedPath;

  public PackageRef(String qualifiedPath) {
    this.qualifiedPath = qualifiedPath;
  }

  @Override
  public String toString() {
    return "PackageRef{" + "qualifiedPath='" + qualifiedPath + '\'' + '}';
  }
}
