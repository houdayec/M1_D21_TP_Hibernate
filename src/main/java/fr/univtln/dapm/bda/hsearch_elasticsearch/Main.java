package fr.univtln.dapm.bda.hsearch_elasticsearch;

import java.io.IOException;

import fr.univtln.dapm.bda.hsearch_elasticsearch.domain.Author;
import fr.univtln.dapm.bda.hsearch_elasticsearch.domain.Book;
import fr.univtln.dapm.bda.hsearch_elasticsearch.domain.Category;
import fr.univtln.dapm.bda.hsearch_elasticsearch.search.IndexSearchApi;

import static com.sun.javafx.scene.control.skin.Utils.getResource;

public class Main {
	public static void main(String[] args) throws IOException {

		// Instanciation de notre classe IndexSearchApi pour indexer et rechercher
		IndexSearchApi api = new IndexSearchApi();

		// Réindexation à chaque nouvel appel de la classe Main (à commenter si besoin).
		api.purgeIndex();
		api.indexFilesInFolder("/etudiants/choudayer630/Téléchargements/hsearch-elasticsearch/src/main/resources/data/raw");

		// Recherche.
		System.out.println("--------------------- searchInTitle --------------------");

		System.out.println(api.searchInTitle("Allan Quatermain 711.txt"));

		System.out.println("--------------------- searchKeyword --------------------");

		System.out.println(api.searchKeywords("THE WITCH'S HEAD"));

		System.out.println("---------------------- searchSentence -------------------");

		System.out.println(api.searchSentence("Produced by John Bickers and Dagny"));

		System.out.println("--------------------- searchInTitleAndContent --------------------");

		System.out.println(api.searchInTitleAndContent("Nada the Lily 1207.txt", "John Bickers"));

		System.out.println("--------------------- Entities part --------------------");

		System.out.println(" NO DATA SET UP SO IT WILL NOT RETURN ANY DATA -> TAKE A LOOK TO THE CODE");

		System.out.println("--------------------- searchByAuthor --------------------");

		System.out.println(api.searchByAuthor("This is the content", "This is the author"));
 
		System.out.println("--------------------- searchByCategory --------------------");

		System.out.println(api.searchByCategory("This is the content", "This is the category"));

		Author author1 = new Author("Corentin","Houdayer","IT student and writer");
		Author author2 = new Author("Joanne", "Rowling", "Writer of Harry Potter");

		Category category1 = new Category("Magic", "This category is for magics universes");
		Category category2 = new Category("Educational", "This category will bring you knowledge");

		Book book1 = new Book("How to code ?", "This book will help you to code apps properly", author1, category1);
		Book book2 = new Book("Harry Potter", "Enter in the magic world of wizards !", author2, category2);

		System.exit(0);
	}
}
