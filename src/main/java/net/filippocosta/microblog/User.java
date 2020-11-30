package net.filippocosta.microblog;

// OVERVIEW:
//   Questa classe contiene funzionalità elementari che assistono nella convalida
//   dei nomi utente all'interno di MicroBlog.
// 
//   Si sottolinea che -nonostante le funzionalità qui implementate non siano
//   imposte dal documento di specifica- il meccanismo di convalida nasce per
//   esigenze di emulazione nei confronti di altri social network quali Facebook,
//   Reddit, e Twitter e in particolare evita queste situazioni sgradevoli:
// 
//   - Il nome utente è troppo corto (< 3) e risulta poco visibile sullo schermo.
//   - Il nome utente è troppo lungo (> 12) e risulta scomodo da scrivere o
//     perfino troppo lungo da rientrare nel limite di caratteri di un post.
//   - Il nome utente contiene caratteri accentati, stranieri, spazi o
//     punteggiatura. Gli unici caratteri ammessi in un nome utente sono:
//     - Lettere.
//     - Numeri.
//     - Trattino basso (l'undescore, ASCII 95).
final public class User {
    // Questa classe non contiene alcuno stato interno e nessun costruttore. Ciò
    // rende superfluo la descrizione di AF e RI.

    public static int USERNAME_MIN_LENGTH = 3;
    public static int USERNAME_MAX_LENGTH = 12;

    // Controlla che `username` rispetti le regole sui nomi utente di MicroBlog
    // (i requisiti di lunghezza e di caratteri che sono descritti nella
    // documentazione di questa classe).
    // 
    // REQUIRES:
    //   `username != null`.
    // THROWS:
    //   `NullPointerException` in caso `username == null`.
    // MODIFIES:
    //   Nessuna modifica.
    // EFFECTS:
    //   Restituisce `true` se e solo se `username` rispetta i requisiti
    //   descritti, `false` altrimenti.
    static boolean usernameIsOk(String username) throws NullPointerException {
        if (username == null) {
            throw new NullPointerException();
        }
        if (username.length() < USERNAME_MIN_LENGTH || username.length() > USERNAME_MAX_LENGTH) {
            return false;
        }
        for (char c : username.toCharArray()) {
            if (!charIsAllowedInUsername(c)) {
                return false;
            }
        }
        return true;
    }

    // Controlla che il carattere `c` faccia parte dell'insieme di caratteri
    // permessi in un nome utente MicroBlog.
    // 
    // MODIFIES:
    //   Nessuna modifica.
    // EFFECTS:
    //   Restituisce `true` se e solo se `c` è permesso, `false` altrimenti.
    private static boolean charIsAllowedInUsername(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }
}