package edu.arizona.cs;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;



public class TestQ1_2_a {

    @Test
    public void testDocsAndScores() {

        String inputFileFullPath="input.txt";

        QueryEngine objQueryEngine = new QueryEngine(inputFileFullPath);
        try {

            String[] common_query = {"information", "retrieval"};

            List<ResultClass> ans1_2_a = objQueryEngine.runQ1_2_a(common_query);
            String[] doc_names_q13a = {"Doc1", "Doc2"};
            assertEquals((doc_names_q13a.length) , ans1_2_a.size());
            int counter2 = 0;
            for (ResultClass docs : ans1_2_a) {

                assertEquals(doc_names_q13a[counter2], docs.DocName.get("docid"));
                counter2 = counter2 + 1;
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



