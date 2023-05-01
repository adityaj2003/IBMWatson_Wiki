package edu.arizona.cs;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.apache.lucene.document.Document;


public class TestQ1_2_b {
    @Test
    public void testDocsAndScores() {
        String inputFileFullPath = "input.txt";
        QueryEngine objQueryEngine = new QueryEngine(inputFileFullPath);
        try {
            String[] common_query = {"information", "retrieval"};
            List<ResultClass> ans1_2_b = objQueryEngine.runQ1_2_b(common_query);
            if(ans1_2_b !=null) {
                assertEquals(ans1_2_b.size(), 0);
            }
        } catch (java.io.FileNotFoundException ex) {
            System.out.println(ex.getMessage());
        } catch (java.io.IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
}



