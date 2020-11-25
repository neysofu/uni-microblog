package net.filippocosta.microblog;

import java.time.Instant;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

// Questa classe descrive e implementa le funzionalità dei post, che sono la
// principale forma di contenuti su MicroBlog.
class Post implements CheckRep {
    // AF(p):
    //   <id, author, text, timestamp, likes, privacy>
    // RI(p):
    //   RI_WEAK(p) && (p.parent != null ==> RI(p.parent))
    // RI_WEAK(p):
    //   p.id != null
    //
    //   && p.author != null
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
    //   && RI_WEAK(p1)
    //   && p1.timestamp.after(p.timestamp)
    //   && p1.parent == p)
    //
    //   && p.replyRestriction != null
    //   && ((p.replyRestriction == ReplyRestriction.ONLY_AUTHOR)
    //       ==>
    //       (forall p1. p.replies ==> p1.author == p.author))
    //   && ((p.replyRestriction == ReplyRestriction.ONLY_IF_TAGGED)
    //       ==>
    //       (forall p1. p.replies ==> (p1.author == p.author) ||
    //        p.getTaggedUsers().contains(p1.author)))
    //
    //   && p.hashtags != null
    //   && (forall h. p.hashtags ==> h != null)

    public static int MAX_LENGTH = 140;

    // Ogni post possiede un identificatore univoco (Universally Unique
    // IDentifier) di grandezza 128-bit. Questi idenficatori vengono generati
    // casualmente tramite `java.util.UUID.randomUUID()` senza rischio effettivo
    // di collisione, considerata l'enorme entropia offerta da 128 bit di
    // informazione.
    UUID id;

    String author;

    // Il corpo di test di questo post.
    String text;

    /**
     * `Instant` is timezone-agnostic and has great precision, so it's a good choice
     * for our intended usage.
     * 
     * RI: after `author`'s account creation date. AF: the time that the post got
     * published.
     */
    Instant timestamp;

    /**
     * According to the specification `like` is supposed to be:
     * 
     * lista degli utenti della rete sociale che hanno messo un like al post
     * 
     * So, intuitively, a `List<String>` would be the immediate choice. However,
     * it's important to note that random-access operations on a `List` take linear
     * time and we don't actually need to mantain an ordering of elements inside
     * `likes`. By loosening the ordering requirement we open up the possibility of
     * performance improvements: in fact, most `Set` implementations take constant
     * amortized random-access time.
     */
    Set<String> likes;

    Post parent;
    List<Post> replies;

    public enum ReplyRestriction {
        ONLY_AUTHOR, ONLY_AUTHOR_OR_TAGGED_USERS, EVERYONE,
    }

    ReplyRestriction replyRestriction;
    List<String> hashtags;

    // Costruttore per la classe `Post`.
    public Post(String author, String text, Post replyingTo, ReplyRestriction restrictions) {
        this.id = UUID.randomUUID();
        this.author = author;
        this.text = text;
        this.timestamp = Instant.now();
        this.likes = new HashSet<>();
        if (text.length() > Post.MAX_LENGTH) {
            throw new IllegalArgumentException();
        }
        this.parent = replyingTo;
        if (this.parent != null) {
            this.parent.replies.add(this);
        }
        this.replies = new ArrayList<Post>();
        this.replyRestriction = restrictions;
        this.hashtags = new ArrayList<String>();
    }

    // EFFECTS:
    //   Restituisce il nome utente dell'autore di `this`.
    public String getAuthor() {
        return this.author;
    }

    // EFFECTS:
    //   Restituisce il corpo di testo di `this`.
    public String getText() {
        return this.text;
    }

    /**
     * @return a `List` of users that likes `this` post.
     */
    public List<String> getLikes() {
        List<String> likes = new ArrayList<String>();
        for (String like : this.likes) {
            likes.add(like);
        }
        return likes;
    }

    // Esamina il contenuto del post `this` e restituisce la lista di utenti
    // taggati nel testo.
    //
    // È possibile taggare un utente scrivendo il nome utente dello stesso,
    // preceduto da una chiocciola. Per esempio:
    //
    //   > Oggi sono uscito a mangiare un gelato con @filippo_costa!
    //   > Grazie a @danielerossi e @gianni99 per una serata fantastica :)
    //
    // MODIFIES:
    //   Nessuna modifica.
    // EFFECTS:
    //   Restituisce la lista di utenti taggati in `this`.
    public List<String> getTaggedUsers() {
        List<String> taggedUsers = new ArrayList<>();
        int tagStart = -1;
        for (int i = 0; i < this.text.length(); i++) {
            char c = this.text.charAt(i);
            if (tagStart < 0 && c == '@') {
                tagStart = i;
            } else if (tagStart >= 0) {
                tagStart = i;
                taggedUsers.add(this.text.substring(tagStart, i + 1));
            }
        }
        if (tagStart >= 0) {
            taggedUsers.add(this.text.substring(tagStart, this.text.length()));
        }
        return taggedUsers;
    }

    // Controlla se l'utente `username` ha messo like al post `this`.
    //
    // MODIFIES:
    //   Nessuna modifica.
    // EFFECTS:
    //   Restituisce `true` se e solo se `username` ha messo like a `this` o meno.
    public boolean isLikedBy(String username) {
        return this.likes.contains(username);
    }

    // Calcola se `this` è un post controverso. Un post è considerato
    // controverso se il numero di risposte al post è strettamente maggiore del
    // numero di like al post stesso.
    //
    // MODIFIES:
    //   Nessuna modifica.
    // EFFECTS:
    //   Restituisce un valore booleano che determina se `this` è un post
    //   controverso o meno.
    public boolean isControversial() {
        return this.totalReplies() > this.likes.size();
    }

    // Calcola il numero totale di risposte e sotto-risposte (ad infinitum) a
    // `this`.
    //
    // MODIFIES:
    //   Nessuna modifica.
    // EFFECTS:
    //   Restituisce il numero di post che sono risposte -sia dirette che
    //   indirette- a `this`.
    // EXAMPLE:
    //   Nel caso della conversazione riportata qui sotto, si avrebbe
    //   `this.totalReplies() == 4`.
    //
    //   > Ciao a tutti! Come state oggi?
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
                for (Post reply : top.replies) {
                    queue.add(reply);
                }
                size++;
            }
        }
        return size;
    }

    // Aggiunge un like a `this` se non già presente da parte di `username`,
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
    //   Restituisce un valore booleano che rappresenta la presenza o meno del
    //   like da parte di `username` al termine dell'esecuzione di questo metodo.
    public boolean toggleLike(String username) throws NullPointerException, IllegalArgumentException {
        if (username == null) {
            throw new NullPointerException();
        } else if (username == this.author) {
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

    /**
     * Update the body of this post with some new text.
     * 
     * @param body the updated post's body.
     * @modifies this
     */
    public void edit(String body) {
        this.text = body;
    }

    // Verifica l'invariante di rappresentazione (IR) per l'instanza `this`.
    //
    // MODIFIES:
    // Nessuna modifica.
    // EFFECTS:
    // Restituisce `true` se e solo se l'instanza `this` verifica l'invariante
    // di rappresentazione della class `Post`, `false` altrimenti.
    public boolean checkRep() {
        boolean ri = this.author != null && User.usernameIsOk(this.author) && this.text != null
                && this.text.length() <= Post.MAX_LENGTH && this.id != null && this.timestamp != null
                && this.likes != null && !this.likes.contains(this.author) && this.replies != null;
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
}