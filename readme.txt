copy from: https://segmentfault.com/a/1190000002704669
一般的工具只能分析代码，不能改变代码，除了IDE的重构功能。但我们还是有办法实现的。

不想让黑科技失传，趁着Java 7还在广泛使用，赶紧写下来(可能无法支持Java 8)。

这个小框架让你看文章前就能上手，快速对代码库做分析/改写，性能很高： https://github.com/sorra/exia

下面介绍经过验证的具体技术，能局部修改代码，调API就行了(感谢Eclipse)。文档里很难查到这些，痛的回忆…… (有句名言说: 画一条线值1美元，知道在哪画线值9999美元。)

核心代码如下:

import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

CompilationUnit cu = parseAST(...); //parse方法参见系列文章
cu.recordModifications(); //开始记录AST变化事件
doChangesOnAST(...); //直接在树上改变结点，参见系列文章
Document document = new Document(content);
TextEdit edits = cu.rewrite(document, formatterOptions); //树上的变化生成了像diff一样的东西
edits.apply(document); //应用diff
return document.get(); //得到新的代码，未改动的部分几乎都保持原样
我用的formatterOptions:

    private static final Map<String, String> formatterOptions = DefaultCodeFormatterConstants.getEclipseDefaultSettings();
    static {
        formatterOptions.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_7);
        formatterOptions.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_7);
        formatterOptions.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_7);
        formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
        formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "2");
        formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_LINE_SPLIT, "100");
        formatterOptions.put(DefaultCodeFormatterConstants.FORMATTER_JOIN_LINES_IN_COMMENTS, DefaultCodeFormatterConstants.FALSE);
        // change the option to wrap each enum constant on a new line
        formatterOptions.put(
            DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_ENUM_CONSTANTS,
            DefaultCodeFormatterConstants.createAlignmentValue(
            true,
            DefaultCodeFormatterConstants.WRAP_ONE_PER_LINE,
            DefaultCodeFormatterConstants.INDENT_ON_COLUMN));
    }
如果改动幅度很大，被改的代码可能会缩进混乱。忍一忍吧，这套API原本会把代码改错，我定位到bug，提给Eclipse，他们发现问题很深，最后没什么办法，只能牺牲缩进换来代码正确性。

由于以上原因，这套便利的API在Java 8不再保证支持。据说只能用原始的ListRewrite来改代码…… 珍惜着用吧。

最后再介绍两个便利方法：

ASTNode#delete()
结点能把自身从树上移除。调这个方法不需要知道parent结点的类型，用起来就知道方便了。

replaceNode
我仿写的方法，能任意替换一个结点，不需要知道parent结点的类型。

    public static void replaceNode(ASTNode old, ASTNode neo) {
      StructuralPropertyDescriptor p = old.getLocationInParent();
      if (p == null) {
          // node is unparented
          return;
      }
      if (p.isChildProperty()) {
          old.getParent().setStructuralProperty(p, neo);
          return;
      }
      if (p.isChildListProperty()) {
          List l = (List) old.getParent().getStructuralProperty(p);
          l.set(l.indexOf(old), neo);
      }
    }
