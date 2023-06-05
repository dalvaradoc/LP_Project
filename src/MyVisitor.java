import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

class Row {
    String id;
    int lvl;
    String type;
    ArrayList<String> arguments;

    public Row(String id, String type, int lvl) {
        this.id = id;
        this.type = type;
        this.lvl = lvl;
        this.arguments = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "Id: " + id + ", type: " + type + ", lvl: " + lvl + ", arguments: " + arguments.toString();
    }
}

class MyList {
    ArrayList<Row> list;

    public MyList() {
        list = new ArrayList<>();
    }

    public void push(Row row){
        list.add(row);
    }

    public Row get(String id){
        for (int i = 0; i < list.size(); ++i){
            if (list.get(i).id.equals(id)){
                return list.get(i);
            }
        }
        return null;
    }

    public void addArg(String arg){
        list.get(list.size()-1).arguments.add(arg);
    }

    public Row last(){
        if (list.size() > 0)
            return list.get(list.size()-1);
        return null;
    }

    public void set(Row row){
        for (int i = 0; i < list.size(); ++i){
            if (list.get(i).id.equals(row.id)){
                list.set(i, row);
            }
        }
    }
}

public class MyVisitor<T> extends JavaGrammarBaseVisitor<T> {

//    private HashMap<String, String> table = new HashMap<>();
    private MyList table = new MyList();
    private int indentation = 0;

    private boolean areFields = false;

    private String localType = "";
    public String getInd(){
        String ret = "";
        for (int i = 0; i < indentation; ++i){
            ret += "  ";
        }
        return ret;
    }

    public void print(String s){
        if (table.last() != null && skip.contains(table.last().type) && declaring)
            return;
        System.out.print(s);
    }
    @Override
    public T visitStatement(JavaGrammarParser.StatementContext ctx) {
        if (ctx.block() == null)
            System.out.print(getInd());
        if (ctx.IF() != null){
            print("if ");
            visitParExpression(ctx.parExpression());
            visitStatement(ctx.statement(0));
            int i = 1;
            while (ctx.statement(i) != null){
                print(getInd() + "else");
                visitStatement(ctx.statement(i));
                ++i;
            }
        } else if (ctx.DO() != null) {
            print("while True");
            visitStatement(ctx.statement(0));
            ++indentation;
            print(getInd() + "if not ");
            visitParExpression(ctx.parExpression());
            System.out.println(":");
            ++ indentation;
            System.out.println(getInd() + "break");
            indentation -= 2;
        } else if (ctx.WHILE() != null) {
            print("while ");
            visitParExpression(ctx.parExpression());
            visitStatement(ctx.statement(0));
        } else if (ctx.FOR() != null) {
            visitForControl(ctx.forControl());
            visitStatement(ctx.statement(0));
        } else if (ctx.RETURN() != null) {
            print("return ");
            if (ctx.expression() != null) {
                visitExpression(ctx.expression(0));
            }
        } else if (ctx.CONTINUE() != null) {
            print("continue ");
        } else if (ctx.BREAK() != null) {
            print("break ");
        } else if (ctx.TRY() != null) {
            
        } else {
            visitChildren(ctx);
        }
        System.out.println();
        return null;
    }

    boolean builtInFun = false;

    @Override
    public T visitIdentifier(JavaGrammarParser.IdentifierContext ctx) {
        if (ctx.IDENTIFIER() != null){
            String id = ctx.IDENTIFIER().getText();
            if (id.equals("System") || id.equals("out") || id.equals("reader")){
                builtInFun = true;
                return (T) "";
            }
            if (table.get(id) != null && table.get(id).type.equals("field")){
                int lvl = indentation;
                boolean same_context = false;
                for (int i = table.list.size()-1; i >= 0; --i){
                    if (table.list.get(i).lvl < 1){
                        break;
                    }
                    if (table.list.get(i).id.equals(id))
                        same_context = true;
                }
                if (same_context)
                    return (T) ("self." + id);
                else
                    return (T) id;
            }
            return (T) ctx.IDENTIFIER().getText();
        } else if (ctx.MODULE() != null){
            return (T) ctx.MODULE().getText();
        } else {
            visitChildren(ctx);
        }
        return null;
    }

