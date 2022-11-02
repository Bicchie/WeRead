package it.unipi.dii.lsdb.weread.model;

import java.util.List;

public class ReadingList {
    private String name;
    private int numLikes;
    private List<Book> books;

    public ReadingList(String name, int numLikes, List<Book> books) {
        this.name = name;
        this.numLikes = numLikes;
        this.books = books;
    }

    public String getName() {
        return name;
    }

    public int getNumLikes() {
        return numLikes;
    }

    public List<Book> getBooks() {
        return books;
    }

}
