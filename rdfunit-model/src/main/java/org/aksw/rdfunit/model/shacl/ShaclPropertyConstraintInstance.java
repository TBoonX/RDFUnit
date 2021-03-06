package org.aksw.rdfunit.model.shacl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.*;
import org.aksw.rdfunit.enums.RLOGLevel;
import org.aksw.rdfunit.enums.TestAppliesTo;
import org.aksw.rdfunit.enums.TestGenerationType;
import org.aksw.rdfunit.model.helper.PropertyValuePair;
import org.aksw.rdfunit.model.impl.ManualTestCaseImpl;
import org.aksw.rdfunit.model.impl.ResultAnnotationImpl;
import org.aksw.rdfunit.model.interfaces.*;
import org.aksw.rdfunit.utils.JenaUtils;
import org.aksw.rdfunit.vocabulary.SHACL;
import org.apache.jena.rdf.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Hacky code for now
 *
 * @author Dimitris Kontokostas
 * @since 14/2/2016 11:20 μμ
 */
@Builder
@ToString
@EqualsAndHashCode
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
        return new HashSet<>(Collections.singletonList(bindings.get(argument)));
    }

    @Override
    public Set<PropertyValuePair> getAdditionalArguments() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public TestCase getTestCase(boolean forInverseProperty) {

        ManualTestCaseImpl.ManualTestCaseImplBuilder testBuilder = ManualTestCaseImpl.builder();
        String sparql = (forInverseProperty) ? generateSparqlWhere(template.getSparqlInvPSnippet()) : generateSparqlWhere(template.getSparqlPropSnippet());

        return testBuilder
                .element(createTestCaseResource())
                .sparqlPrevalence("")
                .sparqlWhere(sparql)
                .testCaseAnnotation(generateTestAnnotations())
                .build();
    }

    @Override
    public boolean usesValidatorFunction() {
        return template.isIncludePropertyFilter();
    }

    private String generateSparqlWhere(String sparqlString) {
        return "{ " + replaceBindings(sparqlString);
    }

    private String replaceBindings(String sparqlSnippet) {
        String bindedSnippet = sparqlSnippet;
        for (Map.Entry<Argument, RDFNode>  entry:  bindings.entrySet()) {
            bindedSnippet = replaceBinding(bindedSnippet, entry.getKey(), entry.getValue());
        }
        return bindedSnippet;
    }

    private String replaceBinding(String sparql, Argument argument, RDFNode value) {
        return sparql.replace("$"+argument.getPredicate().getLocalName(), formatRdfValue(value));
    }

    private String generateMessage() {
        return replaceBindings(this.template.getMessage());
    }

    private String formatRdfValue(RDFNode value) {
            if (value.isResource()) {
                Resource r = value.asResource();
                if (r.isAnon() && r.canAs(RDFList.class)) {
                    return getListItems(r).stream().map(this::formatRdfListValue).collect(Collectors.joining("  "));
                } else {
                    return asFullTurtleUri(r);
                }

            } else {
                return asSimpleLiteral(value.asLiteral());
            }
    }

    private String asSimpleLiteral(Literal value) {
        return value.getLexicalForm();
    }

    private String asFullTurtleLiteral(Literal value) {
        return "\""+value.getLexicalForm()+"\"^^<"+value.getDatatypeURI()+">";
    }

    private String asFullTurtleUri(Resource value) {
        // some vocabularies use spaces in uris
        return "<" + value.getURI().trim().replace(" ", "") + ">";
    }

    private String formatRdfListValue(RDFNode listVal) {
        if (listVal.isResource()) {
            return asFullTurtleUri(listVal.asResource());
        } else {
            return asFullTurtleLiteral(listVal.asLiteral());
        }
    }

    private List<RDFNode> getListItems(Resource list) {
        ImmutableList.Builder<RDFNode> nodes = ImmutableList.builder();
        try {
            RDFList rdfList = list.as( RDFList.class );
            rdfList.iterator().forEachRemaining(nodes::add);
        } catch (Exception e) {
            throw new IllegalArgumentException("Argument not a list", e);
        }

        return nodes.build();
    }

    // hack for now
    private TestCaseAnnotation generateTestAnnotations() {
        return new TestCaseAnnotation(
                createTestCaseResource(),
                TestGenerationType.AutoGenerated,
                null,
                TestAppliesTo.Schema, // TODO check
                SHACL.namespace,      // TODO check
                Collections.singletonList(bindings.get(CoreArguments.predicate).asResource().getURI()),
                generateMessage(),
                RLOGLevel.resolve(bindings.get(CoreArguments.severity).asResource().getURI()),
                createResultAnnotations()
                );
    }

    private List<ResultAnnotation> createResultAnnotations() {
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
