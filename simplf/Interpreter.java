package simplf;

import java.util.List;
import simplf.Stmt.For;

class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Object> {
    public Environment globals = new Environment();
    private Environment environment = globals;

    Interpreter() {}

    public void interpret(List<Stmt> stmts) {
        try {
            for (Stmt stmt : stmts) {
                execute(stmt);
            }
        } catch (RuntimeError error) {
            Simplf.runtimeError(error);
        }
    }

    private Object execute(Stmt stmt) {
        return stmt.accept(this);
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    Object executeBlock(List<Stmt> statements, Environment newEnv) {
        Environment previous = this.environment;
        try {
            this.environment = newEnv;
            for (Stmt stmt : statements) {
                execute(stmt);
            }
        } finally {
            this.environment = previous;
        }
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }
        this.environment = this.environment.define(stmt.name, stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Object visitBlockStmt(Stmt.Block stmt) {
        Environment newEnv = new Environment(this.environment);
        return executeBlock(stmt.statements, newEnv);
    }

    @Override
    public Object visitVarExpr(Expr.Variable expr) {
        return this.environment.get(expr.name);
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        this.environment.assign(expr.name, value);
        return value;
    }

    @Override
    public Object visitExprStmt(Stmt.Expression stmt) {
        Object val = evaluate(stmt.expr);
        if (environment != globals) {
            throw new Return(val);  // ✅ Return only in function context
        }
        return null;
    }

    @Override
    public Object visitPrintStmt(Stmt.Print stmt) {
        Object val = evaluate(stmt.expr);
        System.out.println(stringify(val));
        return null;
    }

    @Override
    public Object visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.cond))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Object visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.cond))) {
            execute(stmt.body);
        }
        return null;
    }

    @Override
    public Object visitForStmt(For stmt) {
        throw new UnsupportedOperationException("For loops are desugared, not interpreted directly.");
    }

    @Override
    public Object visitFunctionStmt(Stmt.Function stmt) {
        SimplfFunction function = new SimplfFunction(stmt, this.environment);
        this.environment.define(stmt.name, stmt.name.lexeme, function);
        return null;
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);

        if (!(callee instanceof SimplfCallable)) {
            throw new RuntimeError(expr.paren, "Can only call functions.");
        }

        List<Object> arguments = new java.util.ArrayList<>();
        for (Expr arg : expr.args) {
            arguments.add(evaluate(arg));
        }

        SimplfCallable function = (SimplfCallable) callee;
        return function.call(this, arguments);
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        throw new UnsupportedOperationException("TODO: implement logical expressions");
    }

    @Override
    public Object visitBinary(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.op.type) {
            case PLUS:
                if (left instanceof String || right instanceof String)
                    return stringify(left) + stringify(right);
                if (left instanceof Double && right instanceof Double)
                    return (double) left + (double) right;
                throw new RuntimeError(expr.op, "Operands must be numbers or strings.");
            case MINUS:
                checkNumbers(expr.op, left, right);
                return (double) left - (double) right;
            case STAR:
                checkNumbers(expr.op, left, right);
                return (double) left * (double) right;
            case SLASH:
                checkNumbers(expr.op, left, right);
                if ((double) right == 0)
                    throw new RuntimeError(expr.op, "Cannot divide by zero.");
                return (double) left / (double) right;
            case GREATER:
                checkNumbers(expr.op, left, right);
                return (double) left > (double) right;
            case GREATER_EQUAL:
                checkNumbers(expr.op, left, right);
                return (double) left >= (double) right;
            case LESS:
                checkNumbers(expr.op, left, right);
                return (double) left < (double) right;
            case LESS_EQUAL:
                checkNumbers(expr.op, left, right);
                return (double) left <= (double) right;
            case EQUAL_EQUAL:
                return isEqual(left, right);
            case BANG_EQUAL:
                return !isEqual(left, right);
            case COMMA:
                return right;
            default:
                return null;
        }
    }

    @Override
    public Object visitUnary(Expr.Unary expr) {
        throw new UnsupportedOperationException("TODO: implement unary expressions");
    }

    @Override
    public Object visitLiteral(Expr.Literal expr) {
        return expr.val;
    }

    @Override
    public Object visitGrouping(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitConditionalExpr(Expr.Conditional expr) {
        throw new UnsupportedOperationException("TODO: implement conditional expressions");
    }

    private void checkNumbers(Token op, Object a, Object b) {
        if (a instanceof Double && b instanceof Double) return;
        throw new RuntimeError(op, "Operands must be numbers.");
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean) object;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null) return b == null;
        return a.equals(b);
    }

    private String stringify(Object object) {
        if (object == null) return "nil";
        if (object instanceof Double) {
            String num = object.toString();
            if (num.endsWith(".0")) {
                num = num.substring(0, num.length() - 2);
            }
            return num;
        }
        return object.toString();
    }
}


class Return extends RuntimeException {
    final Object value;

    Return(Object value) {
        super(null, null, false, false);
        this.value = value;
    }
}
