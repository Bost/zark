package org.domain;

import clojure.lang.IFn;
import clojure.java.api.Clojure;

// TODO create my.bla
// (ns my.bla)
// (def foo-str "my.bla/foo-str")
// (defn foo-fn [] "(my.bla/foo-fn)")
// (defn -main [& args] "(my.bla/-main)")

public class Main2 {
    public static void main(String[] args) {
        String coreNs = "clojure.core";
        IFn deref   = Clojure.var(coreNs, "deref");
        IFn require = Clojure.var(coreNs, "require");
        String ns = "my.bla";
        require.invoke(Clojure.read(ns));

        String retStr = "undef retStr";
        try {
            IFn fooStr = Clojure.var(ns, "foo-str");
            retStr = deref.invoke(fooStr).toString();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        String retFn = "undef retFn";
        try {
            IFn fooFn = Clojure.var(ns, "foo-fn");
            retFn = fooFn.invoke().toString();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return "retStr: " + retStr + "; retFn: "+retFn;
    }
}
