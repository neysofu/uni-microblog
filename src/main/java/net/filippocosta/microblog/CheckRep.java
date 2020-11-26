package net.filippocosta.microblog;

interface CheckRep {

    // Verifica l'invariante di rappresentazione (RI) per l'instanza `this`.
    //
    // Nota bene: questo metodo è pensato unicamente per favorire il debugging e
    // la realizzazione della batteria di test.
    //
    // EFFECTS:
    //   Restituisce `true` se e solo se l'instanza `this` verifica la propria
    //   invariante di rappresentazione, `false` altrimenti.
    boolean checkRep();
}
