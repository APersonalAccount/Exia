package github.exia.filewalker;

import java.io.File;

import org.eclipse.jdt.core.dom.CompilationUnit;

public interface AstFunction {
  /**
   * operate on cu
   * @param cu the compilation unit
   * @param file TODO
   * @return whether cu is modified
   */
  boolean doAndModify(CompilationUnit cu, File file);
}
