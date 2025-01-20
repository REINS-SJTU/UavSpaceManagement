package com.zhd.geometry.structure;

import lombok.Data;

import java.util.Map;

@Data
public class OctreeNode {
    private OctreeNode[] children;
    private int degree;
    private boolean isOccupied;
    private Map<String,Object> properties;

    public OctreeNode(){
        children = new OctreeNode[8];
        degree=0;
        isOccupied=false;
    }

    public OctreeNode getKthChild(int k){
        return (children==null||k>=8||k<0)?null: children[k];
    }

    public void setKthChild(int k,OctreeNode child){
        if(k>=8||k<0) return ;
        if(children[k]==null) degree++;
        children[k]=child;
    }

    public boolean isLeaf(){
        return degree<=0;
    }

}
