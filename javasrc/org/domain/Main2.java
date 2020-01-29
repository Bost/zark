package org.domain;

import clojure.lang.IFn;
import clojure.java.api.Clojure;

// TODO create my.bla
// (ns my.bla)
// (def foo-str "my.bla/foo-str")
// (defn foo-fn [] "(my.bla/foo-fn)")
// (defn -main [& args]
//        (str "(my.bla/-main " args ") ; (count args): " (count args)))

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
        String mainFn = "undef mainFn";
        try {
            IFn minusMain = Clojure.var(ns, "-main");
            switch (SpringbootApplication.xArgs.length) {
            case 0:
                mainFn = minusMain.invoke().toString();
                break;
            case 1:
                mainFn = minusMain.invoke(SpringbootApplication.xArgs[0]).toString();
                break;
            case 2:
                mainFn = minusMain.invoke(
                                          SpringbootApplication.xArgs[0],
                                          SpringbootApplication.xArgs[1]
                                          ).toString();
                break;
            case 3:
                mainFn = minusMain.invoke(
                                          SpringbootApplication.xArgs[0],
                                          SpringbootApplication.xArgs[1],
                                          SpringbootApplication.xArgs[2]
                                          ).toString();
                break;
            case 4:
                mainFn = minusMain.invoke(
                                          SpringbootApplication.xArgs[0],
                                          SpringbootApplication.xArgs[1],
                                          SpringbootApplication.xArgs[2],
                                          SpringbootApplication.xArgs[3]
                                          ).toString();
                break;
            case 5:
                mainFn = minusMain.invoke(
                                          SpringbootApplication.xArgs[0],
                                          SpringbootApplication.xArgs[1],
                                          SpringbootApplication.xArgs[2],
                                          SpringbootApplication.xArgs[3],
                                          SpringbootApplication.xArgs[4]
                                          ).toString();
                break;
            case 6:
                mainFn = minusMain.invoke(
                                          SpringbootApplication.xArgs[0],
                                          SpringbootApplication.xArgs[1],
                                          SpringbootApplication.xArgs[2],
                                          SpringbootApplication.xArgs[3],
                                          SpringbootApplication.xArgs[4],
                                          SpringbootApplication.xArgs[5]).toString();
                break;
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return "retStr: "+retStr+"; retFn: "+retFn+"; mainFn: "+mainFn;
    }
}
