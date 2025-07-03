package com.gurula.stockMate.oauth;

import com.gurula.stockMate.member.Member;
import com.gurula.stockMate.member.MemberService;
import com.gurula.stockMate.member.Provider;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final MemberService memberService;

    public UserService(MemberService memberService) {
        this.memberService = memberService;
    }

    public Member processOAuthPostLogin(String email, String name, String oauth2ClientName) {

        //取得系統上是不是有這個帳號
        Optional<Member> opt = memberService.findByEmail(email);
        Member member = new Member();

        //取得是GOOGLE或FACEBOOK登入
        Provider authType = Provider.valueOf(oauth2ClientName.toUpperCase());
        System.out.println("authType==>" + authType);

        if (opt.isEmpty()) { //如果沒有註冊過就新增
            member.setName(name);
            member.setEmail(email);
            member.setCreateAt(System.currentTimeMillis());
            member.setProvider(authType);
            member.setRole(Role.USER);
            memberService.save(member);
            System.out.println("尚未註冊");
        } else {
            member = opt.get();
        }
        return member;
    }

}
