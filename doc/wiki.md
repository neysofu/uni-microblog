---
title: Primo Progetto Intermedio
author: Filippo Costa
date: Novembre 2020
---

# 1. Introduzione e generalità

La struttura del progetto (*directory layout*) segue le convenzioni classiche dello strumento Maven, ovvero il *build tool* più usato nell'industria per i progetti Java. Strumenti quali Maven non sono necessari per un progetto dalle dimensioni ridotte quali *MicroBlog*, ma permettono di compilare velocemente l'intero progetto usando ambienti di sviluppo integrato. In alternativa a Maven si può compilare da linea da comando i file sorgente che si trovano nella cartella `mavenless`:

```
$ javac -d . *
$ java net.filippocosta.microblog.Main
```

Il pacchetto –denominato `net.filippocosta.microblog` in accordo con le direttive ufficiali [Oracle](https://docs.oracle.com/javase/tutorial/java/package/namingpkgs.html)– contiene numerose classi che concorrono alla realizzazione del progetto e alla batteria di test. Le tre parti del progetto sono implementate rispettivamente nei file `Post.java`, `SocialNetwork.java` e `SocialNetworkWithReports.java`. Il sistema rispetta rigorosamente tutte le specifiche date e si discosta solo in funzionalità aggiuntive o quando l'interpretazione di una meccanica è lasciata al progettista.

# 2. Come funziona MicroBlog

MicroBlog è un componente software scritto in Java dedito alla gestione e all'analisi di un social network. Gli utenti iscritti a MicroBlog possono pubblicare contenuti sotto forma di *post* e interagirci in due modi:

1. Mettere *like* ai post. I like sono manifestazioni di supporto o di approvazione a un post da parte di utenti terzi. Un utente non può mettere like a un proprio post.
2. Rispondere ai post. Al momento di pubblicazione di un post, infatti, è possibile selezionare un post preesistente a cui rispondere. Un utente può anche rispondere a se stesso.

I post non sono editabili ma gli autori rispettivi possono eliminarli in qualsiasi momento. Se un post viene eliminato, anche i like e le risposte al post vengono eliminati immediatamente.

Il contenuto dei post è prettamente testuale, senza la possibilità di allegare immagini o video. Esistono però due forme di contenuti infra-testuali: gli *hashtag* e i *tag*.

- Gli *hashtag* sono parole o concatenazioni di parole precedute da un cancelletto (`#`). L'uso di un particolare hashtag permette di mettere in evidenza il post se l'hashtag è virale e di facilitare il suggerimento di contenuti simili tramite un'operazione di *keyword matching*.
- I *tag* sono la menzione esplicita di un nome utente preceduto dalla chiocciola (`@`). Taggando un utente lo si autorizza a scrivere una risposta al post secondo le modalità descritto nel paragrafo 2.2.

## 2.1 Gli utenti

Gli utenti iscritti al sistema sono idenficati tramite *username* unico. Prendendo ispirazione da molti social network esistenti ho deciso di limitare la scelta di username a combinazioni di lunghezza 3-12 caratteri di caratteri alfanumerici con underscore (`_`). Ognuna di queste limitazioni è giustificata ampiamente nel codice relativo alla classe `User`. Si sottolinea che, una volta iscritti al sistema, gli utenti non sono sottoposti ad autenticazione tramite password o meccanismi di sessione. È palese che un vero social network avrebbe bisogno di queste funzionalità, ma su MicroBlog non sono necessarie.

## 2.2 Il meccanismo di risposta ai post

Di default il sistema autorizza chiunque a rispondere a un post, ma questa impostazione è modificabile al momento di creazione di un post (come documentato dalla classe `Post.Builder`). In tal modo si va a limitare il numero di utenti che sono autorizzati a rispondere al post:

- Autorizzazione pubblica (predefinito). Chiunque può rispondere al post.
- Autorizzazione riservata. Solo l'autore del post e gli utenti taggati nel post possono rispondervi.
- Nessuna autorizzazione. Soltanto l'autore del post può rispondervi.

## 2.3 *followers* e *followees*

È possibile *seguire* un utente mettendo like al suo post di presentazione, ovvero il primo post pubblicato in assoluto sulla piattaforma. (Chiaramente non è possibile seguire un utente senza post.) In maniera analoga, togliendo il like al post di presentazione si smette di seguire l'autore di tale post. In caso di eliminazione di un post il secondo post diviene quello di presentazione e così via.

Più formalmente, seguire un utente significa stabilire una relazione binaria unidirezioinale. La rete di connessioni crea in tal modo un grafo orientato in cui ciascun nodo rappresenta un utente.

> `u1` è un *follower* di `u2` se e solo se `u1` segue `u2`. L'arco esce da `u1` ed entra in `u2`.
> `u1` è un *followee* di `u2` se e solo se `u2` segue `u1` (relazione inversa, simile al concetto di *employer* e *employee*). L'arco esce da `u2` ed entra in `u1`.

## 3. Scelte di implementazione

## 4. Estendere MicroBlog: parte III del progetto

Per la terza parte del progetto ho implementato `SocialNetworkWithReports`, sottotipo di `SocialNetwork`. La nuova classe permette agli utenti di segnalare come spam o offensivi i contenuti di un post e tiene traccia del numero di segnalazioni per post. Oltre a un certo numero di segnalazioni, i post sono considerati *blacklisted* e l'utilizzatore finale della classe può prendere provvedimenti, per esempio riducendo la visibilità del post od oscurandolo. Di seguito si presentano altre due possibili estensioni gerarchiche della classe `SocialNetwork`.

- `ModeratedSocialNetwork`, che garantisce ad alcuni utenti lo status di admin e permette loro di rimuovere manualmente qualsiasi post, anche non loro. Lo status di admin può (per ovvi motivi di sicurezza) essere garantito solo dal *superuser*, che è unico e va creato assieme al social network nel costruttore.
- `AutoModeratedSocialNetwork` aggiunge una serie di controlli al momento di pubblicazione dei post e blocca la pubblicazione dei post che non superano certi criteri:
  - Solleva un'eccezione `ProfanityDetectedException` se il post contiene termini offensivi o dispregiativi.
  - Solleva un'eccezione `MaliciousLinkException` se il post contiene link a siti di spam, porno o di pirateria.
  - Solleva un'eccezione `SpamHashtagsException` se il post contiene più hashtags che testo.

Sia `ModeratedSocialNework` che `SocialNetworkWithReports` vanno a modificare il tipo di dato astratto associato alla classe (TDA) e di conseguenza anche la riformulazione di *AF* e *RI*. `AutoModeratedSocialNetwork` invece non richiede una modifica al TDA ma ha semplicemente impone una RI più forte (ovvero l'assenza di post che contengono certi tipi di contenuti).