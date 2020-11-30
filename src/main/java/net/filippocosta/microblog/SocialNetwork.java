package net.filippocosta.microblog;

import java.util.Set;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

// OVERVIEW:
//   Rappresenta il social network del servizio MicroBlog. Il tipo di dato
//   astratto è
//     <{user_0, user_1, ... user_n}, {post_0, post_1, ... post_m}>
//   con `n <= m`.
class SocialNetwork implements CheckRep {
    // AF(c):
    //   <c.followes.keySet(), c.postsById.valuSet()>
    // RI(c):
    //   c.followees != null
    //   && c.postsByUser != null
    //   && c.postsById != null
    //
    //   && (forall <k, v>. c.followees
    //       ==> User.usernameIsOk(k)
    //        && k != null
    //        && c.postsByUser.containsKey(k)
    //        && v != null
    //        && (forall s. v ==> s != null && c.followees.containsKey(s)))
    //   && (forall <k, v>. c.postsByUser
    //       ==> k != null
    //        && c.followees.containsKey(k)
    //        && v != null
    //        && (forall i. 0 <= i < v.size() ==> v.get(i) != null
    //                                         && c.postsById.containsKey(v.get(i).getId())
    //                                         && c.postsById.containsValue(v.get(i))
    //                                         && c.followees.containsKey(v.get(i).getAuthor())))
    //   && (forall <k, v>. c.postsById
    //       ==> k != null
    //        && v != null
    //        && (forany <k1, v1>. c.postsByUser, v1.contains(v)))

    private Map<String, Set<String>> followees;
    private Map<String, List<Post>> postsByUser;
    private Map<Integer, Post> postsById;

    // Costruttore per la classe `SocialNetwork`.
    //
    // EFFECTS:
    //   Restituisce una nuova istanza di `SocialNetwork` senza utenti né post.
    //   Formalmente:
    //     <{}, {}>
    public SocialNetwork() {
        this.followees = new HashMap<>();
        this.postsByUser = new HashMap<>();
        this.postsById = new HashMap<>();
    }

    // MODIFIES:
    //   Nessuna modifica.
    // EFFECTS:
    //   Restituisce una lista di tutti i post presenti su MicroBlog. L'ordine non
    //   è specificato. Formalmente si ha che
    //     this_pre := <{user_0, user_1, ... user_n}, {post_0, post_1, ... post_m}>
    //   e il valore restituito è
    //     {post_0, post_1, ... post_m}

    public List<Post> getPosts() {
        List<Post> posts = new ArrayList<>();
        for (Map.Entry<String, List<Post>> entry : this.postsByUser.entrySet()) {
            for (Post post : entry.getValue()) {
                posts.add(post.deepCopy());
            }
        }
        return posts;
    }

    // MODIFIES:
    //   Nessuna modifica.
    // EFFECTS:
    //   Restituisce una mappa che associa a tutti gli utenti di MicroBlog gli
    //   ordini che ciascuno ha scritto. L'ordine non
    //   è specificato. Formalmente si ha che
    //     this_pre := <{user_0, user_1, ... user_n}, {post_0, post_1, ... post_m}>
    //   e il valore restituito è
    //     {<user_0, posts_0>, <user_1, posts_1>, ... <user_n, posts_n>}
    public Map<Integer, Post> getPostsById() {
        Map<Integer, Post> posts = new HashMap<>();
        for (Map.Entry<Integer, Post> entry : this.postsById.entrySet()) {
            posts.put(entry.getKey(), entry.getValue().deepCopy());
        }
        return posts;
    }

    // MODIFIES:
    //   Nessuna modifica.
    // EFFECTS:
    //   Restituisce una lista di tutti gli utenti registrati a MicroBlog.
    //   L'ordine non è specificato. Formalmente si ha che
    //     this_pre := <{user_0, user_1, ... user_n}, {post_0, post_1, ... post_m}>
    //   e il valore restituito è
    //     {user_0, user_1, ... user_n}
    public List<String> getUsers() {
        return new ArrayList<String>(this.followees.keySet());
    }

