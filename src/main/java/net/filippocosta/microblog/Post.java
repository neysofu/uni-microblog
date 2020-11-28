package net.filippocosta.microblog;

import java.time.Instant;
import java.util.List;
import java.util.Queue;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// OVERVIEW:
//   Questa classe descrive e implementa le funzionalità dei post, che sono la
//   principale forma di contenuti su MicroBlog. Il tipo di dato astratto
//   associato è composto dalle seguenti informazioni:
//     <id, author, text, timestamp, likes, parent, replies, replyRestriction>
//   Queste informazioni rappresentano rispettivamente:
//     1. L'identificativo numerico unico associato al post.
//     2. Il nome utente dell'autore del post.
//     3. Il corpo di testo -limitato a 140 caratteri- del post.
//     4. Data e ora di pubblicazione del post.
//     5. L'insieme di nomi utenti che hanno messo "mi piace" (like) al post:
//        `{ likes_0, likes_1, ... likes_n }`. Si ha inoltre che
//        `forall l1, l2 ∈ likes ==> l1 != l2`.
//     6. Il post a cui questo post risponde (opzionale).
//     7. L'insieme di risposte a questo post:
//        `{ replies_0, replies_1, ... replies_m }`. Si ha inoltre che
//        `forall r1, r2 ∈ replies ==> r1.getId() != r2.getId()`.
//     8. Un'impostazione che determina chi può rispondere a questo post su
//        MicroBlog tale per cui `replyRestriction ∈ S`, dove S è il tipo di dato
//        astratto della classe `ReplyRestriction`.
class Post implements CheckRep {
    // AF(p):
    //   <p.id,
    //    p.author,
    //    p.text,
    //    p.timestamp,
    //    {p.likes.get(i) | 0 <= i <= n},
    //    p.parent,
    //    {p.replies.get(i) | 0 <= i <= m},
    //    p.replyRestriction>
    // RI(p):
    //   p.author != null
    //   && User.usernameIsOk(p.author)
    //
    //   && p.text != null
    //   && p.text.length() <= Post.MAX_LENGTH
    //
    //   && p.timestamp != null
    //
    //   && p.likes != null
    //   && !p.likes.contains(p.author)
    //   && (forall i | 0 <= i < p.likes.size() ==> p.likes.get(i) != null && User.usernameIsOk(u))
    //
    //   && p.replies != null
    //   && (forall i | 0 <= i < p.replies.size() ==> p.replies.get(i) != null
    //                                             && RI(p.replies.get(i))
    //                                             && p.replies.get(i).timestamp.after(p.timestamp)
    //                                             && p.replies.get(i).parent == p)
    //
    //   && p.replyRestriction != null
    //   && ((p.replyRestriction == ONLY_AUTHOR)
    //       ==>
    //       (forall i | 0 <= i < p.replies.size() ==> p.replies.get(i) == p.author))
    //   && ((p.replyRestriction == ONLY_AUTHOR_OR_TAGGED_USERS)
    //       ==>
    //       (forall i | 0 <= i < p.replies.size() ==> p.replies.get(i) == p.author)
    //        || p.getTaggedUsers().contains(p.replies.get(i).author)))
    //
    //   && p.hashtags != null
    //   && (forall i | 0 <= i < p.hashtags.size()
    //       ==> p.hashtags.get(i) != null
    //        && p.text.contains(String.format("#%s", p.hashtags.get(i)))
    //
    //   && p.taggedUsers != null
    //   && (forall i | 0 <= i < p.taggedUsers.size()
    //       ==> p.taggedUsers.get(i) != null
    //        && p.text.contains(String.format("@%s", p.taggedUsers.get(i)))

    // Questo contatore permette di generare ID autoincrementate senza rischi di
    // collisione.
    private static int ID_COUNTER = 0;

    // Lunghezza massima dei post su MicroBlog.
    public static int MAX_LENGTH = 140;

    // Attributi imposti dalla specifica del progetto:
    private int id;
    private final String author;
    private final String text;
    private final Instant timestamp;
    private List<String> likes;
    // Attributi relativi alle funzionalità aggiuntive (descritte nella relazione):
    private final Post parent;
    private List<Post> replies;
    private final ReplyRestriction replyRestriction;
    private final List<String> hashtags;
    private final List<String> taggedUsers;

