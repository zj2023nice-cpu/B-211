package com.grade.system.context;

import com.grade.system.dto.LoginUserInfo;

public class UserContext {
    private static final ThreadLocal<LoginUserInfo> USER_HOLDER = new ThreadLocal<>();

    public static void setUser(LoginUserInfo user) {
        USER_HOLDER.set(user);
    }

    public static LoginUserInfo getUser() {
        return USER_HOLDER.get();
    }

    public static Long getUserId() {
        LoginUserInfo user = USER_HOLDER.get();
        return user != null ? user.getId() : null;
    }

    public static String getUsername() {
        LoginUserInfo user = USER_HOLDER.get();
        return user != null ? user.getUsername() : null;
    }

    public static String getUserRole() {
        LoginUserInfo user = USER_HOLDER.get();
        return user != null ? user.getRole() : null;
    }

    public static boolean isLoggedIn() {
        return USER_HOLDER.get() != null;
    }

    public static void clear() {
        USER_HOLDER.remove();
    }
}
