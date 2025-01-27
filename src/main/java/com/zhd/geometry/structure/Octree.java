package com.zhd.geometry.structure;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class Octree {
    private OctreeNode root;
    private int M;

    public  Octree(int M_) {root=new OctreeNode(); M=M_; }
    public int getM(){return M;}
    public OctreeNode getRoot(){return root;}
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
        Map<String, Object> props = p.getProperties();
        if(props==null) p.setProperties(prop);
        else{
            // 合并value
            for (String key : prop.keySet()) {
                if(props.containsKey(key)){
                    Object o = props.get(key);
                    if(o instanceof Integer) props.replace(key,(Integer)o+(Integer)prop.get(key));
                    else if(o instanceof Double) props.replace(key,(Double)o+(Double)prop.get(key));
                    else if(o instanceof List){
                        List list = (List) o;
                        list.addAll((List)prop.get(key));
                        props.replace(key,list);
                    }else if(o instanceof Set){
                        Set s = (Set) o;
                        s.addAll((Set)prop.get(key));
                        props.replace(key,s);
                    }
                    // Long和String视为id 不合并，直接覆盖
                }else props.put(key,prop.get(key));
            }
            p.setProperties(props);
        }
    }

    public Map<String,Object> getWithProperties(OctreeGrid grid){
        OctreeNode p = root;
        for(int i=1;i<= grid.getK();i++){
            int octreeNumber = grid.getOctreeNumber(i, M);
            OctreeNode q = p.getKthChild(octreeNumber);
            if(q==null) return null;
            p=q;
        }
        return p.getProperties();
    }
}