    // OVERVIEW:
    //   Un `Builder` è un oggetto modificabile utile alla creazione di istanze
    //   `Post` tramite configurazione. Il tipo di dato astratto associato è
    //   composto dalle quattro informazioni fondamentali alla pubblicazione di un
    //   post:
    //     <author, text, parent, replyRestriction>
    //   Queste quattro informazioni rappresentano rispettivamente:
    //     1. Il nome utente dell'autore del post da pubblicare.
    //     2. Il corpo di testo del post da pubblicare.
    //     3. Il post a cui si intende rispondere pubblicando questo post
    //        (opzionale).
    //     4. Impostazioni relative alle risposte del post che si intende
    //        pubblicare.
    public static class Builder {
        // AF(c):
        //   La rappresentazione concreta `c` rappresenta la schematica di
        //   pubblicazione di un post:
        //     1. Da parte di `c.author`.
        //     2. Che abbia come corpo di testo `c.text`.
        //     3. Che risponda a `c.parent` se e solo se `c.parent != null`,
        //        altrimenti che non risponda a nessuno.
        //     4. Che sia configurato come previsto da `c.replyRestriction`.
        // RI(c):
        //   c.author != null
        //   && c.text != null
        //   && c.replyRestriction != null

        private final String author;
        private final String text;
        private Post parent;
        private ReplyRestriction replyRestriction = ReplyRestriction.EVERYONE;

        // REQUIRES:
        //   `author != null && text != null && text.length() <= 140`.
        // THROWS:
        //   `NullPointerException` se e solo se `author == null || text == null`.
        //   `IllegalArgumentException` se e solo se il corpo di testo del post
        //   supera la lunghezza massima.
        // EFFECTS:
        //   Restituisce un costruttore
        //     <author, text, parent, replyRestriction>
        //   tale per cui:
        //     - `author` è inalterato.
        //     - `text` è inalterato.
        //     - `parent` è inizialmente assente, settabile con il metodo
        //       `inResponseTo`.
        //     - `replyRestriction` ha inizialmente il valore di default `EVERYONE`.
        public Builder(String author, String text) throws NullPointerException, IllegalArgumentException {
            if (author == null || text == null) {
                throw new NullPointerException();
            } else if (text.length() > Post.MAX_LENGTH) {
                throw new IllegalArgumentException();
            }
            this.author = author;
            this.text = text;
        }

        // Configura i limiti imposti alle risposte del post.
        //
        // REQUIRES:
        //   `restrict != null`.
        // THROWS:
        //   `NullPointerException` se e solo se `restrict == null`.
        // MODIFIES:
        //   `this`.
        // EFFECTS:
        //   Modifica il costruttore preselezionando il settaggio di limite alle
        //   risposte del post secondo `restrict`. Dopo aver modificato il
        //   costruttore, lo restituisce. Formalmente si ha che
        //     this_pre := <author, text, parent, replyRestriction_old>
        //   e dopo
        //     this_post := <author, text, parent, replyRestriction_new>
        //   tale per cui:
        //   - `replyRestriction_new` è inalterato rispetto al parametro `restrict`.
        public Builder setReplyRestriction(ReplyRestriction restrict) throws NullPointerException {
            if (restrict == null) {
                throw new NullPointerException();
            }
            this.replyRestriction = restrict;
            return this;
        }

        // REQUIRES:
        //   `post != null && post.userCanReply(author)`.
        // THROWS:
        //   `NullPointerException` se e solo se `post == null`.
        //   `IllegalArgumentException` se e solo se `!post.userCanReply(author)`.
        // MODIFIES:
        //   `this`.
        // EFFECTS:
        //   Modifica il costruttore stabilendo una relazione gerarchica tra
        //   `post` e `this`, tale per cui `this` è una risposta a `post`.
        //   Dopo aver modificato il costruttore lo restituisce. Formalmente si ha
        //   che
        //     this_pre := <author, text, parent_old, replyRestriction>
        //   e dopo
        //     this_post := <author, text, parent_new, replyRestriction>
        //   tale per cui:
        //     - `parent_new` è inalterato rispetto al parametro `post`.
        public Builder inResponseTo(Post post) throws NullPointerException, IllegalArgumentException {
            if (post == null) {
                throw new NullPointerException();
            } else if (!post.userCanReply(this.author)) {
                throw new IllegalArgumentException();
            }
            this.parent = post;
            return this;
        }

