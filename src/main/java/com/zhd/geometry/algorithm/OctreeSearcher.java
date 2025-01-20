package com.zhd.geometry.algorithm;

import com.zhd.entity.tmp.ConflictZonePairs;
import com.zhd.entity.tmp.UavPosShape;
import com.zhd.entity.tmp.Zone;
import com.zhd.geometry.structure.Octree;
import com.zhd.geometry.structure.OctreeGrid;
import com.zhd.geometry.structure.OctreeNode;
import com.zhd.geometry.structure.Point3D;

import java.util.*;

public class OctreeSearcher {
    private OctreeNode root;
    private int M;
    public OctreeSearcher(int M){
        root=new OctreeNode();
        this.M=M;
    }

    public List<ConflictZonePairs> checkLargeLargeConflict(Map<String, Object> props,String uavId,Point3D[] zone0){
        System.out.println("===== check large large conflict ====");
        List<ConflictZonePairs> conflictZones=new ArrayList<>();
        if(props!=null){
            Object o1 = props.get("zids");
            Object o2 = props.get("zones");
            System.out.println(o1+","+o2);
            if(o1!=null&&o2!=null){
                List<String> zids = (List<String>) o1;
                List<Point3D[]> zones = (List<Point3D[]>) o2;
                for(int j=0;j<zids.size();j++){
                    if(Objects.equals(uavId, zids.get(j))) continue;
                    Point3D[] zone1 = zones.get(j);
                    Point3D[] conflict = CollisionDecider.getBlocksConflict(zone0, zone1);
                    System.out.println(uavId+":"+Arrays.toString(zone0));
                    System.out.println(zids.get(j)+":"+Arrays.toString(zone1));
                    System.out.println(Arrays.toString(conflict));
                    if(conflict!=null) {
                        ConflictZonePairs conflictZonePairs = new ConflictZonePairs(uavId, zids.get(j), conflict,3);
                        conflictZones.add(conflictZonePairs);
                    }
                }
            }
        }
        return conflictZones;
    }

    public List<ConflictZonePairs> checkSmallLargeConflict(Map<String, Object> props,String uavId,Point3D[] zone0){
        List<ConflictZonePairs> conflictZones=new ArrayList<>();
        if(props!=null){
            Object o1 = props.get("ids");
            Object o2 = props.get("blocks");
            if(o1!=null&&o2!=null){
                List<String> ids = (List<String>) o1;
                List<Point3D[]> blocks = (List<Point3D[]>) o2;
                for(int j=0;j<ids.size();j++){
                    if(Objects.equals(uavId, ids.get(j))) continue;
                    Point3D[] block1 = blocks.get(j);
                    Point3D[] conflict = CollisionDecider.getBlocksConflict(zone0, block1);
                    if(conflict!=null) {
                        ConflictZonePairs conflictZonePairs = new ConflictZonePairs(uavId, ids.get(j), conflict,2);
                        conflictZones.add(conflictZonePairs);
                    }
                }
            }
        }
        return conflictZones;
    }

