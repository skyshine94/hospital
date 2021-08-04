package com.myself.cmn.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.myself.cmn.listener.DictListener;
import com.myself.cmn.mapper.DictMapper;
import com.myself.cmn.service.DictService;
import com.myself.model.cmn.Dict;
import com.myself.vo.cmn.DictEeVo;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

    //根据id查询子节点数据列表
    @Override
    @Cacheable(value = "dict", keyGenerator = "keyGenerator")
    public List<Dict> findChildData(Long id) {
        List<Dict> dictList = baseMapper.selectList(new QueryWrapper<Dict>().eq("parent_id", id));
        //处理hasChildren字段
        for (Dict dict : dictList) {
            boolean isChild = isChild(dict.getId());
            dict.setHasChildren(isChild);
        }
        return dictList;
    }

    //导出数据字典
    @Override
    public void exportData(HttpServletResponse response) {
        //设置响应头信息
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        String fileName = "dict";
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
        //获取所有数据字典信息
        List<Dict> dictList = baseMapper.selectList(null);
        //将dictList转换成dictEeVoList
        List<DictEeVo> dictEeVoList = new ArrayList<>();
        for (Dict dict : dictList) {
            DictEeVo dictEeVo = new DictEeVo();
            BeanUtils.copyProperties(dict, dictEeVo); //相当于对dict执行get，并将结果set到dictEeVo中
            dictEeVoList.add(dictEeVo);
        }
        //执行写操作
        ServletOutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            EasyExcel.write(outputStream, DictEeVo.class).sheet(fileName).doWrite(dictEeVoList);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != outputStream) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //导入数据字典
    @Override
    @CacheEvict(value = "dict", allEntries = true)
    public void importData(MultipartFile file) {
        InputStream inputStream = null;
        //执行读操作
        try {
            inputStream = file.getInputStream();
            EasyExcel.read(inputStream, DictEeVo.class, new DictListener(baseMapper)).sheet().doRead();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //判断id下是否有子节点
    private boolean isChild(Long id) {
        Integer count = baseMapper.selectCount(new QueryWrapper<Dict>().eq("parent_id", id));
        return count > 0;
    }

    //根据dictcode和value名称获取name
    @Override
    public String getDictName(String dictCode, String value) {
        //判断dictCode是否为空
        if (StringUtils.isEmpty(dictCode)) {
            Dict dict = baseMapper.selectOne(new QueryWrapper<Dict>().eq("value", value));
            return dict.getName();
        } else {
            Dict codeDict = baseMapper.selectOne(new QueryWrapper<Dict>().eq("dict_code", dictCode));
            Dict dict = baseMapper.selectOne(new QueryWrapper<Dict>().eq("parent_id", codeDict.getId()).eq("value", value));
            return dict.getName();
        }
    }

    //根据dictcode获取下级节点
    @Override
    public List<Dict> findByDictCode(String dictCode) {
        Dict dict = baseMapper.selectOne(new QueryWrapper<Dict>().eq("dict_code", dictCode));
        //根据id查询子节点数据列表
        List<Dict> list = findChildData(dict.getId());
        return list;
    }

}
