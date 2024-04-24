package com.spaziocodice.examples.ldfs.repositories;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

public abstract class DataAccessObject {
    @FunctionalInterface
    public interface ThrowsFunction<I, O> {
        O apply(I i) throws Exception;
    }

    protected final Model localFactory = ModelFactory.createDefaultModel();

    @Autowired
    protected DataSource datasource;

    public abstract List<Statement> findBy(Model factory, Optional<Integer> id, Optional<String> predicate, Optional<String> value, Optional<Integer> pageNumber);
}
