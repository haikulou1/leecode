package com.demo.helloworld.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 关于信息返回 VO
 * 对应接口：W02 GET /api/about
 *
 * @author DTCoder
 * @date 2026-07-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AboutVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 关于标题 */
    private String title;

    /** 关于正文内容 */
    private String content;

    /** 作者 */
    private String author;
}