    // MODIFIES:
    //   Nessuna modifica.
    // EFFECTS:
    //   Restituisce una mappa che associa a ogni utente appartenente a MicroBlog
    //   l'insieme di utenti che lo seguono. Formalmente si tratta di tutte le
    //   coppie
    //     <user_i, {u ∈ this.getUsers() | this.getPresentationPost(user_i).getLikes().contains(u)}>
    public Map<String, Set<String>> getFollowers() {
        return SocialNetwork.reverseFollowRelation(this.followees);
    }

    // MODIFIES:
    //   Nessuna modifica.
    // EFFECTS:
    //   Restituisce una mappa che associa a ogni utente appartenente al social
    //   network l'insieme di utenti che questo segue. Formalmente si tratta di
    //   tutte le coppie
    //     <user_i, followees_i>
    //   tali per cui `followees_i` è l'insieme degli utenti al post di
    //   presentazione dei quali `user_i` ha messo like.
    public Map<String, Set<String>> getFollowees() {
        return this.followees;
    }

    // MODIFIES:
    //   Nessuna modifica.
    // EFFECTS:
    //   Restituisce la lista di tutti gli utenti hanno postato almeno un post su
    //   MicroBlog:
    //     {s ∈ this.getUsers() | this.writtenBy(s).size() >= 1}
    public Set<String> getMentionedUsers() {
        return SocialNetwork.getMentionedUsers(this.getPosts());
    }

    // REQUIRES:
    //   `ps != null && (forall i | 0 <= i < ps.size() ==> ps.get(i) != null)`.
    // THROWS:
    //   `NullPointerException` se e solo se
    //   `ps == null || (forany i | 0 <= i < ps.size(), ps.get(i) == null)`.
    // MODIFIES:
    //   Nessuna modifica.
    // EFFECTS:
    //   Restituisce la lista di tutti gli utenti hanno mai postato almeno un post
    //   tra quelli presenti nella lista del parametro `ps`. Formalmente:
    //     {s | (forany p ∈ ps, String.equals(s, p.getAuthor()))
    public static Set<String> getMentionedUsers(List<Post> ps) throws NullPointerException {
        Set<String> mentionedUsers = new HashSet<>();
        for (Post post : ps) {
            mentionedUsers.add(post.getAuthor());
        }
        return mentionedUsers;
    }

    // REQUIRES:
    //   `ps != null && (forall i | 0 <= i < ps.size() ==> ps.get(i) != null)`.
    // THROWS:
    //   `NullPointerException` se e solo se
    //   `ps == null || (forany i | 0 <= i < ps.size(), ps.get(i) == null)`.
    // MODIFIES:
    //   Nessuna modifica.
    // EFFECTS:
    //   Restituisce una mappa che associa a ogni utente l'insieme di tutti e soli
    //   gli utenti che lo seguono. Formalmente si tratta di tutte le coppie
    //     <user_i, followers_i>
    //   tali per cui `followers_i` è l'insieme di utenti che hanno messo like al
    //   post di presentazione di `user_1`.
    public static Map<String, Set<String>> guessFollowers(List<Post> ps) throws NullPointerException {
        if (ps == null) {
            throw new NullPointerException();
        }
        Map<String, Set<String>> followers = new HashMap<>();
        for (Post post : ps) {
            if (post == null) {
                throw new NullPointerException();
            // Se non esistono ancora `followers` associati, vuol dire che si
            // tratta del post di presentazione.
            } else if (!followers.containsKey(post.getAuthor())) {
                followers.put(post.getAuthor(), new HashSet<String>(post.getLikes()));
            }
        }
        return followers;
    }

