package net.filippocosta.microblog;

import java.time.Instant;
import java.util.List;
import java.util.Queue;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Questa classe descrive e implementa le funzionalità dei post, che sono la
// principale forma di contenuti su MicroBlog.
class Post implements CheckRep {
    // AF(p):
    //   <id, author, text, timestamp, likes, privacy>
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
    //   && (forall u. p.likes ==> u != null && User.usernameIsOk(u))
    //
    //   && p.replies != null
    //   && (forall p1. p.replies ==> p1 != null
    //                             && RI(p1)
    //                             && p1.timestamp.after(p.timestamp)
    //                             && p1.parent == p)
    //
    //   && p.replyRestriction != null
    //   && ((p.replyRestriction == ONLY_AUTHOR)
    //       ==>
    //       (forall p1. p.replies ==> p1.author == p.author))
    //   && ((p.replyRestriction == ONLY_AUTHOR_OR_TAGGED_USERS)
    //       ==>
    //       (forall p1. p.replies ==> (p1.author == p.author) ||
    //        p.getTaggedUsers().contains(p1.author)))
    //
    //   && p.hashtags != null
    //   && (forall h. p.hashtags ==> h != null)
    //   && (forall h. p.hashtags ==> p.text.contains(String.format("#%s", h)))
    //
    //   && p.taggedUsers != null
    //   && (forall u. p.taggedUsers ==> u != null)
    //   && (forall u. p.taggedUsers ==> p.text.contains(String.format("@%s", u)))

    // Questo contatore permette di generate ID autoincrementate senza rischi di
    // collisione.
    private static int ID_COUNTER = 0;

    // Lunghezza massima dei post su MicroBlog.
    public static int MAX_LENGTH = 140;

    // Attributi imposti dalla specifica:
    private final int id;
    private final String author;
    private final String text;
    private final Instant timestamp;
    private List<String> likes;
    // Attributi relativi alle funzionalità aggiuntive descritte nella relazione:
    private final Post parent;
    private List<Post> replies;
    private final ReplyRestriction replyRestriction;
    private final List<String> hashtags;
    private final List<String> taggedUsers;

    // Classe utile alla creazione di istanze `Post` tramite configurazione con
    // i metodi `setReplyRestriction` e `inResponseTo`.
    public static class Builder {
        // AF(c):
        //   I dati necessari a istanziare un oggetto della classe `Post`.
        // RI(c):
        //   c.author != null && c.text != null

        private final String author;
        private final String text;
        private Post parent;
        private ReplyRestriction replyRestriction = ReplyRestriction.EVERYONE;

        public Builder(String author, String text) {
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
        //   Restituisce `this` configurato a seconda di `restrict`.
        public Builder setReplyRestriction(ReplyRestriction restrict) throws NullPointerException {
            if (restrict == null) {
                throw new NullPointerException();
            }
            this.replyRestriction = restrict;
            return this;
        }

        // Stabilisce una relazione gerarchica -post e risposta al post-
        // relativa al parametro `post`.
        //
        // REQUIRES:
        //   `post != null && post.userCanReply(author)`.
        // THROWS:
        //   `NullPointerException` se e solo se `post == null`.
        //   `IllegalArgumentException` se e solo se `!post.userCanReply(author)`.
        // MODIFIES:
        //   `this`.
        // EFFECTS:
        //   Restituisce `this` configurato per istanziare una risposta a `post`.
        public Builder inResponseTo(Post post) throws NullPointerException, IllegalArgumentException {
            if (post == null) {
                throw new NullPointerException();
            } else if (!post.userCanReply(this.author)) {
                throw new IllegalArgumentException();
            }
            this.parent = post;
            return this;
        }

        public Post build() {
            return new Post(this);
        }
    }

    // L'autore di un post può limitare la possibilità degli altri utenti di
    // rispondere al post stesso. Le configurazioni possibili sono tre:
    //
    // - Solo l'autore può rispondere al proprio post.
    // - Solo l'autore del post o in alternativa gli utenti taggati nel post
    //   possono rispondere.
    // - Tutti possono rispondere al post.
    public enum ReplyRestriction {
        ONLY_AUTHOR,
        ONLY_AUTHOR_OR_TAGGED_USERS,
        EVERYONE,
    }

    // Costruttore per la classe `Post`.
    private Post(Builder builder) throws NullPointerException, IllegalArgumentException {
        if (builder.author == null || builder.text == null) {
            throw new NullPointerException();
        }
        if (builder.text.length() > Post.MAX_LENGTH) {
            throw new IllegalArgumentException();
        }
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
    //   Restituisce la data e l'ora di invio del post sotto forma di `Instant`.
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

    // Controlla se l'utente `username` ha messo like al post.
    //
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
    //   Restituisce `true` se e solo se `username` ha -al momento di uscita dal
    //   metodo- messo like al post, `false` altrimenti.
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