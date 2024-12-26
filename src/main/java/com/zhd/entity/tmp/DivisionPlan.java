package com.zhd.entity.tmp;

import com.zhd.geometry.structure.OctreeGrid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DivisionPlan {
    private Long uavId;
    private List<OctreeGrid> smallArea;
    private List<OctreeGrid> largeArea;
}