        // EFFECTS:
        //   Restituisce una nuova istanza di `Post`. Se
        //     this_pre := <author, text, parent, replyRestriction>
        //   allora il valore restituito sarà
        //     <id, author, text, timestamp, likes, parent, replies, replyRestriction>
        //   tale per cui:
        //     - `id` è generata automaticamente in modo incrementale, perciò unica.
        //     - `author` è inalterato.
        //     - `text` è inalterato.
        //     - `timestamp` è calcolato in tempo reale utilizzando la libreria
        //       standard.
        //     - `likes` è inizialmente vuoto.
        //     - `parent` è inalterato.
        //     - `replies` è inizialmente vuoto.
        //     - `replyRestriction` è inalterato.
        public Post build() {
            return new Post(this);
        }
    }

    // OVERVIEW:
    //   L'autore di un post può limitare la possibilità degli altri utenti di
    //   rispondere al post stesso. Le configurazioni possibili sono tre:
    //
    //     - Solo l'autore può rispondere al proprio post.
    //     - Solo l'autore del post o in alternativa gli utenti taggati nel post
    //       possono rispondere.
    //     - Tutti possono rispondere al post.
    //
    //   Il tipo di dato astratto associato è l'insieme di cardinalità #3 che
    //   comprende queste tre opzioni.
    public enum ReplyRestriction {
        // AF(rr):
        //   Non necessaria perchè tipo di dato concreto e astratto coincidono.
        // RI(rr):
        //   true

        ONLY_AUTHOR,
        ONLY_AUTHOR_OR_TAGGED_USERS,
        EVERYONE,
    }

    // Costruttore per la classe `Post`.
    private Post(Builder builder) {
        this.id = ID_COUNTER;
        this.author = builder.author;
        this.text = builder.text;
        this.replyRestriction = builder.replyRestriction;
        this.timestamp = Instant.now();
        this.likes = new ArrayList<String>();
        this.replies = new ArrayList<Post>();
        this.hashtags = Post.parseHashtags(text);
        this.taggedUsers = Post.parseTaggedUsers(text);
        this.parent = builder.parent;
        if (this.parent != null) {
            this.parent.replies.add(this);
        }
        ID_COUNTER += 1;
    }

    // MODIFIES:
    //   Nessuna modifica.
    // EFFECTS:
    //   Restituisce l'ID del post.
    public int getId() {
        return this.id;
    }

    // MODIFIES:
    //   Nessuna modifica.
    // EFFECTS:
    //   Restituisce il nome utente dell'autore del post.
    public String getAuthor() {
        return this.author;
    }

    // MODIFIES:
    //   Nessuna modifica.
    // EFFECTS:
    //   Restituisce il corpo di testo del post.
    public String getText() {
        return this.text;
    }

    // MODIFIES:
    //   Nessuna modifica.
    // EFFECTS:
    //   Restituisce la data e l'ora di invio del post.
    public Instant getTimestamp() {
        return this.timestamp;
    }

    // MODIFIES:
    //   Nessuna modifica.
    // EFFECTS:
    //   Restituisce la lista di nomi utente che hanno messo like al post.
    public List<String> getLikes() {
        return this.likes;
    }


    // MODIFIES:
    //   Nessuna modifica.
    // EFFECTS:
    //   Restituisce il tipo di configurazione per le risposte del post.
    public ReplyRestriction getReplyRestriction() {
        return this.replyRestriction;
    }

    // MODIFIES:
    //   Nessuna modifica.
    // EFFECTS:
    //   Restituisce il post a cui risponde questo post (se esiste), `null`
    //   altrimenti.
    public Post getParent() {
        return this.parent;
    }

