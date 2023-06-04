public class MyVisitor<T> extends JavaGrammarBaseVisitor<T> {
    private int indentation = 0;
    public String getInd(){
        String ret = "";
        for (int i = 0; i < indentation; ++i){
            ret += "  ";
        }
        return ret;
    }
    @Override
    public T visitStatement(JavaGrammarParser.StatementContext ctx) {
        if (ctx.block() == null)
            System.out.print(getInd());
        if (ctx.IF() != null){
            System.out.print("if ");
            visitParExpression(ctx.parExpression());
            visitStatement(ctx.statement(0));
            int i = 1;
            while (ctx.statement(i) != null){
                System.out.print("else");
                visitStatement(ctx.statement(i));
                ++i;
            }
        } else {
            visitChildren(ctx);
        }
        return null;
    }

    @Override
    public T visitIdentifier(JavaGrammarParser.IdentifierContext ctx) {
        if (ctx.IDENTIFIER() != null){
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
        if (ctx.CLASS() != null){
            System.out.print("class " + visitIdentifier(ctx.identifier()));
        }
        visitClassBody(ctx.classBody());
        return null;
    }

    @Override
    public T visitClassBody(JavaGrammarParser.ClassBodyContext ctx) {
        System.out.println(":");
        ++indentation;
        visitChildren(ctx);
        --indentation;
        return null;
    }

    @Override
    public T visitBlock(JavaGrammarParser.BlockContext ctx) {
        System.out.println(":");
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
        visitChildren(ctx);
        System.out.println("main()");
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
//        String qn = ctx.qualifiedName().getText();
//        switch (qn){
//            case "java.util.Scanner":
//                System.out.println("import sys");
//                break;
//        }
        visitChildren(ctx);
        return null;
    }

    @Override
    public T visitLiteral(JavaGrammarParser.LiteralContext ctx) {
        visitChildren(ctx);
        return (T) ctx.getText();
    }

    @Override
    public T visitPrimary(JavaGrammarParser.PrimaryContext ctx) {
        if (ctx.LPAREN() != null) {
            System.out.print("(");
            T ret = visitChildren(ctx);
            System.out.print(")");
            return ret;
        } else if (ctx.literal() != null || ctx.identifier() != null) {
            System.out.print(ctx.getText());
        } else {
            visitChildren(ctx);
        }
        return null;
    }

    @Override
    public T visitExpression(JavaGrammarParser.ExpressionContext ctx) {
        if (ctx.LBRACK() != null){
            visitChildren(ctx.expression(0));
            System.out.print("[");
            visitChildren(ctx.expression(1));
            System.out.print("]");
        } else if (ctx.bop != null && ctx.bop.getText().equals(".")) {
            visitExpression(ctx.expression(0));
            System.out.print(".");
            if (ctx.identifier() != null){
                System.out.print(visitIdentifier(ctx.identifier()));
            } else if (ctx.methodCall() != null){
                visitMethodCall(ctx.methodCall());
            }
        } else if (ctx.bop != null) {
            String bop = ctx.bop.getText();
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
                    visitExpression(ctx.expression(0));
                    System.out.print(" " + bop + " ");
                    visitExpression(ctx.expression(1));
                    break;
                case "||":
                    visitExpression(ctx.expression(0));
                    System.out.print(" " + "Or" + " ");
                    visitExpression(ctx.expression(1));
                    break;
                case "&&":
                    visitExpression(ctx.expression(0));
                    System.out.print(" " + "And" + " ");
                    visitExpression(ctx.expression(1));
                    break;
                case "?":
                    visitExpression(ctx.expression(1));
                    System.out.print(" if ");
                    visitExpression(ctx.expression(0));
                    System.out.print(" else ");
                    visitExpression(ctx.expression(2));
                    break;
            }
        } else if (ctx.postfix != null) {
            visitExpression(ctx.expression(0));
            if (ctx.postfix.getText().equals("++")){
                System.out.print( " += 1");
            } else {
                System.out.print(" -= 1");
            }
        } else if (ctx.prefix != null) {
            String pf = ctx.prefix.getText();
            switch (pf){
                case "+":
                case "-":
                    System.out.print(pf);
                    visitExpression(ctx.expression(0));
                    break;
                case "!":
                    System.out.print(" not ");
                    visitExpression(ctx.expression(0));
                    break;
                case "++":
                    visitExpression(ctx.expression(0));
                    System.out.print(" += 1");
                    break;
                case "--":
                    visitExpression(ctx.expression(0));
                    System.out.print(" -= 1");
                    break;
            }
        } else {
            return visitChildren(ctx);
        }
        return null;
    }

    @Override
    public T visitMethodCall(JavaGrammarParser.MethodCallContext ctx) {
        if (ctx.identifier() != null){
            String mid = ctx.identifier().getText();
            switch (mid){
                case "println":
                    System.out.print("print");
                    break;
                default:
                    System.out.println(mid);
            }
            visitArguments(ctx.arguments());
        }
        return null;
    }

    @Override
    public T visitArguments(JavaGrammarParser.ArgumentsContext ctx) {
        System.out.print("(");
        visitChildren(ctx);
        System.out.print(")");
        return null;
    }

    @Override
    public T visitExpressionList(JavaGrammarParser.ExpressionListContext ctx) {
        int i = 1;
        while (ctx.expression(i) != null){
            System.out.print(",");
            ++i;
        }
        return super.visitExpressionList(ctx);
    }

    @Override
    public T visitElementValuePair(JavaGrammarParser.ElementValuePairContext ctx) {
        System.out.print(visitIdentifier(ctx.identifier()));
        System.out.print(" = ");
        visitElementValue(ctx.elementValue());
        return null;
    }

    @Override
    public T visitVariableDeclarators(JavaGrammarParser.VariableDeclaratorsContext ctx) {
        visitVariableDeclarator(ctx.variableDeclarator(0));
        int i = 1;
        while (ctx.variableDeclarator(i) != null){
            System.out.print(", ");
            visitVariableDeclarator(ctx.variableDeclarator(i));
            ++i;
        }
        return null;
    }

    @Override
    public T visitVariableDeclarator(JavaGrammarParser.VariableDeclaratorContext ctx) {
        String id = ctx.variableDeclaratorId().getText();
        System.out.print(id);
        if (ctx.ASSIGN() != null){
            System.out.print(" = ");
            visitVariableInitializer(ctx.variableInitializer());
        }
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

        System.out.print("def " + visitIdentifier(ctx.identifier()));
        visitFormalParameters(ctx.formalParameters());
        int i = 0;
        while (ctx.LBRACK(i) != null){
            System.out.print("[]");
            ++i;
        }
        return visitMethodBody(ctx.methodBody());
    }

    @Override
    public T visitFormalParameters(JavaGrammarParser.FormalParametersContext ctx) {
        System.out.print("(self");
        visitChildren(ctx);
        System.out.print(")");
        return null;
    }
}