    // REQUIRES:
    //   `followers != null
    //    && (forall <k, v> ∈ followers
    //        ==> k != null
    //         && v != null
    //         && (forall f ∈ v ==> f != null))`.
    // THROWS:
    //   `NullPointerException` se e solo se
    //   `followers == null
    //    || (forany <k, v> ∈ followers,
    //        k == null || v == null || (forany f ∈ v, f == null))`.
    // MODIFIES:
    //   Nessuna modifica.
    // EFFECTS:
    //   Restituisce una lista di nomi utente estratta da `followers` che sono
    //   seguiti da meno utenti di quanti non ne seguano.
    public static List<String> influencers(Map<String, Set<String>> followers) throws NullPointerException {
        List<String> influencers = new ArrayList<>();
        Map<String, Set<String>> followees = SocialNetwork.reverseFollowRelation(followers);
        for (String username : followers.keySet()) {
            int numOfFollowers = followers.get(username).size();
            int numOfFollowees = followees.get(username).size();
            if (numOfFollowers > numOfFollowees) {
                influencers.add(username);
            }
        }
        return influencers;
    }

    // Crea e restituisce una mappa con gli stessi nomi utente di `followers` ma
    // invertendone la relazione binaria:
    //
    //   - `followers` associa a ogni utente la lista di utenti che lo seguono.
    //   - `followees` associa a ogni utente la lista di utenti che sono seguiti
    //     dallo stesso.
    //
    // REQUIRES:
    //   `followers != null
    //    && !followers.containsKey(null)
    //    && (forall v ∈ followers.values()
    //        ==>
    //       (v != null && (forall s ∈ v ==> s != null)))`.
    // THROWS:
    //   `NullPointerException` se e solo se `followers == null` o se `followers`
    //   contiene valori `null`.
    // MODIFIES:
    //   Nessuna modifica.
    // EFFECTS:
    //   Restituisce una mappa invertita rispetto a `followers`, seguendo le
    //   modalità descritte sopra.
    // EXAMPLES:
    //   La lista di `followers` potrebbe essere così:
    //     > Mario -> [Beatrice, Filippo, Lucia, Emma, Federico]
    //     > Beatrice -> []
    //     > Filippo -> [Mario, Beatrice, Federico]
    //     > Lucia -> [Filippo]
    //     > Emma -> [Mario, Filippo, Lucia]
    //     > Federico -> [Mario]
    //   Il valore atteso dal metodo sarebbe il seguente (in ordine qualsiasi):
    //     > Mario -> [Filippo, Emma, Federico]
    //     > Beatrice -> [Mario, Filippo]
    //     > Filippo -> [Mario, Lucia, Emma]
    //     > Lucia -> [Mario, Emma]
    //     > Emma -> [Mario]
    //     > Federico -> [Mario, Filippo]
    public static Map<String, Set<String>> reverseFollowRelation(Map<String, Set<String>> followers) {
        Map<String, Set<String>> followees = new HashMap<>();
        for (String followee : followers.keySet()) {
            if (!followees.containsKey(followee)) {
                followees.put(followee, new HashSet<String>());
            }
            for (String follower : followers.get(followee)) {
                if (!followees.containsKey(follower)) {
                    followees.put(follower, new HashSet<String>());
                }
                followees.get(follower).add(followee);
            }
        }
        return followees;
    }

    // Restituisce la lista di tutti i post scritti dall'utente denominato
    // `username`. Nessun ordine in particolare è specificato.
    //
    // REQUIRES:
    //   `username != null`.
    // THROWS:
    //   `NullPointerException` se e solo se `username == null`.
    // MODIFIES:
    //   Nessuna modifica.
    // EFFECTS:
    //   Restituisce la lista di tutti i post scritti dall'utente denominato
    //   `username`. Formalmente:
    //     {p ∈ this.getPosts() | String.equalst(p.getAuthor(), username)}
    public List<Post> writtenBy(String username) throws NullPointerException {
        if (username == null) {
            throw new NullPointerException();
        } else if (!this.postsByUser.containsKey(username)) {
            return new ArrayList<Post>();
        } else {
            List<Post> posts = new ArrayList<>();
            for (Post writtenBy : this.postsByUser.get(username)) {
                posts.add(writtenBy.deepCopy());
            }
            return posts;
        }
    }

