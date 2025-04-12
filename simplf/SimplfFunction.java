package simplf;

import java.util.List;

class SimplfFunction implements SimplfCallable {
    private final Stmt.Function declaration;
    private final Environment closure;

    SimplfFunction(Stmt.Function declaration, Environment closure) {
        this.declaration = declaration;
        this.closure = closure;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        Environment functionEnv = new Environment(closure);
        for (int i = 0; i < declaration.params.size(); i++) {
            Token param = declaration.params.get(i);
            functionEnv.define(param, param.lexeme, args.get(i));
        }

        try {
            interpreter.executeBlock(declaration.body, functionEnv);
        } catch (Return r) {
            return r.value;
        }

        return null;
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }
}