    // 插入一个Zone(有冲突版的R_max)
    // 返回插入该Zone时会发生的冲突
    public List<ConflictZonePairs> insertZone(String uavId, Zone zone){
        System.out.println("insert zone:"+uavId+",  "+zone);
        Point3D pMax = new Point3D(Math.max(zone.getX()[0],zone.getX()[1]),
                Math.max(zone.getY()[0],zone.getY()[1]),Math.max(zone.getZ()[0],zone.getZ()[1]));
        Point3D pMin = new Point3D(Math.min(zone.getX()[0],zone.getX()[1]),
                Math.min(zone.getY()[0],zone.getY()[1]),Math.min(zone.getZ()[0],zone.getZ()[1]));
        Point3D pc = new Point3D((zone.getX()[0]+zone.getX()[1])/2,(zone.getY()[0]+zone.getY()[1])/2,
                (zone.getZ()[0]+zone.getZ()[1])/2);
        Point3D[] zone0=new Point3D[]{pc, pMin, pMax};
        OctreeGrid tau = OctreeGrid.tau(zone0, M);
        System.out.println(tau);
        OctreeNode p = root;
        List<ConflictZonePairs> conflictZones = new ArrayList<>();
        // 祖辈冲突检测
        for(int i=0;i<=tau.getK();i++){
            if(p!=null){
                Map<String, Object> propsP = p.getProperties();
                conflictZones.addAll(checkLargeLargeConflict(propsP,uavId,zone0));
                conflictZones.addAll(checkSmallLargeConflict(propsP,uavId,zone0));
            }
            int octreeNumber = tau.getOctreeNumber(i, M);
            OctreeNode q = p.getKthChild(octreeNumber);
            if(q==null) {
                q=new OctreeNode();
                p.setKthChild(octreeNumber,q);
            }
            p=q;
        }

        // 插入当前块
        Map<String, Object> props = p.getProperties();
        if(props==null) props=new HashMap<>();
        List<String> zids_ = new ArrayList<>();
        List<Point3D[]> zones_ = new ArrayList<>();
        if(props.containsKey("zids")&&props.containsKey("zones")){
            zids_=(List<String>)props.get("zids");
            zids_.add(uavId);
            zones_=(List<Point3D[]>)props.get("zones");
            zones_.add(zone0);
        }else {
            zids_.add(uavId);
            zones_.add(zone0);
        }
        props.put("zids",zids_);
        props.put("zones",zones_);
        p.setProperties(props);
        System.out.println(p.getProperties());;

        // 子辈冲突检测
        LinkedList<OctreeNode> Q = new LinkedList<>();
        Q.add(p);
        while(!Q.isEmpty()){
            OctreeNode q = Q.removeFirst();
            if(q==null) continue;
            System.out.println(q);
            Map<String, Object> propsQ = q.getProperties();
            conflictZones.addAll(checkLargeLargeConflict(propsQ,uavId,zone0));
            conflictZones.addAll(checkSmallLargeConflict(propsQ,uavId,zone0));
            for(int j=0;j<8;j++){
                OctreeNode child = q.getKthChild(j);
                if(child!=null) Q.addLast(child);
            }
        }
        return conflictZones;
    }



    // 插入一个Block(Rmin)
    public void insertUav(String uavId,Point3D[] block){
        OctreeGrid tau = OctreeGrid.tau(block,M);
        OctreeNode p = root;
        for(int i=1;i<=tau.getK();i++){

            if(p!=null){
                Map<String, Object> propsP = p.getProperties();
                if(propsP!=null){
                    Object o = propsP.get("count");
                    propsP.replace("count",o==null?1:(int)o+1);
                    Object o1 = propsP.get("ids");
                    if(o1!=null){
                        List<String> ids =(List<String>)o1;
                        List<Point3D[]> blocks=(List<Point3D[]>) propsP.get("blocks");
                        for(int j=0;j<ids.size();j++){
                            Point3D[] thisBlock = blocks.get(j);
                            Point3D[] conflict = CollisionDecider.getBlocksConflict(thisBlock, block);
                            if(conflict!=null){
                                // 输出小圈冲突
                                System.out.println("S-S,"+uavId+","+ids.get(j)+","+conflict[0]+","+conflict[1]);
                            }
                        }
                    }
                }else{
                    propsP=new HashMap<>();
                    List<String> ids = new ArrayList<>();
                    List<Point3D[]> blocks = new ArrayList<>();
                    ids.add(uavId);
                    blocks.add(block);
                    propsP.put("ids",ids);
                    propsP.put("blocks",blocks);
                    propsP.put("count",1);
                    p.setProperties(propsP);
                }
            }

            int octreeNumber = tau.getOctreeNumber(i, M);
            OctreeNode q = p.getKthChild(octreeNumber);
            if(q==null) {
                q=new OctreeNode();
                p.setKthChild(octreeNumber,q);
            }
            p=q;
        }
        Map<String, Object> propsP = p.getProperties();
        List<String> ids = new ArrayList<>();
        List<Point3D[]> blocks = new ArrayList<>();
        ids.add(uavId);
        blocks.add(block);
        if(propsP!=null){
            Object o = propsP.get("ids");
            if(o==null) {
                propsP.put("ids",ids);
                propsP.put("blocks",blocks);
                propsP.put("count",1);
            }else{
                ids=(List<String>) propsP.get("ids");
                ids.add(uavId);
                blocks=(List<Point3D[]>) propsP.get("blocks");
                blocks.add(block);
                propsP.put("count",(int)propsP.get("count")+1);
            }
        }else{
            propsP=new HashMap<>();
            propsP.put("ids",ids);
            propsP.put("blocks",blocks);
            propsP.put("count",1);
        }
        p.setOccupied(true);
        p.setProperties(propsP);
        System.out.println(uavId);
        System.out.println(p.getProperties());
    }

