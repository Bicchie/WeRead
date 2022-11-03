package it.unipi.dii.lsdb.weread.model;

import java.util.ArrayList;
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

    //constructor for new reading lists, with some variables that must be initialized with default values
    public ReadingList(String name){
        this.name = name;
        numLikes = 0;
        books = new ArrayList<>();
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
