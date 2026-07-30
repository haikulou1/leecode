package com.demo.helloworld.mapper;

import com.demo.helloworld.entity.HelloMessage;
import org.apache.ibatis.annotations.Param;

/**
 * 欢迎语 Mapper 接口
 *
 * @author DTCoder
 * @date 2026-07-30
 */
public interface HelloMapper {

    /**
     * 按语言查询未删除的欢迎语记录
     *
     * @param language  语言标识
     * @param isDeleted 是否删除：0-未删除
     * @return 欢迎语实体，无匹配返回 null
     */
    HelloMessage selectByLanguage(@Param("language") String language, @Param("isDeleted") Integer isDeleted);
}
