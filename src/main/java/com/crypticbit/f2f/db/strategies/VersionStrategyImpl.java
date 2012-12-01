package com.crypticbit.f2f.db.strategies;

public abstract class VersionStrategyImpl implements VersionStrategy {

    private VersionStrategy parent;

    public VersionStrategyImpl(VersionStrategy parent) {
	this.parent = parent;
    }

    public VersionStrategy getParent() {
	return parent;
    }


}