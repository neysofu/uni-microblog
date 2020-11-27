package net.filippocosta.microblog;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A fully self-contained MicroBlog instance.
 */
class SocialNetwork implements CheckRep {

    /**
     * `Set` of followers by user.
     * 
     * RI: followees.forEach(user -> followees[user])
     */
    Map<String, Set<String>> followees;
    Map<String, Set<Post>> posts;

    public List<Post> getPosts() {
        List<Post> posts = new ArrayList<>();
        for (Map.Entry<String, Set<Post>> entry : this.posts.entrySet()) {
            for (Post post : entry.getValue()) {
                posts.add(post);
            }
        }
        return posts;
    }

    public List<String> getUsers() {
        return new ArrayList<String>(this.followees.keySet());
    }

    /**
     * Give a `Set` of followers by post.
     * 
     * @param   ps a `List` of posts, the authors of which will be the keys of the
     *          returned `Map`.
     * @effects None.
     * @throws  NullPointedException if `isNull(ps)`.
     * @return  a `Map` of followers by user (`String`) that includes all and only
     *          the authors of the given posts `ps`.
     */
    public static Map<String, Set<String>> guessFollowers(List<Post> ps) throws NullPointerException {
        Map<String, Set<String>> followers = new HashMap<>();
        for (Post post : ps) {
            if (!followers.containsKey(post.getAuthor())) {
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

    public List<String> getMentionedUsers() {
        return new ArrayList<String>(SocialNetwork.getMentionedUsers(this.getPosts()));
    }

    public Map<String, Set<String>> getFollowers() {
        return this.followees;
    }

    public Map<String, Set<String>> getFollowees() {
        return this.followees;
    }

    public static Set<String> getMentionedUsers(List<Post> ps) {
        Set<String> mentionedUsers = new HashSet<>();
        for (Post post : ps) {
            mentionedUsers.add(post.getAuthor());
            for (String taggedUser : post.getTaggedUsers()) {
                mentionedUsers.add(taggedUser);
            }
        }
        return mentionedUsers;
    }

    /**
     * Return a `List` of all posts ever written by `username`.
     * 
     * @effects None.
     * @throws NullPointerException
     * @return a `List<Post>` instance such that for all posts the author is
     *         `username`.
     */
    public List<Post> writtenBy(String username) {
        return new ArrayList<Post>(this.posts.get(username));
    }

    public static List<Post> writtenBy(List<Post> ps, String username) {
        List<Post> posts = new ArrayList<Post>();
        for (Post post : ps) {
            if (post.getAuthor().equals(username)) {
                posts.add(post);
            }
        }
        return posts;
    }

    /**
     * Search some keywords amongst the text of all posts.
     * 
     * @param words
     * @return
     */
    public List<Post> containing(List<String> words) {
        List<Post> results = new ArrayList<>();
        for (Post post : this.getPosts()) {
            for (String word : words) {
                if (post.getText().contains(word)) {
                    results.add(post);
                }
            }
        }
        return results;
    }

    /**
     * 
     * @param username the username we must check the existance of.
     * @return `true` if and only if `username` is a real user in this MicroBlog
     *         instance; `false` otherwise.
     */
    public boolean userExists(String username) {
        return this.followees.containsKey(username);
    }

    public String register(String username) {
        if (this.userExists(username)) {
            // Duplicate usernames are not allowed!
            throw new IllegalArgumentException();
        }
        // Create (empty, for now) data associated with `username`.
        this.followees.put(username, new HashSet<String>());
        this.posts.put(username, new HashSet<Post>());
        return username;
    }

    public boolean writePost(String username, Post post) {
        if (!this.userExists(username)) {
            // Only existing users can write posts!
            throw new IllegalArgumentException();
        }
        this.posts.get(username).add(post);
        return true;
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