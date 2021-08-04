package com.myself.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.myself.model.order.OrderInfo;
import com.myself.vo.order.OrderCountQueryVo;
import com.myself.vo.order.OrderCountVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderMapper extends BaseMapper<OrderInfo> {

    //查询预约统计数据
    List<OrderCountVo> selectOrderCount(@Param("vo") OrderCountQueryVo orderCountQueryVo);
}
