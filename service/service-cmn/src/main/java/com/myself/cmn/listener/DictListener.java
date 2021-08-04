package com.myself.cmn.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.myself.cmn.mapper.DictMapper;
import com.myself.model.cmn.Dict;
import com.myself.vo.cmn.DictEeVo;
import org.springframework.beans.BeanUtils;

/**
 * 导入数据监听器
 *
 * @author Wei
 * @since 2021/6/21
 */
public class DictListener extends AnalysisEventListener<DictEeVo> {

    private DictMapper dictMapper;

    public DictListener(DictMapper dictMapper) {
        this.dictMapper = dictMapper;
    }

    //每解析一行数据调用的方法
    @Override
    public void invoke(DictEeVo dictEeVo, AnalysisContext analysisContext) {
        Dict dict = new Dict();
        dict.setIsDeleted(0);
        BeanUtils.copyProperties(dictEeVo, dict);
        dictMapper.insert(dict);
    }

    //解析所有数据后调用的方法
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
