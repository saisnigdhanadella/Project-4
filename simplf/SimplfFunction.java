package simplf;

import java.util.List;

class SimplfFunction implements SimplfCallable {
    private final Stmt.Function declaration;
    private Environment closure;

    SimplfFunction(Stmt.Function declaration, Environment closure) {
        this.declaration = declaration;
        this.closure = closure;
    }

    public void setClosure(Environment environment) {
        this.closure = environment;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        Environment env = new Environment(closure);
        for (int i = 0; i < declaration.params.size(); i++) {
            env = env.define(declaration.params.get(i), declaration.params.get(i).lexeme, args.get(i));
        }

        Object result = null;
        for (Stmt stmt : declaration.body) {
            result = interpreter.execute(stmt);
        }

        return result;
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }
}
