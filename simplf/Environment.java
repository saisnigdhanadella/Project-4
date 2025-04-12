package simplf;

class Environment {
    private AssocList values;
    private final Environment enclosing;

    Environment() {
        this.values = null;
        this.enclosing = null;
    }

    Environment(Environment enclosing) {
        this.values = null;
        this.enclosing = enclosing;
    }

    Environment(AssocList assocList, Environment enclosing) {
        this.values = assocList;
        this.enclosing = enclosing;
    }

    Environment define(Token varToken, String name, Object value) {
        AssocList newValues = new AssocList(name, value, this.values);
        return new Environment(newValues, this.enclosing);
    }

    void assign(Token name, Object value) {
        for (AssocList curr = values; curr != null; curr = curr.next) {
            if (curr.name.equals(name.lexeme)) {
                curr.value = value;
                return;
            }
        }
        if (enclosing != null) {
            enclosing.assign(name, value);
        } else {
            throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
        }
    }

    Object get(Token name) {
        for (AssocList curr = values; curr != null; curr = curr.next) {
            if (curr.name.equals(name.lexeme)) {
                return curr.value;
            }
        }
        if (enclosing != null) {
            return enclosing.get(name);
        } else {
            throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
        }
    }
}
