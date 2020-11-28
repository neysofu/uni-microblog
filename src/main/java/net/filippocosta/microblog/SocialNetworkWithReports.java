package net.filippocosta.microblog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// OVERVIEW:
//   Questa sottoclasse di `SocialNetwork` aggiunge un'informazione (`reports`) al
//   tipo di dato astratto. L'elemento tipico diventa perciò diventa da
//     <{post_0, post_1, ... post_n}>
//   a
//     <{<post_0, {segnalazione_0, ... segnalazione_m0}>,
//       <post_1, {segnalazione_0, ... segnalazione_m1}>}>
//   Ciò significa che a ogni post si aggiunge un insieme di segnalazioni da parte
//   di altri utenti.
public class SocialNetworkWithReports extends SocialNetwork {
    // AF(s):
    //
    // RI(s):
    //   RI_SocialNetwork(s)
    //   &&

    private Map<Post, Set<String>> reports;

    // Costruttore per la classe `SocialNeworkWithReports`.
    //
    // EFFECTS:
    //   Restituisce una nuova istanza di `SocialNetworkWithReports` senza utenti
    //   né post (e perciò anche senza segnalazioni ai post).
    //   Formalmente:
    //     <{}>
    public SocialNetworkWithReports() {
        super();
        this.reports = new HashMap<>();
    }

    // Segnala un post.
    //
    // REQUIRES:
    //   `post != null
    //    && username != null
    //    && this.getUsers().contains(username)
    //    && this.getPosts().contains(post)`.
    // THROWS:
    //   `NullPointerException` se e solo se `post == null || username == null`.
    //   `IllegalArgumentException` se e solo se `TODO`.
    // MODIFIES:
    //   `this`.
    // EFFECTS:
    //   Aggiunge la segnalazione da parte di `username` a `post`. Formalmente si
    //   ha da
    //     this_pre := <{<post_0, {segnalazione_0, ... segnalazione_m0}>,
    //                   <post_1, {segnalazione_0, ... segnalazione_m1}>}>
    //                   ...
    //                   <post_i, {segnalazione_i, ... segnalazione_i1}>}>
    //                   ...
    //                   <post_n, {segnalazione_0, ... segnalazione_mn}>}>
    //   a
    //     this_pre := <{<post_0, {segnalazione_0, ... segnalazione_m0}>,
    //                   <post_1, {segnalazione_0, ... segnalazione_m1}>}>
    //                   ...
    //                   <post_i, {segnalazione_i, ... segnalazione_i1, segnalazione_i1+1}>}>
    //                   ...
    //                   <post_n, {segnalazione_0, ... segnalazione_mn}>}>
    //   dove `post_1` identifica il parametro `post`. Nessuna operazione viene
    //   effettuata in caso la segnalazione da parte dell'utente sia già presente.
    public void report(Post post, String username) {
        this.reports.get(post).add(username);
    }

    @Override
    public Post writePost(Post.Builder builder) {
        Post post = super.writePost(builder);
        this.reports.put(post, new HashSet<String>());
        return post;
    }

    // REQUIRES:
    //   `post != null && this.getPosts().contains(post)`.
    // THROWS:
    //   `NullPointerException` se e solo se `post == null`.
    // MODIFIES:
    //   Nessuna modifica.
    // EFFECTS:
    //   Restituisce `true` se e solo se il post supera una certa soglia di
    //   segnalazioni, `false` altrimenti. Il numero di segnalazioni da superare
    //   non fa parte della specifica ed è da considerarsi un dettaglio
    //   dell'implementazione.
    public boolean postIsBlacklisted(Post post) {
        if (post == null) {
            throw new NullPointerException();
        } else if (!this.reports.containsKey(post)) {
            throw new IllegalArgumentException();
        } else {
            return this.reports.get(post).size() > java.lang.Math.sqrt(this.getUsers().size());
        }
    }

    // MODIFIES:
    //   Nessuna modifica.
    // EFFECTS:
    //   Restituisce una lista di tutti i post presenti all'interno del social
    //   network da considersi "blacklisted", ovvero soggetti a numerose
    //   segnalazioni e quindi ad alto rischio di spam. Il valore restituito,
    //   formalmente, è
    //     val := <{post_0, post_1, ... post_n}>
    //   tale per cui
    //     (forall i. 0 <= i < this.getPosts().size()
    //     ==>
    //     (this.postIsBlacklisted(this.getPosts().get(i))
    //      <==>
    //      this.blacklist().contains(this.getPosts().get(i)))
    public List<Post> blacklist() {
        List<Post> blacklist = new ArrayList<>();
        for (Post post : this.getPosts()) {
            if (this.postIsBlacklisted(post)) {
                blacklist.add(post.deepCopy());
            }
        }
        return blacklist;
    }
}
