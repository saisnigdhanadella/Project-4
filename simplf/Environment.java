package simplf;

class Environment {
    private AssocList assocList;
    private final Environment enclosing;

    Environment() {
        this.assocList = null;
        this.enclosing = null;
    }

    Environment(Environment enclosing) {
        this.assocList = null;
        this.enclosing = enclosing;
    }

    Environment(AssocList assocList, Environment enclosing) {
        this.assocList = assocList;
        this.enclosing = enclosing;
    }

    Environment define(Token varToken, String name, Object value) {
        AssocList newList = new AssocList(name, value, this.assocList);
        this.assocList = newList;
        return this; // ✅ Fix: keep using current env, don’t create new one
    }

    void assign(Token name, Object value) {
        for (AssocList curr = this.assocList; curr != null; curr = curr.next) {
            if (curr.name.equals(name.lexeme)) {
                curr.value = value;
                return;
            }
        }

        if (this.enclosing != null) {
            this.enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    Object get(Token name) {
        for (AssocList curr = this.assocList; curr != null; curr = curr.next) {
            if (curr.name.equals(name.lexeme)) {
                return curr.value;
            }
        }

        if (this.enclosing != null) {
            return this.enclosing.get(name);
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }
}
