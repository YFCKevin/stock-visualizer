package com.gurula.stockMate.member;

public class MemberContext {
    private static final ThreadLocal<Member> tl = new ThreadLocal<>();

    public static void setMember(Member member) {
        tl.set(member);
    }

    public static Member getMember(){
        return tl.get();
    }

    public static void removeMember (){
        tl.remove();
    }
}
