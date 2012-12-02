package com.crypticbit.f2f.db.neo4j.strategies;

public abstract class VersionStrategyImpl implements VersionStrategy {

    private VersionStrategy parent;

    public VersionStrategyImpl(VersionStrategy parent) {
	this.parent = parent;
    }

    public VersionStrategy getParent() {
	return parent;
    }

}