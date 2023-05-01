package edu.arizona.cs;

import java.util.List;

import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class TestQ1_1 {

    @Test
    public void testDocsAndScores() {
        String inputFileFullPath="input.txt";
        QueryEngine objQueryEngine = new QueryEngine(inputFileFullPath);
        try {
            String[] common_query = {"information", "retrieval"};
            List<ResultClass> ans1_1= objQueryEngine.runQ1_1(common_query);
            String[] doc_names_q1 = {"Doc1", "Doc2"};
            int counter1 = 0;
            assertEquals(ans1_1.size(),(doc_names_q1.length) ) ;
            for (ResultClass docs : ans1_1) {
                assertEquals(doc_names_q1[counter1], docs.DocName.get("docid"));
                counter1 = counter1 + 1;
            }
        }
        catch ( java.io.FileNotFoundException ex)
        {
            System.out.println(ex.getMessage()); }

        catch ( java.io.IOException ex)
        {
            System.out.println(ex.getMessage()); }

    }


}