    @Override
    public T visitClassDeclaration(JavaGrammarParser.ClassDeclarationContext ctx) {
        String cid = visitIdentifier(ctx.identifier()).toString();
        print("class " + cid);
        table.push(new Row(cid, "class", indentation));
        if (ctx.EXTENDS() != null){
            String class_ext = ctx.typeType().getText();
            print("(" + class_ext + ")");
            for (int i = 0; i < table.list.size(); ++i){
                if (table.list.get(i).id.equals(class_ext)){
                    for (int j = i+1; j < table.list.size(); ++j){
                        if (table.list.get(j).type.equals("field"))
                            table.push(table.list.get(j));
                        else
                            i = table.list.size();
                            break;
                    }
                }
            }
        }
        visitClassBody(ctx.classBody());
        if (table.get("main") != null && table.get("main").type.equals("")){
            table.get("main").type = cid;
            table.get("main").lvl = indentation;
        }
        return null;
    }

    @Override
    public T visitClassBody(JavaGrammarParser.ClassBodyContext ctx) {
        print(":");
        System.out.println();
        ++indentation;
        visitChildren(ctx);
        --indentation;
        return null;
    }

    @Override
    public T visitBlock(JavaGrammarParser.BlockContext ctx) {
        print(":");
        System.out.println();
        ++indentation;
        int i = 0;
        while (ctx.blockStatement(i) != null){
            visitBlockStatement(ctx.blockStatement(i));
            ++i;
        }
        --indentation;
        System.out.println();
        return null;
    }

    @Override
    public T visitCompilationUnit(JavaGrammarParser.CompilationUnitContext ctx) {
        skip.add("Scanner");
//        print("import sys");
        System.out.println();

        visitChildren(ctx);
        if (table.get("main") == null || table.get("main").type.equals("")){
            System.out.println("Semantic error, there is no main function.");
            return null;
        }
        System.out.println("main = " + table.get("main").type + "()");
        System.out.println("main.main()");

        for (Row row : table.list) {
            System.out.println(row);
        }
        return null;
    }

    @Override
    public T visitQualifiedName(JavaGrammarParser.QualifiedNameContext ctx) {
        visitChildren(ctx);
        String qn = "";
        int i = 0;
        while (ctx.identifier(i) != null){
            qn += "." + ctx.identifier(i).getText();
            ++i;
        }
        qn = qn.substring(1);
//        System.out.println(qn);
        return null;
    }

    @Override
    public T visitImportDeclaration(JavaGrammarParser.ImportDeclarationContext ctx) {
        String qn = ctx.qualifiedName().getText();
        switch (qn){
            case "java.util.Scanner":
                print("import sys");
                System.out.println();
                break;
        }
        visitChildren(ctx);
        return null;
    }

    @Override
    public T visitLiteral(JavaGrammarParser.LiteralContext ctx) {
        visitChildren(ctx);
        if (ctx.floatLiteral() != null){
            String float_number = ctx.floatLiteral().getText();
            if (float_number.contains("f"))
                float_number = float_number.substring(0, float_number.length()-1);
            return (T) float_number;
        } else if (ctx.BOOL_LITERAL() != null) {
            if (ctx.BOOL_LITERAL().getText().equals("true"))
                return (T) "True";
            else
                return (T) "False";
        }
        table.addArg(ctx.getText());
        return (T) ctx.getText();
    }

    @Override
    public T visitPrimary(JavaGrammarParser.PrimaryContext ctx) {
        if (ctx.LPAREN() != null) {
            print("(");
            T ret = visitChildren(ctx);
            print(")");
            return ret;
        } else if (ctx.literal() != null) {
            print(visitLiteral(ctx.literal()).toString());
        } else if (ctx.identifier() != null) {
            print(visitIdentifier(ctx.identifier()).toString());
        } else {
            visitChildren(ctx);
        }
        return null;
    }