    // MODIFIES:
    //   Nessuna modifica.
    // EFFECTS:
    //   Restituisce una lista delle risposte dirette al post.
    public List<Post> getReplies() {
        return this.replies;
    }

    // MODIFIES:
    //   Nessuna modifica.
    // EFFECTS:
    //   Restituisce la lista di hashtag utilizzati nel post, in ordine di
    //   presenza.
    // EXAMPLES:
    //   Gli hashtag sono parole o composizioni di parole precedute dal carattere
    //   cancelletto. Per esempio:
    //
    //     > Tra poco ci sono gli esami! #paura #studio #programmazione2
    public List<String> getHashtags() {
        return this.hashtags;
    }

    // MODIFIES:
    //   Nessuna modifica.
    // EFFECTS:
    //   Restituisce la lista di nomi utente taggati nel post, in ordine di
    //   presenza.
    // EXAMPLES:
    //   È possibile taggare un utente scrivendo il nome utente dello stesso,
    //   preceduto dal carattere chiocciola. Per esempio:
    //
    //     > Oggi sono uscito a mangiare un gelato con @filippo_costa!
    //     > Grazie a @danielerossi e @gianni99 per una serata fantastica :)
    public List<String> getTaggedUsers() {
        return this.taggedUsers;
    }

    // REQUIRES:
    //   `user != null`.
    // THROWS:
    //   `NullPointerException` se e solo se `user == null`.
    // MODIFIES:
    //   Nessuna modifica.
    // EFFECTS:
    //   Restituisce `true` se e solo se `user` è autorizzato a postare una
    //   risposta a `this`, `false` altrimenti.
    public boolean userCanReply(String user) {
        switch(this.getReplyRestriction()) {
            case EVERYONE:
                return true;
            case ONLY_AUTHOR_OR_TAGGED_USERS:
                return this.author == user
                    || this.getTaggedUsers().contains(user);
            case ONLY_AUTHOR:
                return this.author == user;
            default:
                return false;
        }
    }

    // REQUIRES:
    //   `username != null`.
    // THROWS:
    //   `NullPointerException` se e solo se `username == null`.
    // MODIFIES:
    //   Nessuna modifica.
    // EFFECTS:
    //   Restituisce `true` se e solo se `username` ha messo like al post, `false`
    //   altrimenti.
    public boolean isLikedBy(String username) {
        return this.likes.contains(username);
    }

    // Determina se il post è controverso o meno. Un post è considerato
    // controverso se il numero di risposte al post è strettamente maggiore del
    // numero di like al post stesso.
    //
    // MODIFIES:
    //   Nessuna modifica.
    // EFFECTS:
    //   Restituisce `true` se e solo se `this` è un post controverso, `false`
    //   altrimenti.
    public boolean isControversial() {
        return this.totalReplies() > this.likes.size();
    }

    // Calcola il numero totale di risposte e sotto-risposte (ad infinitum) al
    // post.
    //
    // MODIFIES:
    //   Nessuna modifica.
    // EFFECTS:
    //   Restituisce il numero di post che sono risposte -sia dirette che
    //   indirette- a `this`.
    // EXAMPLES:
    //   Nel caso della conversazione riportata qui sotto, si avrebbe
    //   `this.totalReplies() == 4` (cinque post in totale, di cui uno è il post
    //   originale).
    //
    //   > Ciao a tutti! Come state oggi? #monday #helloeveryone
    //      > Ciao Maria! Così così... i lunerì mattina sono sempre terribili :(
    //      > Benissimoooo oggi ho la prima lezione del mio nuovo corso di ballo!
    //         > Ma dai! Il corso di danza latino-americana di cui mi parlavi al telefono?
    //            > Esatto :D
    public int totalReplies() {
        // Potrei usare una funziona ricorsiva ma utilizziamo una DFS (Depth
        // First Search) fer migliorare le prestazioni.
        Queue<Post> queue = new LinkedList<Post>();
        queue.add(this);
        int size = 0;
        while (!queue.isEmpty()) {
            Post top = queue.poll();
            if (top != null) {
                for (Post reply : top.getReplies()) {
                    queue.add(reply);
                }
                size++;
            }
        }
        // `-1` perchè deve escludere il post originale.
        return size - 1;
    }

