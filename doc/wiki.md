# Primo Progetto Intermedio

## 1. Introduzione e generalità

Questa relazione descrive le scelte progettuali del progetto.

## 1.1 Struttura del progetto

La struttura del progetto (*directory layout*) segue le convenzioni classiche dello strumento Maven. Maven è un programma da linea di comando che gestisce in modo complementamente automatico alcuni aspetti:

- Download di librerie e pacchetti esterni.
- Esecuzione di test.
- Compilazione.

Maven è il *build tool* più usato nell'industria per i progetti Java. La scelta di impostare il progetto seguendo le convenzioni Maven nasce con la speranza che i professori possano sentirsi a loro agio trovando una struttura di progetto familiare e già nota.

## 1.2 Nome del pacchetto Java

L'intero progetto è organizzato in un unico pacchetto Java denominato `net.filippocosta.microblog`. Ho deciso di adottare le direttive ufficiali [Oracle](https://docs.oracle.com/javase/tutorial/java/package/namingpkgs.html), che suggeriscono di utilizzare il nome di domini in proprio possesso per evitare collisioni di nomi, solamente al contrario. `filippocosta.net` è il mio dominio Internet personale.

## 1.3 Interpretazione delle specifiche originali

Il documento originale di specifica del programma è volutamente scarno e poco dettagliato. Si impongono alcuni vincoli sull'implementazione del sistema su certi aspetti ma la maggior parte delle decisioni vengono lasciate dallo studente, tanto per cui nemmeno il funzionamento base delle funzionalità previste da MicroBlog sono chiare. Mi sono confrontato con diversi studenti -sia del Corso A che del Corso B- circa l'interpretazione del testo e gli approcci possibili sono davvero tanti. In particolare, diverse questioni non sono chiarissime:

- Utilizzo o meno di interfacce per le classi `Post` e `SocialNetwork`.
- Meccanismi di "following", "menzioni", e "like".
- Invarianti previste dai metodi assegnati.

Alcuni dubbi sono stati risolti con delle domande opportune a lezione -spesso rivolte sia alla prof.ssa Levi che al prof. Ferrari, ottenendo riscontri diversi- ma mi sono preso la libertà di implementare come ho meglio ritenuto i dettagli non previsti dalle implementazioni. Queste decisioni sono accompagnate da spiegazioni esaustive sia nel presente documento che nel codice sotto forma di commenti, ma rimango disponibile per chiarimenti su ogni aspetto del programma durante l'orale.

## 2.1 Gli utenti

Gli utenti iscritti al sistema sono idenficati univocamente tramite *username* unico. Prendendo ispirazione da molti social network esistenti ho deciso di limitare la scelta di username a combinazioni di caratteri alfanumerici con underscore di lunghezza 3-12 caratteri.

La scelta architetturale di scrivere una classe `User` apposta per gestire gli utenti sarebbe valida. Purtroppo il documento di specifica richiede esplicitamente l'utilizzo di `String`.

## 2.2 I post

Ogni utente iscritto a MicroBlog può pubblicare dei contenuti sotto forma di post (classe `Post`). La visibilità di un post può essere modificata durante la creazione dello stesso, limitando perciò il numero di persone che potranno visualizzarlo e interagirci. Esistono tre (3) impostazioni di visibilità:

- Segreto. Il post è accessibile solo all'autore stesso. I post segreti sono utili come note personali e promemoria.
- Protetto. Il post è accessibile solo alle conoscenze ristrette.
- Pubblico (predefinito). Il post è accessibile a tutti.

I post sono identificati da un `java.util.UUID`. Le UUID (Unique Uniform IDentifiers) sono valide alternative alle ID autogenerate incrementalmente e estinguono il rischio di collisione aumentando di molto lo spazio (128 bits contro 32 bits). Ho deciso di adottare le UUID per semplificare la logica del costruttore `Post`.

## 2.3 Seguire un utente

In quanto social network, MicroBlog permette dei meccanismi di interazione tra gli utenti iscritti al sistema. Il modo principale per interagire con un utente è quello di *seguirlo*. Seguire un utente non è un meccanismo simmetrico: infatti è possibile seguire un utente senza che questo segua a sua volta. In astratto, possiamo modellare le interazioni sociali su MicroBlog con un grafo orientato.