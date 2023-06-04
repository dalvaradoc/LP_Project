import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Main {

    public static void main(String [] args) throws Exception{
        JavaGrammarLexer lexer = new JavaGrammarLexer(CharStreams.fromFileName("input/input.txt"));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JavaGrammarParser parser = new JavaGrammarParser(tokens);
        ParseTree tree = parser.compilationUnit();

        MyVisitor loader = new MyVisitor();
        loader.visit(tree);
    }
}