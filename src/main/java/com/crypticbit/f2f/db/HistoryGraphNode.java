package com.crypticbit.f2f.db;

import java.util.List;

public interface HistoryGraphNode extends GraphNode {

    public List<History> getHistory();
    public long getTimestamp();
    
}
