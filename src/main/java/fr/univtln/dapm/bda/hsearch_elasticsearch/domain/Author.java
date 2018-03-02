package fr.univtln.dapm.bda.hsearch_elasticsearch.domain;

import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.StopFilterFactory;
import org.apache.lucene.analysis.ngram.EdgeNGramFilterFactory;
import org.apache.lucene.analysis.snowball.SnowballPorterFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.hibernate.search.annotations.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Entité indexable représentant un auteur.
 *
 * @author Corentin Houdayer
 *
 */
@Entity
@Indexed
public class Author implements Serializable{

    @Id
    @GeneratedValue
    private int id;
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String firstname;
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String lastname;
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String biography;

    public Author(){

    }

    public Author(String firstname, String lastname, String biography) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.biography = biography;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    @Override
    public String toString() {
        return "Author{" +
                "id=" + id +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", biography='" + biography + '\'' +
                '}';
    }
}
