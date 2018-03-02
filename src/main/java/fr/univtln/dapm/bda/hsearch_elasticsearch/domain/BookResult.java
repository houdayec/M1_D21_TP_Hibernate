package fr.univtln.dapm.bda.hsearch_elasticsearch.domain;

import java.io.Serializable;

/**
 * Wrapper contenant un livre et son score.
 * 
 * @author vincent
 *
 */
public class BookResult implements Serializable {
	private static final long serialVersionUID = 1L;

	private Book book;
	private float score;

	public BookResult(Book book, float score) {
		super();
		this.book = book;
		this.score = score;
	}

	public Book getBook() {
		return book;
	}

	public void setBook(Book book) {
		this.book = book;
	}

	public float getScore() {
		return score;
	}

	public void setScore(float score) {
		this.score = score;
	}

	@Override
	public String toString() {
		return "BookResult [book=" + book + ", score=" + score + "]";
	}

}
