package com.zhd.geometry.algorithm;

import com.zhd.entity.UavSpaceOcp;
import com.zhd.entity.tmp.ConflictResult;
import com.zhd.entity.tmp.DivisionPlan;
import com.zhd.geometry.structure.Octree;
import com.zhd.geometry.structure.OctreeGrid;
import com.zhd.mapper.UavSpaceOcpMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CollisionDecider {

    private final static String KEY_UAV_ID="uavId";
    private final static String KEY_SMALL_OCP="smallOcp";
    private final static String KEY_LARGE_OCP_LIST="largeOcpList";

    @Autowired
    private UavSpaceOcpMapper uavSpaceOcpMapper;

    /**
     * 第一种划分方式   java内部解决
     * @param orgPlans
     * @param M
     * @return
     */
    public List<DivisionPlan> divide1(List<DivisionPlan> orgPlans,int M){
        Octree octree = new Octree(M);
        List<DivisionPlan> result = new ArrayList<>();

        // 插入Small Area
        for(DivisionPlan orgPlan: orgPlans){
            List<OctreeGrid> smallArea = orgPlan.getSmallArea();
            Map<String,Object> props = new HashMap<>();
            props.put(KEY_UAV_ID,orgPlan.getUavId());
            props.put(KEY_SMALL_OCP,true);
            props.put(KEY_LARGE_OCP_LIST,null);
            for(OctreeGrid grid:smallArea){
                Map<String, Object> props_ = octree.getWithProperties(grid);
                if(props_!=null) {
                    System.out.println("Small Area Collides:"+orgPlan.getUavId()+"-"+props_.get(KEY_UAV_ID)+","+grid);
                }
                octree.insertWithProperties(grid,props);
            }
        }


        // 插入Large Area
        // 不考虑优先级，冲突区域归属于第一个遍历到的uav
        for(DivisionPlan orgPlan:orgPlans){
            List<OctreeGrid> largeArea = orgPlan.getLargeArea();
            List<OctreeGrid> newLargeArea = new ArrayList<>();
            for(OctreeGrid grid:largeArea){
                Map<String, Object> props_ = octree.getWithProperties(grid);

                if(props_==null){
                    Map<String,Object> props = new HashMap<>();
                    props.put(KEY_UAV_ID,orgPlan.getUavId());
                    props.put(KEY_SMALL_OCP,false);
                    List<Long> largeOcp = new ArrayList<>();
                    largeOcp.add(orgPlan.getUavId());
                    props.put(KEY_LARGE_OCP_LIST,largeArea);
                    octree.insertWithProperties(grid,props);
                    newLargeArea.add(grid);
                    continue;
                }
                if(props_.get(KEY_UAV_ID)==orgPlan.getUavId()) continue;
                Map<String, Object> props = octree.getWithProperties(grid);
                List<Long> largeOcpList = (List<Long>)props.get(KEY_LARGE_OCP_LIST);
                largeOcpList.add(orgPlan.getUavId());
                System.out.println("Large Area Collides:"+props_.get(KEY_UAV_ID)+"-"+orgPlan.getUavId()+","+grid);
            }
            result.add(new DivisionPlan(orgPlan.getUavId(),orgPlan.getSmallArea(),newLargeArea));
        }

        return result;
    }

    /**
     * 第二种分法，SQL
     * @param orgPlans
     * @param M
     * @return
     */
    public List<DivisionPlan> divide2(List<DivisionPlan> orgPlans,int M){

        uavSpaceOcpMapper.delete(null);

        for(DivisionPlan orgPlan:orgPlans) {
            Long uavId = orgPlan.getUavId();
            List<OctreeGrid> largeArea = orgPlan.getLargeArea();
            for (OctreeGrid grid : largeArea) {
                OctreeGrid reverseGrid = grid.reverseBits();
                uavSpaceOcpMapper.insert(new UavSpaceOcp(
                        uavId,
                        reverseGrid.getX(),
                        reverseGrid.getY(),
                        reverseGrid.getZ(),
                        reverseGrid.getK()
                ));
            }
        }

        List<ConflictResult> conflictResults = uavSpaceOcpMapper.calculateConflict();


        return null;
    }
}
