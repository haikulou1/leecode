package cn.wy.template.dao;

import cn.wy.template.JdbcTemplate;
import cn.wy.template.RowMapper;
import cn.wy.template.entity.Member;

import java.sql.ResultSet;
import java.util.List;


public class MemberDao {

    //为什么不继承，主要是为了解耦
    private cn.wy.template.JdbcTemplate JdbcTemplate = new JdbcTemplate(null);

    public List<?> query(){
        String sql = "select * from t_member";
        return JdbcTemplate.executeQuery(sql,new RowMapper<Member>(){
            @Override
            public Member mapRow(ResultSet rs, int rowNum) throws Exception {
                Member member = new Member();
                member.setUsername(rs.getString("username"));
                member.setPassword(rs.getString("password"));
                member.setAge(rs.getInt("age"));
                member.setAddr(rs.getString("addr"));
                return member;
            }
        },null);
    }


}
