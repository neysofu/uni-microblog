package net.filippocosta.microblog;

import java.util.List;
import java.util.ArrayList;

// Questa classe contiene alcuni metodi statici che assistono nella convalida
// dei nomi utente all'interno di MicroBlog. Si fornisce inoltre un metodo
// `suggestOtherUsernames` che propone nomi utente alternativi in caso quello
// scelto sia già in uso da terzi.
// 
// Si sottolinea che -nonostante le funzionalità qui implementate non siano
// imposte dal documento di specifica- il meccanismo di convalida e di
// suggerimento nascono per esigenze di emulazione nei confronti di altri
// social network quali Facebook, Reddit, e Twitter. In particolare, la
// convalida dei nomi utente pre-iscrizione evita queste situazioni sgradevoli:
// 
// - Il nome utente è troppo corto (< 3) e risulta poco visibile sullo schermo.
// - Il nome utente è troppo lungo (> 12) e risulta scomodo da scrivere o
//   perfino troppo lungo da rientrare nel limite di caratteri di un post.
// - Il nome utente contiene caratteri accentati, stranieri, spazi o
//   punteggiatura. Gli unici caratteri ammessi in un nome utente sono:
//   - Lettere.
//   - Numeri.
//   - Trattino basso (l'undescore, ASCII 95).
// 
// Questa classe non contiene alcuno stato interno e nessun costruttore. Ciò
// rende superfluo la descrizione di AF e RI.
final public class User {
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

    // Genera un massimo di tre nomi utente simili a quello fornito, evitando
    // duplicati in `others`.
    // 
    // REQUIRES:
    //   `username != null` e `others != null`.
    // THROWS:
    //   `NullPointerException` in caso `username == null` o `others == null`.
    // MODIFIES:
    //   Nessuna modifica.
    // EFFECTS:
    //   Restituisce una lista di nomi utente generati automaticamente. Questi
    //   nomi utente sono il più possibile simili a `username`.
    //   Questo metodo restituisce una lista vuota nel remoto caso in cui nessuno
    //   dei nomi utente generati sia disponibile.
    static public List<String> suggestOtherUsernames(String username, List<String> others) {
        List<String> suggestions = new ArrayList<>();
        if (!others.contains(username)) {
            suggestions.add(username);
            return suggestions;
        }
        //int postfixMax = 10;
        //while (false) {
        //    for (int i = 0; i < 3; i++) {
        //        int randomPostfix = 3;
        //        String suggestion = String.join(username, Integer.toString(randomPostfix));
        //        suggestions.add(username);
        //    }
        //}
        return suggestions;
    }
}
