package edu.arizona.cs;


import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.core.StopFilter;



import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class QueryEngine {
    static boolean indexExists=false;
    static List<documentEntry> docStrings;
    static Directory index;
    static int errors = 0;
    static Properties props = new Properties();
    static StanfordCoreNLP pipeline;
    static CharArraySet customStopWords = EnglishAnalyzer.getDefaultStopSet();

    static CharArraySet stopWords =   new CharArraySet(customStopWords, true);

    public static void main(String[] args ) {
    	 props.setProperty("annotators", "tokenize,ssplit,pos,lemma");
    	 pipeline = new StanfordCoreNLP(props);
    	
        docStrings = new ArrayList<documentEntry>();   
        index = new ByteBuffersDirectory();
        String directoryPath = "src/main/resources/wiki-subset-20140602";

        try {
            Path dirPath = Paths.get(directoryPath);
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath, "*.txt")) {
                for (Path filePath : stream) {
                    // Print the file path
                    parseFile(filePath.toString());
                    writeToIndex();
                }
            }
            int score = 0;
            List<JeopardyQuestion> questions = getQuestions();
            for (JeopardyQuestion question : questions) {
            	String returnVal = searchQuery(question.category, question.clue);
            	System.out.println(returnVal);
            	System.out.println(question.answer);
            	if (question.answer.toLowerCase().contains(returnVal.toLowerCase())) {
            		
            		score+=1;
            	}
            }
          System.out.println("Total Score: "+score);
          System.out.println("Errors: "+errors);
          
            index.close();
            
            
            
            
            
            
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
    
    public static void parseFile(String filePath) throws IOException {
        docStrings.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            String title = null;
            StringBuilder intro = new StringBuilder();
            String categories = "";
            Pattern titlePattern = Pattern.compile("\\[\\[(.*?)\\]\\]");
            Pattern categoryPattern = Pattern.compile("CATEGORIES:(.*?)$");

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("[[")) {
                    Matcher matcher = titlePattern.matcher(line);
                    if (matcher.find()) {
                        if (title != null) {
                            docStrings.add(new documentEntry(title, intro.toString(), categories));
                            intro = new StringBuilder();
                            categories = "";
                        }
                        title = matcher.group(1);
                    }
                } else if (line.startsWith("CATEGORIES:")) {
                    Matcher matcher = categoryPattern.matcher(line);
                    if (matcher.find()) {
                        categories = matcher.group(1);
                    }
                } else if (title != null && !line.isEmpty()) {
                    intro.append(line).append(" ");
                }
            }

            // Add the last article
            if (title != null && intro.length() > 0) {
                docStrings.add(new documentEntry(title, intro.toString(), categories));
            }
        }
    }
    
    public static class CustomPorterStemmingAnalyzer extends Analyzer {
        @Override
        public TokenStreamComponents createComponents(String fieldName) {
        	Tokenizer source = new StandardTokenizer();
            TokenStream filter = new LowerCaseFilter(source);
            filter = new StopFilter(filter, stopWords);
            filter = new PorterStemFilter(filter);
            return new TokenStreamComponents(source, filter);
        }
    }

    public static List<String> lemmatize(String input) {
        List<String> lemmas = new ArrayList<>();


        CoreDocument document = new CoreDocument(input);

        List<CoreLabel> tokens = pipeline.processToCoreDocument(input).tokens();
        for (CoreLabel token : tokens) {
            lemmas.add(token.lemma());
        }

        return lemmas;
    }
    
    public static void writeToIndex() throws java.io.FileNotFoundException,java.io.IOException {
        Analyzer analyzer = new CustomPorterStemmingAnalyzer();

        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        IndexWriter w = new IndexWriter(index, config);
        for (documentEntry doc : docStrings) {
        	Document luceneDoc = new Document();
            luceneDoc.add(new StringField("title", doc.title, Field.Store.YES));

//            List<String> lemmatizedIntro = lemmatize(doc.intro);
//            String lemmatizedIntroText = String.join(" ", lemmatizedIntro);
            luceneDoc.add(new TextField("intro", doc.intro , Field.Store.NO));

            luceneDoc.add(new TextField("categories", doc.categories, Field.Store.NO));

            w.addDocument(luceneDoc);
        }
        w.commit();
        w.close();
        
        
    }
    
    public static String searchQuery(String category, String clue) throws IOException {
    	try {
    		Analyzer analyzer = new CustomPorterStemmingAnalyzer();

    		// Combine clue and category into a single query string
    		String combinedQueryString = "intro:(" + clue + ") categories:(\"" + category + "\")";

    		// Create a single QueryParser instance and parse the combined query string
    		QueryParser parser = new QueryParser("", analyzer);
    		Query combinedQuery = parser.parse(combinedQueryString);
		
        int hitsPerPage = 1;
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
//        searcher.setSimilarity(new ClassicSimilarity());
        TopDocs docs = searcher.search(combinedQuery, hitsPerPage);
        
        	
        ScoreDoc[] hits = docs.scoreDocs;
        if (hits.length > 0) {
            int docId = hits[0].doc;
            
            Document d = searcher.doc(docId);
            reader.close();
            return d.get("title");
        }
        reader.close();
        } catch (Exception e) {
    		
    	}
        return "lol";
    }
    
    public static List<JeopardyQuestion> getQuestions() {
    	List<JeopardyQuestion> questions = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/questions.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String category = line;
                String clue = reader.readLine();
                String answer = reader.readLine();
                category = category.strip();
                clue = clue.strip();
                answer = answer.strip();
                clue = clue.replaceAll("[!\\*+&|()\\[\\]{}^~?:\"/]", "");
                reader.readLine(); // Read the newline
                questions.add(new JeopardyQuestion(category, clue, answer));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return questions;
    }
    
    
    public static class documentEntry {
        private String title;
        private String intro;
        private String categories;

        public documentEntry(String title, String intro, String categories) {
            this.title = title;
            this.intro = intro;
            this.categories = categories;
        }
    }
    
    public static class JeopardyQuestion {
        String category;
        String clue;
        String answer;

        public JeopardyQuestion(String category, String clue, String answer) {
            this.category = category;
            this.clue = clue;
            this.answer = answer;
        }
    }

}
