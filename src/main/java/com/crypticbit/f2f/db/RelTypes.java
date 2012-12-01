package com.crypticbit.f2f.db;

import org.neo4j.graphdb.RelationshipType;

public enum RelTypes implements RelationshipType {
    ARRAY, MAP, INCOMING_VERSION
}