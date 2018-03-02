package fr.univtln.dapm.bda.hsearch_elasticsearch.search;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.engine.ProjectionConstants;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;

import fr.univtln.dapm.bda.hsearch_elasticsearch.domain.Book;
import fr.univtln.dapm.bda.hsearch_elasticsearch.domain.BookResult;

/**
 * API pour l'indexation et la recherche de documents (des livres ici).
 * 
 * @author vincent
 *
 */
public class IndexSearchApi {
	private EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("bda");
	private EntityManager entityManager = entityManagerFactory.createEntityManager();
	private FullTextEntityManager fullTextSession = Search.getFullTextEntityManager(entityManager);

	public void purgeIndex() {
		fullTextSession.purgeAll(Book.class);
	}

	public boolean indexFilesInFolder(String folderPath) throws IOException {
		entityManager.getTransaction().begin();
		Files.list(Paths.get(folderPath)).filter(Files::isRegularFile).forEach(t -> {
			try {
				indexFile(t);
			} catch (IOException e) {
				System.err.println("Cannot process " + t.toString());
			}
		});
		entityManager.getTransaction().commit();
		return true;
	}

	public boolean indexFile(Path path) throws IOException {
		String fileName = path.getFileName().toString();
		String fileContent = new String(Files.readAllBytes(path));

		Book book = new Book();
		book.setTitle(fileName);
		book.setContent(fileContent);
		entityManager.persist(book); // le livre est automatiquement indexé!
		return true;
	}

	public List<BookResult> searchInTitle(String query) {
		List<BookResult> bookResults = new ArrayList<>();

		entityManager.getTransaction().begin();

		QueryBuilder queryBuilder = fullTextSession.getSearchFactory().buildQueryBuilder().forEntity(Book.class).get();
		org.apache.lucene.search.Query luceneQuery = queryBuilder.simpleQueryString().onFields("title").matching(query)
				.createQuery();
		FullTextQuery fullTextQuery = fullTextSession.createFullTextQuery(luceneQuery);

		// projection du score (requête, document)
		fullTextQuery.setProjection(ProjectionConstants.SCORE, ProjectionConstants.THIS);
		fullTextQuery.setMaxResults(5);
		List<Object[]> results = fullTextQuery.getResultList();

		for (Object[] result : results) {
			float score = (float) result[0];
			Book book = (Book) result[1];
			bookResults.add(new BookResult(book, score));
		}
		entityManager.getTransaction().commit();

		return bookResults;
	}

	public List<BookResult> searchKeywords(String keywords) {
		List<BookResult> bookResults = new ArrayList<>();

		entityManager.getTransaction().begin();

			// Query creation
			QueryBuilder queryBuilderKeywords = fullTextSession.getSearchFactory().buildQueryBuilder().forEntity(Book.class).get();
			org.apache.lucene.search.Query luceneQuery = queryBuilderKeywords.keyword().onField("content").matching(keywords)
					.createQuery();
			FullTextQuery fullTextQuery = fullTextSession.createFullTextQuery(luceneQuery);

			// Score projection
			fullTextQuery.setProjection(ProjectionConstants.SCORE, ProjectionConstants.THIS);
			fullTextQuery.setMaxResults(5);
			List<Object[]> results = fullTextQuery.getResultList();

			// Fetching results
			for (Object[] result : results) {
				try {
					float score = (float) result[0];
					Book book = (Book) result[1];
					bookResults.add(new BookResult(book, score));

				} catch (NullPointerException e) {
					System.out.println("Caught inside searchKeywords. Fields not found.");
					throw e; // rethrowing the exception
				}
			}

		entityManager.getTransaction().commit();

		return bookResults;
	}

	public List<BookResult> searchSentence(String sentence) {
		List<BookResult> bookResults = new ArrayList<>();

		entityManager.getTransaction().begin();

		// Query creation
		QueryBuilder queryBuilderSentence = fullTextSession.getSearchFactory().buildQueryBuilder().forEntity(Book.class).get();
		org.apache.lucene.search.Query luceneQuery = queryBuilderSentence.phrase().onField("content").sentence(sentence)
				.createQuery();
		FullTextQuery fullTextQuery = fullTextSession.createFullTextQuery(luceneQuery);

		// Score projection
		fullTextQuery.setProjection(ProjectionConstants.SCORE, ProjectionConstants.THIS);
		fullTextQuery.setMaxResults(5);
		List<Object[]> results = fullTextQuery.getResultList();

		// Fetching results
		for (Object[] result : results) {
			try {
				float score = (float) result[0];
				Book book = (Book) result[1];
				bookResults.add(new BookResult(book, score));

			} catch (NullPointerException e) {
				System.out.println("Caught inside searchSentence. Fields not found.");
				throw e; // rethrowing the exception
			}
		}

		entityManager.getTransaction().commit();

		return bookResults;
	}