    @Override
    public T visitExpression(JavaGrammarParser.ExpressionContext ctx) {
        table.addArg(ctx.getText());
        if (ctx.LBRACK() != null){
            visitChildren(ctx.expression(0));
            print("[");
            visitChildren(ctx.expression(1));
            print("]");
        } else if (ctx.bop != null && ctx.bop.getText().equals(".")) {
            visitExpression(ctx.expression(0));
            if (!builtInFun){
                print(".");
            }
            builtInFun = false;
            if (ctx.identifier() != null){
                print(visitIdentifier(ctx.identifier()).toString());
            } else if (ctx.methodCall() != null){
                visitMethodCall(ctx.methodCall());
                builtInFun = false;
            }
        } else if (ctx.bop != null) {
            String bop = ctx.bop.getText();
            if (bop.equals("+") && printing){
                print("str(");
                visitExpression(ctx.expression(0));
                print(")");
                print(" + ");
                print("str(");
                visitExpression(ctx.expression(1));
                print(")");
                return null;
            }
            switch (bop) {
                case "+":
                case "-":
                case "*":
                case "/":
                case "%":
                case "<=":
                case ">=":
                case ">":
                case "<":
                case "==":
                case "=":
                case "+=":
                case "-=":
                case "*=":
                case "/=":
                case "%=":
                case "!=":
                    visitExpression(ctx.expression(0));
                    print(" " + bop + " ");
                    visitExpression(ctx.expression(1));
                    break;
                case "||":
                    visitExpression(ctx.expression(0));
                    print(" " + "Or" + " ");
                    visitExpression(ctx.expression(1));
                    break;
                case "&&":
                    visitExpression(ctx.expression(0));
                    print(" " + "And" + " ");
                    visitExpression(ctx.expression(1));
                    break;
                case "?":
                    visitExpression(ctx.expression(1));
                    print(" if ");
                    visitExpression(ctx.expression(0));
                    print(" else ");
                    visitExpression(ctx.expression(2));
                    break;
            }
        } else if (ctx.postfix != null) {
            visitExpression(ctx.expression(0));
            if (ctx.postfix.getText().equals("++")){
                print( " += 1");
            } else {
                print(" -= 1");
            }
        } else if (ctx.prefix != null) {
            String pf = ctx.prefix.getText();
            switch (pf){
                case "+":
                case "-":
                    print(pf);
                    visitExpression(ctx.expression(0));
                    break;
                case "!":
                    print(" not ");
                    visitExpression(ctx.expression(0));
                    break;
                case "++":
                    visitExpression(ctx.expression(0));
                    print(" += 1");
                    break;
                case "--":
                    visitExpression(ctx.expression(0));
                    print(" -= 1");
                    break;
            }
        } else {
            return visitChildren(ctx);
        }
        return null;
    }

    private boolean printing = false;

    @Override
    public T visitMethodCall(JavaGrammarParser.MethodCallContext ctx) {
        if (ctx.identifier() != null){
            String mid = ctx.identifier().getText();
            switch (mid){
                case "println":
                case "print":
                    print("print");
                    printing = true;
                    break;
                case "nextInt":
                    String id = table.last().arguments.get(1).split("[.]")[0];
                    if (table.get(id) == null || !table.get(id).type.equals("Scanner"))
                        System.out.println("Semantic error");
                    else
                        switch (table.get(id).arguments.get(1)){
                            case "System.in":
                                print("int(input())");
                        }
                    return null;
                case "printf":
                    print("print");
                    break;
                case "close":
                    return null;
                default:
                    if (table.get(mid) != null){
                        boolean same_context = false;
                        for (int i = table.list.size()-1; i >= 0; --i){
                            if (table.list.get(i).lvl < 1){
                                break;
                            }
                            if (table.list.get(i).id.equals(mid))
                                same_context = true;
                        }
                        if (same_context)
                            print("self."+mid);
                        else
                            print(mid);
                    } else {
                        print(mid);
                    }
            }
            visitArguments(ctx.arguments());
            printing = false;
        }
        return null;
    }

    @Override
    public T visitArguments(JavaGrammarParser.ArgumentsContext ctx) {
        print("(");
        visitChildren(ctx);
        print(")");
        return null;
    }

    @Override
    public T visitExpressionList(JavaGrammarParser.ExpressionListContext ctx) {
        visitExpression(ctx.expression(0));
        int i = 1;
        while (ctx.expression(i) != null){
            print(",");
            visitExpression(ctx.expression(i));
            ++i;
        }
//        return super.visitExpressionList(ctx);
        return null;
    }

    @Override
    public T visitElementValuePair(JavaGrammarParser.ElementValuePairContext ctx) {
        print(visitIdentifier(ctx.identifier()).toString());
        print(" = ");
        visitElementValue(ctx.elementValue());
        return null;
    }

    @Override
    public T visitVariableDeclarators(JavaGrammarParser.VariableDeclaratorsContext ctx) {
        visitVariableDeclarator(ctx.variableDeclarator(0));
        int i = 1;
        while (ctx.variableDeclarator(i) != null){
//            print(", ");
            System.out.println();
            print(getInd());
            visitVariableDeclarator(ctx.variableDeclarator(i));
            ++i;
        }
        return null;
    }

