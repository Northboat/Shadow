package com.northboat.shadow.mapper;


import com.northboat.shadow.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import java.util.List;

@Mapper
@Repository
public interface UserMapper {
    public List<User> queryAll();

    public void add(User user);
    public User queryByName(String name);
    public User queryByEmail(String email);
    public void login(String name);
    public void logoff(String name);
}
