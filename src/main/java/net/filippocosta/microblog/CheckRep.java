package net.filippocosta.microblog;

interface CheckRep {

    // Verifica l'invariante di rappresentazione (IR) per l'instanza `this`.
    //
    // EFFECTS:
    //   Restituisce `true` se e solo se l'instanza `this` verifica la propria
    //   invariante di rappresentazione, `false` altrimenti.
    boolean checkRep();
}