    // Restituisce la lista di tutti i post appartenenti a `ps` scritti
    // dall'utente denominato `username`. Nessun ordine in particolare è specificato.
    //
    // REQUIRES:
    //   `ps != null && username != null && (forall i | 0 <= i < ps.size() ==> ps.get(i) != null)`.
    // THROWS:
    //   `NullPointerException` se e solo se
    //   `ps == null || username == null || (forany i | 0 <= i < ps.size(), ps.get(i) == null)`.
    // MODIFIES:
    //   Nessuna modifica.
    // EFFECTS:
    //   Restituisce la lista di tutti i post di `ps`. scritti dall'utente denominato
    //   `username`. Formalmente:
    //     {p ∈ ps | p.getAuthor() == username}
    public static List<Post> writtenBy(List<Post> ps, String username) throws NullPointerException {
        if (ps == null || username == null) {
            throw new NullPointerException();
        }
        List<Post> posts = new ArrayList<Post>();
        for (Post post : ps) {
            if (post == null) {
                throw new NullPointerException();
            } else if (post.getAuthor().equals(username)) {
                posts.add(post);
            }
        }
        return posts;
    }

    // Restituisce la lista di tutti i post all'interno del social network che
    // contengono una o più dei termini di ricerca richiesti.
    //
    // REQUIRES:
    //   `words != null && (forall w ∈ words ==> w != null)`.
    // THROWS:
    //   `NullPointerException` se e solo se
    //   `words == null || (forany i | 0 <= i < words.size(), w == null)`.
    // MODIFIES:
    //   Nessuna modifica.
    // EFFECTS:
    //   Restituisce la lista di tutti i post all'interno del social network che
    //   contengono una o più dei termini di ricerca richiesti. Formalmente:
    //     {p ∈ this.getPosts() | (forany i | 0 <= i < words.size(), p.getText().contains(i))}
    public List<Post> containing(List<String> words) throws NullPointerException {
        if (words == null) {
            throw new NullPointerException();
        }
        List<Post> results = new ArrayList<>();
        for (Post post : this.getPosts()) {
            for (String word : words) {
                if (post == null) {
                    throw new NullPointerException();
                } else if (post.getText().contains(word)) {
                    results.add(post.deepCopy());
                    break;
                }
            }
        }
        return results;
    }

    // Verifica l'estistenza dell'utente denominato `username` all'interno del
    // social network.
    //
    // REQUIRES:
    //   `username != null`.
    // THROWS:
    //   `NullPointerException` se e solo se `username == null`.
    // MODIFIES:
    //   Nessuna modifica.
    // EFFECTS:
    //   Restituisce `true` se e solo se `this.getUsers().contains(username)`,
    //   `false` altrimenti.
    public boolean userExists(String username) {
        return this.followees.containsKey(username);
    }

    // Crea un nuovo utente denominato `username` sul social network.
    // social network.
    //
    // REQUIRES:
    //   `username != null
    //    && (forall i | 0 <= i < this.getUsers().size()
    //        ==> this.getUsers().get(i) != username)
    //    && User.isUsernameOk(username)`.
    // THROWS:
    //   `NullPointerException` se e solo se `username == null`.
    //   `IllegalArgumentException` se e solo se il nome utente è già occupato
    //   oppure non rispetta i requisiti imposti da MicroBlog.
    // MODIFIES:
    //   `this`.
    // EFFECTS:
    //   Aggiunge `username` al registro interno degli utenti di MicroBlog e
    //   restituisce il nome utente invariato `username`.
    public String register(String username) throws NullPointerException, IllegalArgumentException {
        if (this.userExists(username)) {
            throw new IllegalArgumentException();
        }
        this.followees.put(username, new HashSet<String>());
        this.postsByUser.put(username, new ArrayList<Post>());
        return username;
    }