    // Aggiunge un like al post se non già presente da parte di `username`,
    // altrimenti lo rimuove.
    //
    // REQUIRES:
    //   `username != null`.
    // THROWS:
    //   `NullPointerException` se e solo se `username == null`.
    //   `IllegalArgumentException` se e solo se `username == this.author`.
    // MODIFIES:
    //   `this`.
    // EFFECTS:
    //   Se
    //     (forany i. 0 <= i < this.getLikes().size(), String.equals(this.getLikes().get(i), username))
    //   allora rimuove il like di `username` e restituisce `false`. Altrimenti
    //   aggiunge un like da parte di `username` e restituisce `true`.
    public boolean toggleLike(String username) throws NullPointerException, IllegalArgumentException {
        if (username == null) {
            throw new NullPointerException();
        }
        if (username == this.author) {
            throw new IllegalArgumentException("You can't like your own post.");
        }
        if (this.likes.contains(username)) {
            this.likes.remove(username);
            return false;
        } else {
            this.likes.add(username);
            return true;
        }
    }

    // MODIFIES:
    //   Nessuna modifica.
    // EFFECTS:
    //   Restituisce una cosiddetta "deep copy" di `this`.
    public Post deepCopy() {
        Post.Builder builder = new Post.Builder(this.getAuthor(), this.getText())
            .setReplyRestriction(this.getReplyRestriction());
        if (this.getParent() != null) {
            builder = builder.inResponseTo(this.getParent());
        }
        Post copy = builder.build();
        copy.id = this.id;
        for (String like : this.getLikes()) {
            copy.toggleLike(like);
        }
        for (Post reply : this.getReplies()) {
            reply.deepCopy();
        }
        return copy;
    }

    // Verifica l'invariante di rappresentazione (RI) per l'instanza `this`.
    //
    // Nota bene: questo metodo è pensato unicamente per favorire il debugging e
    // la realizzazione della batteria di test.
    //
    // MODIFIES:
    //   Nessuna modifica.
    // EFFECTS:
    //   Restituisce `true` se e solo se il post verifica l'invariante di
    //   rappresentazione della classe `Post`, `false` altrimenti.
    public boolean checkRep() {
        boolean ri = this.author != null
                  && User.usernameIsOk(this.author)
                  && this.text != null
                  && this.text.length() <= Post.MAX_LENGTH
                  && this.timestamp != null
                  && this.likes != null
                  && !this.likes.contains(this.author)
                  && this.replies != null;
        for (String like : this.likes) {
            ri = ri && (like != null) && User.usernameIsOk(like);
        }
        for (Post reply : this.replies) {
            ri = ri && (reply != null) && reply.checkRep();
        }
        if (this.replyRestriction == ReplyRestriction.ONLY_AUTHOR) {
            ri = ri && this.replies.size() == 0;
        } else if (this.replyRestriction == ReplyRestriction.ONLY_AUTHOR_OR_TAGGED_USERS) {
            List<String> taggedUsers = new ArrayList<String>();
            for (Post reply : this.replies) {
                ri = ri && ((reply.author == this.author) || taggedUsers.contains(reply.author));
            }
        }
        return ri;
    }

    private static List<String> parseHashtags(String text) {
        List<String> hashtags = new ArrayList<>();
        Matcher regex = Pattern.compile("#[a-zA-Z0-9_]+").matcher(text);
        while (regex.find()) {
            // `.substring(1)` per rimuovere il carattere cancelletto.
            hashtags.add(regex.group().substring(1));
        }
        return hashtags;
    }

    private static List<String> parseTaggedUsers(String text) {
        List<String> taggedUsers = new ArrayList<>();
        Matcher regex = Pattern.compile("@[a-zA-Z0-9_]+").matcher(text);
        while (regex.find()) {
            // `.substring(1)` per rimuovere il carattere chiocciola.
            taggedUsers.add(regex.group().substring(1));
        }
        return taggedUsers;
    }
}