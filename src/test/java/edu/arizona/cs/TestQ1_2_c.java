package edu.arizona.cs;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestQ1_2_c {

    @Test
    public void testDocsAndScores() {

        String inputFileFullPath="input.txt";

        QueryEngine objQueryEngine = new QueryEngine(inputFileFullPath);
        try {
            String[] common_query = {"information", "retrieval"};
            List<ResultClass> ans1_2_c = objQueryEngine.runQ1_2_c(common_query);
            String[] doc_names_q1_2_c = {"Doc1"};
            int counter3 = 0;
            assertEquals(1, ans1_2_c.size());
            for (ResultClass docs : ans1_2_c) {
                assertEquals(doc_names_q1_2_c[counter3], docs.DocName.get("docid"));
                counter3 = counter3 + 1;
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



