package net.filippocosta.microblog;

import java.util.Set;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

// OVERVIEW:
//   Rappresenta il social network del servizio MicroBlog. Il tipo di dato
//   astratto è formato dalle seguenti informazioni:
//     <{<user_0, {follower_0_0, ... follower_0_n0}>, 
//       <user_1, {follower_1_0, ... follower_1_n1}>,
//       <user_2, {follower_2_0, ... follower_2_n2}>,
//       ...
//       <user_m, {follower_m_0, ... follower_m_nm}>},
//      {post_0, post_1, ... post_k}>
class SocialNetwork implements CheckRep {
    // AF(c):
    //   TODO
    // RI(c):
    //   c.followees != null
    //   && (forall key, value. c.followees ==> key != null && value != null && (forall s. value ==> s != null))
    //
    //   && (forall p. p.likes ==> u != null && User.usernameIsOk(u))
    //   && c.posts != null
    //
    //
    //
    //
    //
    //

    private Map<String, Set<String>> followees;
    private Map<String, List<Post>> postsByUser;
    private Map<Integer, Post> postsById;

    // Costruttore per la classe `SocialNetwork`.
    //
    // EFFECTS:
    //   Restituisce una nuova istanza di `SocialNetwork` senza utenti né post.
    //   Formalmente:
    //     <{}, {}, {}>
    public SocialNetwork() {
        this.followees = new HashMap<>();
        this.postsByUser = new HashMap<>();
        this.postsById = new HashMap<>();
    }

    // EFFECTS:
    //   Restituisce una lista di tutti i post presenti su MicroBlog. L'ordine non
    //   è specificato.
    public List<Post> getPosts() {
        List<Post> posts = new ArrayList<>();
        for (Map.Entry<String, List<Post>> entry : this.postsByUser.entrySet()) {
            for (Post post : entry.getValue()) {
                posts.add(post);
            }
        }
        return posts;
    }

    // EFFECTS:
    //   Restituisce una lista di tutti gli utenti registrati a MicroBlog.
    //   L'ordine non è specificato.
    public List<String> getUsers() {
        return new ArrayList<String>(this.followees.keySet());
    }

    // REQUIRES:
    //   `ps != null && (forall i. 0 <= i < ps.size() ==> ps.get(i) != null)`.
    // THROWS:
    //   `NullPointerException` se e solo se
    //   `ps == null || (forany i. 0 <= i < ps.size(), ps.get(i) == null)`.
    // MODIFIES:
    //   Nessuna modifica.
    // EFFECTS:
    //   Restituisce una mappa che associa a ogni utente l'insieme di tutti e soli
    //   gli utenti che lo seguono. Le relazioni sono estrapolate da `ps`.
    public static Map<String, Set<String>> guessFollowers(List<Post> ps) throws NullPointerException {
        if (ps == null) {
            throw new NullPointerException();
        }
        Map<String, Set<String>> followers = new HashMap<>();
        for (Post post : ps) {
            if (post == null) {
                throw new NullPointerException();
            } else if (!followers.containsKey(post.getAuthor())) {
                followers.put(post.getAuthor(), new HashSet<String>());
            }
            for (String userWhoLiked : post.getLikes()) {
                if (!followers.containsKey(userWhoLiked)) {
                    followers.put(userWhoLiked, new HashSet<String>());
                }
                followers.get(post.getAuthor()).add(userWhoLiked);
            }
        }
        return followers;
    }

