package com.crypticbit.f2f.db.neo4j.types;

import org.neo4j.graphdb.RelationshipType;

public enum RelationshipTypes implements RelationshipType {
    ARRAY, MAP, INCOMING_VERSION, HISTORY
}