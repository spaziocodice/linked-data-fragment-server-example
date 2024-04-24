package com.spaziocodice.examples.ldfs.resolvers;

import com.spaziocodice.examples.ldfs.Utility;
import com.spaziocodice.examples.ldfs.repositories.OrganizationDataAccessObject;
import com.spaziocodice.examples.ldfs.repositories.PersonDataAccessObject;
import com.spaziocodice.labs.fraglink.Identifiers;
import com.spaziocodice.labs.fraglink.domain.TriplePatternResponse;
import com.spaziocodice.labs.fraglink.service.impl.TriplePatternResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.jena.rdf.model.ModelFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.spaziocodice.examples.ldfs.Prefix.ORGANIZATIONS;
import static com.spaziocodice.examples.ldfs.Prefix.PEOPLE;
import static java.util.Optional.of;

@Component(Identifiers.TRIPLE_PATTERN_RESOLVER)
public class MyTriplePatternResolver implements TriplePatternResolver {

    @Autowired
    private PersonDataAccessObject personDataAccessObject;

    @Autowired
    private OrganizationDataAccessObject organizationDataAccessObject;

    @Override
    public TriplePatternResponse linkedDataFragment(
            Optional<String> s,
            Optional<String> p,
            Optional<String> o,
            Optional<String> g,
            Optional<Integer> pageNumber,
            HttpServletRequest request) {

        var factory = ModelFactory.createDefaultModel();
        var id = s.map(Utility::localIdentifierFrom).map(Integer::parseInt);

        if (s.isEmpty() || s.filter(v -> v.startsWith(ORGANIZATIONS)).isPresent()) {
            factory.add(organizationDataAccessObject.findBy(factory, id, p, o, pageNumber));
        }

        if (s.isEmpty() || s.filter(v -> v.startsWith(PEOPLE)).isPresent()) {
            factory.add(personDataAccessObject.findBy(factory, id, p, o, pageNumber));
        }

        var fragment = factory.listStatements().toList();
        return TriplePatternResponse.builder()
                .fragmentCardinality(of(fragment).map(java.util.Collection::size)
                                                 .map(Number::longValue)
                                                 .orElse(0L))
                .matches(fragment)
                .build();
    }
}