    // Restituisce una lista con tutti i nomi utente di `followers` che sono
    // seguiti da più persone di quante non ne seguano.
    //
    // REQUIRES:
    //   `followers != null`.
    // THROWS:
    //   `NullPointerException` se e solo se `followers == null`.
    // MODIFIES:
    //   Nessuna modifica.
    // EFFECTS:
    //   Restituisce una lista di nomi utente estratta da `followers` che sono
    //   seguiti da meno utenti di quanti non ne seguano.
    public static List<String> influencers(Map<String, Set<String>> followers) throws NullPointerException {
        List<String> influencers = new ArrayList<>();
        Map<String, Set<String>> followees = SocialNetwork.getFollowees(followers);
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
    //    && (forall v. followers.values()
    //        ==>
    //       (v != null && (forall s. v ==> s != null)))`.
    // THROWS:
    //   `NullPointerException` se e solo se `followers == null`.
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
    public static Map<String, Set<String>> getFollowees(Map<String, Set<String>> followers) {
        Map<String, Set<String>> followees = new HashMap<>();
        for (String followee : followers.keySet()) {
            for (String follower : followers.get(followee)) {
                if (!followees.containsKey(followee)) {
                    followees.put(follower, new HashSet<String>());
                }
                followees.get(follower).add(followee);
            }
        }
        return followees;
    }

    // Restituisce la lista di tutti gli utenti che hanno mai interagito con il
    // social network. Essere iscritto a MicroBlog non è sufficiente e serve
    // soddisfare uno dei seguenti requisiti:
    //   - Mettere like a un post.
    //   - Scrivere a un post.
    //   - Venire taggati da un altro utente in un post.
    //
    // MODIFIES:
    //   Nessuna modifica.
    // EFFECTS:
    //   Restituisce la lista di tutti gli utenti hanno main interagito con il
    //   social network.
    public List<String> getMentionedUsers() {
        return new ArrayList<String>(SocialNetwork.getMentionedUsers(this.getPosts()));
    }

    public Map<String, Set<String>> getFollowers() {
        return this.followees;
    }

    public Map<String, Set<String>> getFollowees() {
        return this.followees;
    }

    // Restituisce la lista di tutti gli utenti che hanno mai interagito con il
    // social network. Essere iscritto a MicroBlog non è sufficiente e serve
    // soddisfare uno dei seguenti requisiti:
    //   - Mettere like a un post.
    //   - Scrivere a un post.
    //   - Venire taggati da un altro utente in un post.
    //
    // REQUIRES:
    //   `ps != null && (foreach p. ps ==> p != null)`.
    // THROWS:
    //   `NullPointerException` se e solo se `ps == null`.
    // MODIFIES:
    //   Nessuna modifica.
    // EFFECTS:
    //   Restituisce la lista di tutti gli utenti hanno main interagito con il
    //   social network.
    public static Set<String> getMentionedUsers(List<Post> ps) {
        Set<String> mentionedUsers = new HashSet<>();
        for (Post post : ps) {
            mentionedUsers.add(post.getAuthor());
            for (String taggedUser : post.getTaggedUsers()) {
                mentionedUsers.add(taggedUser);
            }
            for (String userWhoLiked : post.getLikes()) {
                mentionedUsers.add(userWhoLiked);
            }
        }
        return mentionedUsers;
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
    //   `username`.
    public List<Post> writtenBy(String username) throws NullPointerException {
        if (username == null) {
            throw new NullPointerException();
        } else if (!this.followees.containsKey(username)) {
            return new ArrayList<Post>();
        } else {
            List<Post> posts = new ArrayList<>();
            for (Post writtenBy : this.postsByUser.get(username)) {
                posts.add(writtenBy.deepCopy());
            }
            return posts;
        }
    }

    // Restituisce la lista di tutti i post scritti dall'utente denominato
    // `username`. Nessun ordine in particolare è specificato.
    //
    // REQUIRES:
    //   `ps != null && username != null && (forall i. 0 <= i < ps.size() ==> ps.get(i) != null)`.
    // THROWS:
    //   `NullPointerException` se e solo se
    //   `ps == null || username == null || (forany i. 0 <= i < ps.size(), ps.get(i) == null)`.
    // MODIFIES:
    //   Nessuna modifica.
    // EFFECTS:
    //   Restituisce la lista di tutti i post scritti dall'utente denominato
    //   `username`.
    public static List<Post> writtenBy(List<Post> ps, String username) {
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
    //   `words != null && (forall w. words ==> w != null)`.
    // THROWS:
    //   `NullPointerException` se e solo se `words == null || (forany w. words: w == null)`.
    // MODIFIES:
    //   Nessuna modifica.
    // EFFECTS:
    //   Restituisce la lista di tutti i post all'interno del social network che
    //   contengono una o più dei termini di ricerca richiesti.
    public List<Post> containing(List<String> words) {
        if (words == null) {
            throw new NullPointerException();
        }
        List<Post> results = new ArrayList<>();
        for (Post post : this.getPosts()) {
            for (String word : words) {
                if (post == null) {
                    throw new NullPointerException();
                } else if (post.getText().contains(word)) {
                    results.add(post);
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
    //   Restituisce `true` se e solo se contiene `username`, `false` altrimenti.
    public boolean userExists(String username) {
        return this.followees.containsKey(username);
    }

    // Crea un nuovo utente denominato `username` sul social network.
    // social network.
    //
    // REQUIRES:
    //   `username != null && (forall u. this.getUsers() ==> u != username) && User.`.
    // THROWS:
    //   `NullPointerException` se e solo se `username == null`.
    //   `IllegalArgumentException` se e solo se il nome utente è già occupato
    //   oppure non rispetta i requisiti imposti da MicroBlog.
    // MODIFIES:
    //   `this`.
    // EFFECTS:
    //   TODO.
    public String register(String username) {
        if (this.userExists(username)) {
            throw new IllegalArgumentException();
        }
        this.followees.put(username, new HashSet<String>());
        this.postsByUser.put(username, new ArrayList<Post>());
        return username;
    }

    // Pubblica un post sul social network.
    //
    // REQUIRES:
    //   `post != null &&
    //    && (post.getParent() != null ==> this.getPosts().contains(post.getParent())
    //    && (post.getReplies().size() == 0`.
    // THROWS:
    //   `NullPointerException` se e solo se `post == null`.
    // MODIFIES:
    //   `this`.
    // EFFECTS:
    //   Restituisce ...
    public Post writePost(Post.Builder builder) throws NullPointerException, IllegalArgumentException {
        if (builder == null) {
            throw new NullPointerException();
        }
        Post post = builder.build();
        String author = post.getAuthor();
        if (!this.userExists(author)) {
            throw new IllegalArgumentException();
        }
        this.postsByUser.get(author).add(post);
        this.postsById.put(post.getId(), post);
        return post.deepCopy();
    }

    public void like(Post post, String username) throws NullPointerException, IllegalArgumentException {
        if (post == null || username == null) {
            throw new NullPointerException();
        }
        Post internalPost = this.postsById.get(post.getId());
        if (!internalPost.isLikedBy(username)) {
            internalPost.toggleLike(username);
        }
        Post firstPost = this.firstPostByUser(post.getAuthor());
        if (firstPost.getId() == post.getId()) {
            this.followees.get(username).add(post.getAuthor());
        }
    }

    public void dislike(Post post, String username) throws NullPointerException, IllegalArgumentException {
        if (post == null || username == null) {
            throw new NullPointerException();
        }
        Post internalPost = this.postsById.get(post.getId());
        if (internalPost.isLikedBy(username)) {
            internalPost.toggleLike(username);
        }
        Post firstPost = this.firstPostByUser(post.getAuthor());
        if (firstPost.getId() == post.getId()) {
            this.followees.get(username).remove(post.getAuthor());
        }
    }

    public Post firstPostByUser(String username) throws NullPointerException, IllegalArgumentException {
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