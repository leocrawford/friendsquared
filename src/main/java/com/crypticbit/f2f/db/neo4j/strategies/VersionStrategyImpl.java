package com.crypticbit.f2f.db.neo4j.strategies;

public abstract class VersionStrategyImpl implements VersionStrategy {

    private VersionStrategyImpl successor;
    private VersionStrategyImpl root;


    public VersionStrategyImpl(VersionStrategyImpl successor) {
	this.successor = successor;
    }

    public VersionStrategyImpl getSuccessor() {
	return successor;
    }


    public VersionStrategyImpl getRoot() {
	return root;
    }

    public void setRoot(VersionStrategyImpl root) {
	this.root = root;
    }
    
}