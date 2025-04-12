package simplf;

import java.util.ArrayList;
import java.util.List;

import simplf.Expr.Assign;
import simplf.Expr.Binary;
import simplf.Expr.Call;
import simplf.Expr.Conditional;
import simplf.Expr.Grouping;
import simplf.Expr.Literal;
import simplf.Expr.Logical;
import simplf.Expr.Unary;
import simplf.Expr.Variable;
import simplf.Stmt.Block;
import simplf.Stmt.Expression;
import simplf.Stmt.For;
import simplf.Stmt.Function;
import simplf.Stmt.If;
import simplf.Stmt.Print;
import simplf.Stmt.Var;
import simplf.Stmt.While;

public class Desugar implements Expr.Visitor<Expr>, Stmt.Visitor<Stmt> {

    public Desugar() {}

    public List<Stmt> desugar(List<Stmt> stmts) {
        ArrayList<Stmt> ret = new ArrayList<>();
        for (Stmt stmt : stmts) {
            ret.add(stmt.accept(this));
        }
        return ret;
    }

    @Override
    public Stmt visitPrintStmt(Print stmt) {
        return stmt;
    }

    @Override
    public Stmt visitExprStmt(Expression stmt) {
        return new Expression(stmt.expr.accept(this));
    }

    @Override
    public Stmt visitVarStmt(Var stmt) {
        return new Var(stmt.name, stmt.initializer.accept(this));
    }

    @Override
    public Stmt visitBlockStmt(Block stmt) {
        ArrayList<Stmt> newStmts = new ArrayList<>();
        for (Stmt s : stmt.statements) {
            newStmts.add(s.accept(this));
        }
        return new Block(newStmts);
    }

    @Override
    public Stmt visitIfStmt(If stmt) {
        Stmt newElse = (stmt.elseBranch == null) ? null : stmt.elseBranch.accept(this);
        return new If(stmt.cond.accept(this), stmt.thenBranch.accept(this), newElse);
    }

    @Override
    public Stmt visitWhileStmt(While stmt) {
        return new While(stmt.cond.accept(this), stmt.body.accept(this));
    }

    @Override
    public Stmt visitForStmt(For stmt) {
        Stmt init = stmt.init == null ? null : new Expression(stmt.init.accept(this));
        Expr cond = stmt.cond == null ? new Literal(true) : stmt.cond.accept(this);
        Stmt incr = stmt.incr == null ? null : new Expression(stmt.incr.accept(this));
        Stmt body = stmt.body.accept(this);

        if (incr != null) {
            List<Stmt> newBody = new ArrayList<>();
            newBody.add(body);
            newBody.add(incr);
            body = new Block(newBody);
        }

        Stmt whileLoop = new While(cond, body);

        if (init != null) {
            List<Stmt> block = new ArrayList<>();
            block.add(init);
            block.add(whileLoop);
            return new Block(block);
        }

        return whileLoop;
    }

    @Override
    public Stmt visitFunctionStmt(Function stmt) {
        List<Stmt> body = new ArrayList<>();
        for (Stmt s : stmt.body) {
            body.add(s.accept(this));
        }
        return new Function(stmt.name, stmt.params, body);
    }

    @Override
    public Expr visitBinary(Binary expr) {
        return new Binary(expr.left.accept(this), expr.op, expr.right.accept(this));
    }

    @Override
    public Expr visitUnary(Unary expr) {
        return new Unary(expr.op, expr.right.accept(this));
    }

    @Override
    public Expr visitLiteral(Literal expr) {
        return expr;
    }

    @Override
    public Expr visitGrouping(Grouping expr) {
        return new Grouping(expr.expression.accept(this));
    }

    @Override
    public Expr visitVarExpr(Variable expr) {
        return expr;
    }

    @Override
    public Expr visitAssignExpr(Assign expr) {
        return new Assign(expr.name, expr.value.accept(this));
    }

    @Override
    public Expr visitLogicalExpr(Logical expr) {
        return new Logical(expr.left.accept(this), expr.op, expr.right.accept(this));
    }

    @Override
    public Expr visitConditionalExpr(Conditional expr) {
        return new Conditional(expr.cond.accept(this),
                expr.thenBranch.accept(this),
                expr.elseBranch.accept(this));
    }

    @Override
    public Expr visitCallExpr(Call expr) {
        List<Expr> args = new ArrayList<>();
        for (Expr arg : expr.args) {
            args.add(arg.accept(this));
        }
        return new Call(expr.callee.accept(this), expr.paren, args);
    }
}
