package com.crypticbit.f2f.db;

import org.neo4j.graphdb.RelationshipType;

enum RelTypes implements RelationshipType {
	HISTORY, ARRAY, MAP
}