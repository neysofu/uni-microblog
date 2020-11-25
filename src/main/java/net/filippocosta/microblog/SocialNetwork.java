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
    Map<String, List<Post>> posts;

    public List<Post> getPosts() {
        List<Post> posts = new ArrayList<>();
        for (Map.Entry<String, List<Post>> entry : this.posts.entrySet()) {
            for (Post post : entry.getValue()) {
                posts.add(post);
            }
        }
        return posts;
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
            if (!followers.containsKey(post.author)) {
                followers.put(post.author, new HashSet<String>());
            }
            for (String userWhoLiked : post.likes) {
                if (!followers.containsKey(userWhoLiked)) {
                    followers.put(userWhoLiked, new HashSet<String>());
                }
                followers.get(post.author).add(userWhoLiked);
            }
        }
        return followers;
    }

    /**
     * Return a `List` of the top 10 users by follower count based on the
     * `followers` data.
     * 
     * @param followers a `Map` of 'influencers'.
     * @effects None.
     * @throws NullPointerException if `isNull(followers)`.
     * @return A `List` of 'influencers'.
     */
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

    public Set<String> getMentionedUsers() {
        return SocialNetwork.getMentionedUsers(this.getPosts());
    }

    public static Set<String> getMentionedUsers(List<Post> ps) {
        Set<String> mentionedUsers = new HashSet<>();
        for (Post post : ps) {
            mentionedUsers.add(post.author);
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
        return this.posts.get(username);
    }

    public static List<Post> writtenBy(List<Post> ps, String username) {
        List<Post> posts = new ArrayList<>();
        for (Post post : ps) {
            if (post.author.equals(username)) {
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
                if (post.text.contains(word)) {
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

    /**
     * 
     * @param username
     * @modifies this
     */
    public void createAccount(String username) {
        if (this.userExists(username)) {
            // Duplicate usernames are not allowed!
            throw new IllegalArgumentException();
        }
        // Create (empty, for now) data associated with `username`.
        this.followees.put(username, new HashSet<String>());
        this.posts.put(username, new ArrayList<Post>());
    }

    public boolean writePost(String username, Post post) {
        if (!this.userExists(username)) {
            // Only existing users can write posts!
            throw new IllegalArgumentException();
        }
        this.posts.get(username).add(post);
        return true;
    }
    
    public boolean checkRep() {
        for (Post ps : this.getPosts()) {
            if (!ps.checkRep()) {
                return false;
            }
        }
        return true;
    }
}