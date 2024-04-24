package com.spaziocodice.examples.ldfs.repositories;

import com.spaziocodice.examples.ldfs.Prefix;
import com.spaziocodice.examples.ldfs.Utility;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.ORG;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.spaziocodice.examples.ldfs.Prefix.ORGANIZATIONS;
import static java.util.Collections.singletonList;
import static java.util.Map.entry;
import static java.util.stream.Collectors.toMap;

@Component
public class PersonDataAccessObject extends DataAccessObject {

    private final Map<String, Collection<String>> predicateToColumnMapping =
            Map.ofEntries(entry(FOAF.member.getURI(), singletonList("contact.organization_id")),
                        entry(FOAF.firstName.getURI(), singletonList("contact.name")),
                            entry(FOAF.familyName.getURI(), singletonList("contact.surname")),
                            entry(FOAF.mbox.getURI(), singletonList("contact.email")),
                            entry(FOAF.phone.getURI(), List.of("contact.mobile", "contact.phone")),
                            entry(ORG.role.getURI(), singletonList("role_name")));

    private final Map<String, String> columnToPredicateMapping =
            predicateToColumnMapping
                .entrySet()
                .stream()
                .flatMap(entry -> entry.getValue().stream().map(columnName -> Map.entry(columnName, entry.getKey())))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

    private final Map<String, ThrowsFunction<ResultSet, List<RDFNode>>> mappers =
            Map.ofEntries(
                    entry(FOAF.member.getURI(), rs -> singletonList(localFactory.createResource(ORGANIZATIONS + rs.getString("organization_id")))),
                    entry(FOAF.firstName.getURI(), rs -> singletonList(localFactory.createLiteral(rs.getString("name")))),
                    entry(FOAF.familyName.getURI(), rs -> singletonList(localFactory.createLiteral(rs.getString("surname")))),
                    entry(FOAF.mbox.getURI(), rs -> singletonList(localFactory.createResource("mailto:" + rs.getString("email")))),
                    entry(ORG.role.getURI(), rs -> singletonList(localFactory.createLiteral(rs.getString("role_name")))),
                    entry(FOAF.phone.getURI(), rs -> List.of(localFactory.createResource("tel:" + rs.getString("mobile")), localFactory.createResource("tel:" + rs.getString("phone")))));

    public List<org.apache.jena.rdf.model.Statement> findBy(Model factory, Optional<Integer> id, Optional<String> predicate, Optional<String> value, Optional<Integer> pageNumber) {
        var targetColumns =
                predicate.map(predicateToColumnMapping::get)
                         .orElseGet(columnToPredicateMapping::keySet);

        var constraintValue = value.map(Utility::withoutMailScheme).map(v -> "'" + v + "'");

        var whereClause =
                " contact.role_id = role.id " +
                        (constraintValue.isPresent() ? "AND " : "") +
                        constraintValue.map(v -> targetColumns.stream().map(columnName -> columnName + " = " + v).collect(Collectors.joining(" OR ", "(", ")"))).orElse("") +
                        id.map(identifier -> " AND contact.id = " + identifier).orElse("");

        var offset = pageNumber.map(v -> v * 10);

        var sql = "SELECT contact.*, role.name as role_name FROM contact, role WHERE "
                    + whereClause
                    + offset.map(v -> " OFFSET " + v).orElse("");

        try (var connection = datasource.getConnection();
             var statement = connection.prepareStatement(sql);
             var resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                var subject = factory.createResource(Prefix.PEOPLE + resultSet.getString("id"));
                factory.add(predicate.map(mappers::get)
                                     .map(fn -> { try { return fn.apply(resultSet); } catch (Exception exception) { throw new RuntimeException(exception);}})
                                     .map(nodes -> nodes.stream().map(node -> factory.createStatement(subject, factory.createProperty(predicate.get()), node)).toList())
                                     .orElseGet( () -> mappers.entrySet()
                                                            .stream()
                                                            .flatMap(entry -> {
                                                                var fn = entry.getValue();
                                                                try {
                                                                    var nodes = fn.apply(resultSet);
                                                                    return nodes.stream().map(node -> factory.createStatement(subject, factory.createProperty(entry.getKey()), node));
                                                                } catch (Exception exception) {
                                                                    throw new RuntimeException(exception);
                                                                }})
                                                            .peek(System.out::println)
                                                            .map(Statement.class::cast)
                                                            .toList()));
            }
            return factory.listStatements().toList();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}