    private HashSet<String> skip = new HashSet<>();
    private boolean declaring = false;

    @Override
    public T visitVariableDeclarator(JavaGrammarParser.VariableDeclaratorContext ctx) {
        declaring = true;
        String id = ctx.variableDeclaratorId().getText();
        Row last = table.last();
        if (last != null && last.id.equals("")) {
            last.id = id;
            table.list.set(table.list.size() - 1, last);
        } else {
            table.push(new Row(id, "", indentation));
        }
        if (ctx.ASSIGN() != null){
            print(id);
            print(" = ");
            visitVariableInitializer(ctx.variableInitializer());
        } else if (areFields) {
            last = table.last();
            last.type = "field";
            table.set(last);
            System.out.println();
        } else {
            print(id + " = 0");
        }
        ArrayList<String> values = new ArrayList<>();
        values.add(id);
        declaring = false;
        return null;
    }

    @Override
    public T visitBlockStatement(JavaGrammarParser.BlockStatementContext ctx) {
        if (ctx.statement() == null)
            System.out.print(getInd());
        if (ctx.localVariableDeclaration() != null){
            visitLocalVariableDeclaration(ctx.localVariableDeclaration());
            System.out.println();
            return null;
        }
        return super.visitBlockStatement(ctx);
    }

    @Override
    public T visitMemberDeclaration(JavaGrammarParser.MemberDeclarationContext ctx) {
        System.out.print(getInd());
        return super.visitMemberDeclaration(ctx);
    }

    @Override
    public T visitMethodDeclaration(JavaGrammarParser.MethodDeclarationContext ctx) {
        String mid = visitIdentifier(ctx.identifier()).toString();
        print("def " + mid);
        table.push(new Row(mid, "", indentation));
        visitFormalParameters(ctx.formalParameters());
        int i = 0;
        while (ctx.LBRACK(i) != null){
            print("[]");
            ++i;
        }
        return visitMethodBody(ctx.methodBody());
    }

    @Override
    public T visitFormalParameters(JavaGrammarParser.FormalParametersContext ctx) {
        print("(self");
        if (ctx.receiverParameter() != null){
            print(",");
            visitReceiverParameter(ctx.receiverParameter());
        }
        if (ctx.formalParameterList() != null){
            print(",");
            visitFormalParameterList(ctx.formalParameterList());
        }
        print(")");
        return null;
    }

    @Override
    public T visitFormalParameterList(JavaGrammarParser.FormalParameterListContext ctx) {
        visitFormalParameter(ctx.formalParameter(0));
        int i = 1;
        while (ctx.formalParameter(i) != null){
            visitFormalParameter(ctx.formalParameter(i));
            ++i;
        }
        return null;
    }

    @Override
    public T visitFormalParameter(JavaGrammarParser.FormalParameterContext ctx) {
        if (table.last().id.equals("main")){
            return null;
        }
        print(ctx.variableDeclaratorId().getText());
        return null;
    }

    @Override
    public T visitLocalVariableDeclaration(JavaGrammarParser.LocalVariableDeclarationContext ctx) {
        if (ctx.typeType() != null){
            table.push(new Row("", ctx.typeType().getText(), indentation));
        }
        return visitChildren(ctx);
    }

    @Override
    public T visitForControl(JavaGrammarParser.ForControlContext ctx) {
        if (ctx.forInit() != null){
            visitForInit(ctx.forInit());
            print(getInd() + "while ");
            visitExpression(ctx.expression());
            ++indentation;
            print(getInd());
            visitExpressionList(ctx.expressionList());
            --indentation;
        } else if (ctx.enhancedForControl() != null){
            print("for ");
            visitEnhancedForControl(ctx.enhancedForControl());
        }
        return null;
    }

    @Override
    public T visitEnhancedForControl(JavaGrammarParser.EnhancedForControlContext ctx) {
        print(ctx.variableDeclaratorId().getText());
        print(" in ");
        visitExpression(ctx.expression());
        return null;
    }

    @Override
    public T visitFieldDeclaration(JavaGrammarParser.FieldDeclarationContext ctx) {
        areFields = true;
        visitChildren(ctx);
        areFields = false;
        return null;
    }

    @Override
    public T visitCreatedName(JavaGrammarParser.CreatedNameContext ctx) {
        print(visitIdentifier(ctx.identifier(0)).toString());
        return null;
    }
}
