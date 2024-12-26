package com.zhd.geometry.structure;

import java.util.Map;

public class Octree {
    private OctreeNode root;
    private int M;

    public  Octree(int M_) {root=new OctreeNode(); M=M_; }
    public void insert(OctreeGrid grid){
        OctreeNode p = root;
        for(int i=1;i<=grid.getK();i++){
            int octreeNumber = grid.getOctreeNumber(i, M);
            OctreeNode q = p.getKthChild(octreeNumber);
            if(q==null) {
                q=new OctreeNode();
                p.setKthChild(octreeNumber,q);
            }
            p=q;
        }
        p.setOccupied(true);
    }

    public boolean has(OctreeGrid grid){
        OctreeNode p = root;
        for(int i=1;i<= grid.getK();i++){
            int octreeNumber = grid.getOctreeNumber(i, M);
            OctreeNode q = p.getKthChild(octreeNumber);
            if(q!=null&&q.isOccupied()) return true;
            if(q==null) return false;
            p=q;
        }
        return false;
    }

    public void insertWithProperties(OctreeGrid grid, Map<String,Object> prop){
        OctreeNode p = root;
        for(int i=1;i<=grid.getK();i++){
            int octreeNumber = grid.getOctreeNumber(i, M);
            OctreeNode q = p.getKthChild(octreeNumber);
            if(q==null) {
                q=new OctreeNode();
                p.setKthChild(octreeNumber,q);
            }
            p=q;
        }
        p.setOccupied(true);
        p.setProperties(prop);
    }

    public Map<String,Object> getWithProperties(OctreeGrid grid){
        OctreeNode p = root;
        for(int i=1;i<= grid.getK();i++){
            int octreeNumber = grid.getOctreeNumber(i, M);
            OctreeNode q = p.getKthChild(octreeNumber);
            if(q!=null&&q.isOccupied()) return q.getProperties();
            if(q==null) return null;
            p=q;
        }
        return null;
    }
}
