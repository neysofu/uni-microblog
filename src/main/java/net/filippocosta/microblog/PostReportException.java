package net.filippocosta.microblog;

// OVERVIEW:
//   Eccezione sollevata quando si cerca di segnalare un post non esistente o con
//   parametri errati.
public class PostReportException extends Exception {
    public PostReportException() {
        super();
    }

    public PostReportException(String s) {
        super(s);
    }
}
