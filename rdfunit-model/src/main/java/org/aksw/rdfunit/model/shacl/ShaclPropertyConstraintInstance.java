package org.aksw.rdfunit.model.shacl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import org.aksw.rdfunit.enums.RLOGLevel;
import org.aksw.rdfunit.enums.TestAppliesTo;
import org.aksw.rdfunit.enums.TestGenerationType;
import org.aksw.rdfunit.model.helper.PropertyValuePair;
import org.aksw.rdfunit.model.impl.ManualTestCaseImpl;
import org.aksw.rdfunit.model.impl.ResultAnnotationImpl;
import org.aksw.rdfunit.model.interfaces.*;
import org.aksw.rdfunit.utils.JenaUtils;
import org.aksw.rdfunit.vocabulary.SHACL;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

import java.util.*;

/**
 * Hacky code for now
 *
 * @author Dimitris Kontokostas
 * @since 14/2/2016 11:20 μμ
 */
@Builder
public class ShaclPropertyConstraintInstance implements PropertyConstraint{
    @NonNull @Getter private final ShaclPropertyConstraintTemplate template;
    @NonNull @Getter @Singular private final ImmutableMap<Argument, RDFNode> bindings;

    @Override
    public Property getFacetProperty() {
        return template.getArguments().stream().findFirst().get().getPredicate();
    }

    @Override
    public Set<RDFNode> getFacetValues() {
       Argument argument =  bindings.keySet().stream().filter(arg -> arg.getPredicate().equals(getFacetProperty())).findFirst().get();
        return new HashSet<>(Arrays.asList(bindings.get(argument)));
    }

    @Override
    public Set<PropertyValuePair> getAdditionalArguments() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public TestCase getTestCase() {

        ManualTestCaseImpl.ManualTestCaseImplBuilder testBuilder = ManualTestCaseImpl.builder();

        return testBuilder
                .element(createTestCaseResource())
                .sparqlPrevalence("")
                .sparqlWhere(generateSparqlWhere())
                .testCaseAnnotation(generateTestAnnotations())
                .build();
    }

    private String generateSparqlWhere() {
        String finalSparqlSnippet = this.template.getSparqlSnippet();

        finalSparqlSnippet = replaceBindings(finalSparqlSnippet);
        return "{ " + finalSparqlSnippet;
    }

    private String replaceBindings(String finalSparqlSnippet) {
        for (Map.Entry<Argument, RDFNode>  entry:  bindings.entrySet()) {
            finalSparqlSnippet = replaceBinding(finalSparqlSnippet, entry.getKey(), entry.getValue());
        }
        return finalSparqlSnippet;
    }

    private String replaceBinding(String sparql, Argument argument, RDFNode value) {
        return sparql.replace("$"+argument.getPredicate().getLocalName(), formatRdfValue(value));
    }

    private String generateMessage() {
        String finalMessage = this.template.getMessage();

        finalMessage = replaceBindings(finalMessage);
        return finalMessage;
    }

    private String formatRdfValue(RDFNode value) {
            if (value.isResource()) {
                // some vocabularies use spaces in uris
                return "<" + value.asResource().getURI().trim().replace(" ", "") + ">";

            } else {
                return value.asLiteral().getLexicalForm();
            }
    }

    // hack for now
    private TestCaseAnnotation generateTestAnnotations() {
        return new TestCaseAnnotation(
                createTestCaseResource(),
                TestGenerationType.AutoGenerated,
                null,
                TestAppliesTo.Schema, // TODO check
                SHACL.namespace,      // TODO check
                Arrays.asList(bindings.get(CoreArguments.predicate).asResource().getURI()),
                generateMessage(),
                RLOGLevel.resolve(bindings.get(CoreArguments.severity).asResource().getURI()),
                createResultAnnotations()
                );
    }

    List<ResultAnnotation> createResultAnnotations() {
        ImmutableList.Builder<ResultAnnotation> annotations = ImmutableList.builder();
        // add property
        annotations.add(new ResultAnnotationImpl.Builder(ResourceFactory.createResource(), SHACL.predicate)
                .setValue(bindings.get(CoreArguments.predicate)).build());

        List<Property> nonValueArgs = Arrays.asList(CoreArguments.minCount.getPredicate(), CoreArguments.maxCount.getPredicate());
        if (!nonValueArgs.contains(getFacetProperty())) {
            annotations.add(new ResultAnnotationImpl.Builder(ResourceFactory.createResource(), SHACL.object)
                    .setVariableName("value").build());
        }

        return annotations.build();
    }

    private Resource createTestCaseResource() {
        // FIXME temporary solution until we decide how to build stable unique test uris
        return ResourceFactory.createProperty(JenaUtils.getUniqueIri());
    }
}
