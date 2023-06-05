import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Main {

    public static void main(String [] args) throws Exception{
        JavaGrammarLexer lexer;
        if (args.length>0){
            lexer = new JavaGrammarLexer(CharStreams.fromFileName(args[0]));
        } else {
            lexer = new JavaGrammarLexer(CharStreams.fromStream(System.in));
        }
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JavaGrammarParser parser = new JavaGrammarParser(tokens);
        ParseTree tree = parser.compilationUnit();

        MyVisitor loader = new MyVisitor();
        loader.visit(tree);
    }
}