	public List<BookResult> searchInTitleAndContent(String title, String keywords) {
		List<BookResult> bookResults = new ArrayList<>();

		entityManager.getTransaction().begin();

		// Query creation
		QueryBuilder queryBuilder = fullTextSession.getSearchFactory().buildQueryBuilder().forEntity(Book.class).get();

		SearchFactory searchFactory = fullTextSession.getSearchFactory();

		// Query creation
		QueryBuilder queryBuilderSentence = fullTextSession.getSearchFactory().buildQueryBuilder().forEntity(Book.class).get();

		Query query = queryBuilderSentence.bool().must(queryBuilderSentence.phrase().onField("title").sentence(title)
				.createQuery()).should(queryBuilderSentence.keyword().onField("content").matching(keywords).createQuery()).createQuery();


		FullTextQuery fullTextQuery = fullTextSession.createFullTextQuery(query);


		// Score projection
		fullTextQuery.setProjection(ProjectionConstants.SCORE, ProjectionConstants.THIS);
		fullTextQuery.setMaxResults(5);
		List<Object[]> results = fullTextQuery.getResultList();

		// Fetching results
		for (Object[] result : results) {
			try {
				float score = (float) result[0];
				Book book = (Book) result[1];
				bookResults.add(new BookResult(book, score));

			} catch (NullPointerException e) {
				System.out.println("Caught inside searchInTitleAndContent. Fields not found.");
				throw e; // rethrowing the exception
			}
		}

		entityManager.getTransaction().commit();

		return bookResults;
	}

	// When author is unique for a book and included in book table
	public List<BookResult> searchByAuthor(String content, String author) {
		List<BookResult> bookResults = new ArrayList<>();

		entityManager.getTransaction().begin();

		// Query creation

		QueryBuilder queryBuilder = fullTextSession.getSearchFactory().buildQueryBuilder().forEntity(Book.class).get();

		Query query = queryBuilder.bool().must(queryBuilder.keyword().onField("content").matching(content)
				.createQuery()).must(queryBuilder.keyword().onField("author").matching(author).createQuery()).createQuery();

		FullTextQuery fullTextQuery = fullTextSession.createFullTextQuery(query);

		// projection du score (requête, document)
		fullTextQuery.setProjection(ProjectionConstants.SCORE, ProjectionConstants.THIS);
		fullTextQuery.setMaxResults(5);
		List<Object[]> results = fullTextQuery.getResultList();

		// Iterating though each fetched object

		for (Object[] result : results) {
			float score = (float) result[0];
			Book book = (Book) result[1];
			bookResults.add(new BookResult(book, score));
		}

		entityManager.getTransaction().commit();

		return bookResults;
	}

	// When categeory is unique for a book and included in book table
	public List<BookResult> searchByCategory(String content, String category) {
		List<BookResult> bookResults = new ArrayList<>();

		entityManager.getTransaction().begin();

		// Query creation

		QueryBuilder queryBuilder = fullTextSession.getSearchFactory().buildQueryBuilder().forEntity(Book.class).get();

		Query query = queryBuilder.bool().should(queryBuilder.keyword().onField("content").matching(content)
				.createQuery()).should(queryBuilder.keyword().onField("category").matching(category).createQuery()).createQuery();

		FullTextQuery fullTextQuery = fullTextSession.createFullTextQuery(query);

		// Score projection

		fullTextQuery.setProjection(ProjectionConstants.SCORE, ProjectionConstants.THIS);
		fullTextQuery.setMaxResults(5);
		List<Object[]> results = fullTextQuery.getResultList();

		// Iterating though each fetched object

		for (Object[] result : results) {
			float score = (float) result[0];
			Book book = (Book) result[1];
			bookResults.add(new BookResult(book, score));
		}

		entityManager.getTransaction().commit();

		return bookResults;
	}



}
