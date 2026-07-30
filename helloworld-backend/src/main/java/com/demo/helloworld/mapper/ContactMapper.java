package com.demo.helloworld.mapper;

import com.demo.helloworld.entity.ContactRecord;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

/**
 * 联系记录 Mapper 接口
 *
 * @author DTCoder
 * @date 2026-07-30
 */
public interface ContactMapper {

    /**
     * 插入联系记录，返回自增主键
     *
     * @param record 联系记录实体
     * @return 影响行数
     */
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ContactRecord record);

    /**
     * 探测数据库连通性
     *
     * @return 探测结果
     */
    int checkConnection(@Param("testValue") Integer testValue);
}
