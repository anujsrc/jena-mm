jena-mm
=======

This contribution is an extension to the existing Jena in-memory model that adds the capability of generating very large sized graphs based on a caching algorithm.

The contribution also includes the ability to query large tables from the RDB model based on getting a part of the result at a time into the extended in-memory model based on a caching algorithm.

An implementation for the SDB model provides the ability to query large tables again using the extended in-memory model. 

ARQ has been extended to be able to query this new model.

A unified Jena model has been implemented that transitions from the extended in-memory model to the RDB model when a large number of triples are streamed by a user application.

Ontology models have been extended to support reasoning in this version.
