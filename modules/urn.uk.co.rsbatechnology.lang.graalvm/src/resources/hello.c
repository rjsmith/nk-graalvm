#include <stdio.h>
#include <polyglot.h>

int main() {

    // import NK context object from host Java
    void *context = polyglot_import("context");

    void * str = polyglot_invoke(context, "source", polyglot_from_string("res:/dummy", "utf8"));

    polyglot_invoke(context, "createResponseFrom", str);

    return 0;
}