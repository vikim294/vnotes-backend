package com.vikim.demo.oauthAccount;

import com.vikim.demo.user.CreateUserForOAuthAccountParam;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

@Mapper
public interface OauthAccountMapper {

    @Select("""
            select *
            from oauth_accounts
            where provider_user_id = #{sub} and provider = 'GOOGLE'
            """)
    Optional<OauthAccount> findByGoogleSub(String sub);

    @Insert("""
            insert into oauth_accounts(user_id, provider, provider_user_id)
            values(#{userId}, #{provider}, #{providerUserId})
            """)
    int createGoogleOAuthAccount(@Param("userId") Long userId, @Param("provider") OauthProvider provider, @Param("providerUserId") String providerUserId);
}