    // Pubblica un post sul social network. Dato `builder` il parametro in
    // ingresso, il post risultante sarà noto come `post` nella seguente
    // specifica.
    //
    // REQUIRES:
    //   `post != null &&
    //    && (post.getParent() != null ==> this.getPosts().contains(post.getParent());
    // THROWS:
    //   `NullPointerException` se e solo se `post == null`.
    //   `IllegalArgumentException` se e solo se
    //   `post.getParent() != null && this.getPosts().contains(post.getParent())`.
    // MODIFIES:
    //   `this`.
    // EFFECTS:
    //   Restituisce una copia del post creato su MicroBlog. Inoltre, formalmente,
    //   si ha che se
    //     this_pre := <{user_0, user_1, ... user_n}, {post_0, post_1, ... post_m}>
    //   allora
    //     this_post := <{user_0, user_1, ... user_n}, {post_0, post_1, ... post_m, post_m+1}>
    //   e il valore restituito è
    //     <post_m+1>
    public Post writePost(Post.Builder builder) throws NullPointerException, IllegalArgumentException {
        if (builder == null) {
            throw new NullPointerException();
        }
        Post post = builder.build();
        String author = post.getAuthor();
        if (!this.userExists(author)) {
            throw new IllegalArgumentException();
        }
        if (post.getParent() != null && this.getPosts().contains(post.getParent())) {
            throw new IllegalArgumentException();
        }
        this.postsByUser.get(author).add(post);
        this.postsById.put(post.getId(), post);
        return post.deepCopy();
    }

    // REQUIRES:
    //   `post != null
    //    && username != null
    //    && this.getPosts().contains(post)`.
    // MODIFIES:
    //   `this`.
    // THROWS:
    //   `NullPointerException` se e solo se `post == null || username == null`.
    //   `IllegalArgumentException` se e solo se `!this.getPosts().contains(post)`.
    // EFFECTS:
    //   Aggiunge il like a `post` da parte di `username` e lo aggiunge alla lista
    //   di followers dell'autore se il post è di presentazione. Nessuna
    //   operazione viene effettuata se l'utente ha già messo like al post.
    public void like(Post post, String username) throws NullPointerException, IllegalArgumentException {
        if (post == null || username == null) {
            throw new NullPointerException();
        }
        Post internalPost = this.postsById.get(post.getId());
        if (!internalPost.isLikedBy(username)) {
            internalPost.toggleLike(username);
            Post firstPost = this.getPresentationPost(post.getAuthor());
            if (firstPost.getId() == post.getId()) {
                this.followees.get(username).add(post.getAuthor());
            }
        }
    }

    // REQUIRES:
    //   `post != null
    //    && username != null
    //    && this.getPosts().contains(post)`.
    // MODIFIES:
    //   `this`.
    // THROWS:
    //   `NullPointerException` se e solo se `post == null || username == null`.
    //   `IllegalArgumentException` se e solo se `!this.getPosts().contains(post)`.
    // EFFECTS:
    //   Toglie il like a `post` da parte di `username` e lo rimuove alla lista
    //   di followers dell'autore se il post è di presentazione. Nessuna
    //   operazione viene effettuata se l'utente non ha già messo like al post.
    public void dislike(Post post, String username) throws NullPointerException, IllegalArgumentException {
        if (post == null || username == null) {
            throw new NullPointerException();
        }
        Post internalPost = this.postsById.get(post.getId());
        if (internalPost.isLikedBy(username)) {
            internalPost.toggleLike(username);
        }
        Post firstPost = this.getPresentationPost(post.getAuthor());
        if (firstPost.getId() == post.getId()) {
            this.followees.get(username).remove(post.getAuthor());
        }
    }

    // REQUIRES:
    //   `username != null && this.writtenBy(username).size() != 0`.
    // THROWS:
    //   `NullPointerException` se e solo se `username == null`.
    //   `IllegalArgumentException` se e solo se l'utente non ha ancora pubblicato
    //   alcun post di presentazione.
    // MODIFIES:
    //   Nessuna modifica.
    // EFFECTS:
    //   Restituisce una copia del post di presentazione scritto da `username`.
    public Post getPresentationPost(String username) throws NullPointerException, IllegalArgumentException {
        if (username == null) {
            throw new NullPointerException();
        }
        List<Post> posts = this.postsByUser.get(username);
        if (posts.size() == 0) {
            throw new IllegalArgumentException();
        } else {
            return posts.get(0).deepCopy();
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
    //   rappresentazione della classe `SocialNetwork`, `false` altrimenti.
    public boolean checkRep() {
        for (Post ps : this.getPosts()) {
            if (!ps.checkRep()) {
                return false;
            }
        }
        return true;
    }
}