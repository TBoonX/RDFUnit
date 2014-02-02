package org.aksw.databugger.Utils;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import org.aksw.databugger.tests.results.ResultAnnotation;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Dimitris Kontokostas
 * Description
 * Created: 1/24/14 6:08 PM
 */
public class SparqlUtils {
    static public List<ResultAnnotation> getResultAnnotations(QueryExecutionFactory queryFactory, String uri) {
        List<ResultAnnotation> annotations = new ArrayList<ResultAnnotation>();
        String sparql = DatabuggerUtils.getAllPrefixes() +
                " SELECT ?annotationProperty ?annotationValue WHERE {" +
                " <" + uri + "> tddo:resultAnnotation ?annotation . " +
                " ?annotation a tddo:ResultAnnotation ; " +
                "   tddo:annotationProperty ?annotationProperty ; " +
                "   tddo:annotationValue ?annotationValue . } " ;

        QueryExecution qe = null;

        try {
            qe = queryFactory.createQueryExecution(sparql);
            ResultSet results = qe.execSelect();

            while (results.hasNext()) {
                QuerySolution qs = results.next();
                String annotationProperty = qs.get("annotationProperty").toString();
                RDFNode annotationValue = qs.get("annotationValue");
                annotations.add(new ResultAnnotation(annotationProperty, annotationValue));
            }

        } catch (Exception e) {

        } finally {
            if (qe != null)
                qe.close();
        }

        return annotations;
    }
}