    public OctreeNode searchGrid(OctreeGrid grid){
        OctreeNode p=root;
        for(int i=1;i<=grid.getK();i++){
            if(p==null) return null;
            int octreeNumber = grid.getOctreeNumber(i, M);
            OctreeNode q = p.getKthChild(octreeNumber);
            p=q;
        }
        return p;
    }

    // 找到可能最近的K个Uav，自身除外。需要后续再通过距离筛查
    public List<String> findNearestKUav(Point3D[] block,int K){

        List<String> result = new ArrayList<>();

        OctreeGrid tau = OctreeGrid.tau(block,M);
        int count=0;
        // 1. 先找同块内
        OctreeNode node1=searchGrid(tau);
        Map<String, Object> props1 = node1.getProperties();
        Object countObj = props1.get("count");
        if(countObj!=null){
            count+=(int)countObj;
            if(count>K){
                // 如果能在同块里找到至少K个
                // 则把该块中的所有uavId输出
                result.addAll(getAllUavIdInASubTree(node1));
                result = new ArrayList<>(new HashSet<>(result));
                return result;
            }
        }

        // 2. 找邻居块
        OctreeGrid[] neighbors = tau.getNeighbors(M);
        for(OctreeGrid neighbor:neighbors){
            OctreeNode node2 = searchGrid(neighbor);
            if(node2==null) continue;
            Map<String, Object> props2 = node2.getProperties();
            if(props2==null) continue;
            Object countObj2 = props2.get("count");
            if(countObj2!=null){
                count+=(int)countObj2;
                result.addAll(getAllUavIdInASubTree(node2));
                result = new ArrayList<>(new HashSet<>(result));
                if(count>K){
                    return result;
                }
            }
        }

        // 3. 找父块
        OctreeNode p = root;
        LinkedList<OctreeNode> Q= new LinkedList<>();
        Q.addFirst(p);
        for(int i=1;i<= tau.getK();i++){
            int octreeNumber = tau.getOctreeNumber(i, M);
            OctreeNode q = p.getKthChild(octreeNumber);
            Q.addFirst(q);
            p=q;
        }
        while(!Q.isEmpty()){
            OctreeNode q = Q.removeFirst();
            if(q==null) continue;
            Map<String, Object> props3 = q.getProperties();
            if(props3==null) continue;
            Object countObj3 = props3.get("count");
            if(countObj3!=null){
                count=(int)countObj3;
                if(count>K){
                    // 如果能在同块里找到至少K个
                    // 则把该块中的所有uavId输出
                    result.addAll(getAllUavIdInASubTree(node1));
                    result = new ArrayList<>(new HashSet<>(result));
                    return result;
                }
            }
        }
        return result;
    }

    private List<String> getAllUavIdInASubTree(OctreeNode root){
        List<String> result = new ArrayList<>();
        LinkedList<OctreeNode> Q=new LinkedList<>();
        Q.add(root);
        while(!Q.isEmpty()){
            OctreeNode q = Q.removeFirst();
            Map<String, Object> props = q.getProperties();
            Object o = props.get("ids");
            if(o!=null){
                result.addAll((List<String>)o);
            }
            for(int i=0;i<8;i++){
                OctreeNode child = q.getKthChild(i);
                if(child!=null) Q.addLast(child);
            }
        }
        return result;
    }
}
