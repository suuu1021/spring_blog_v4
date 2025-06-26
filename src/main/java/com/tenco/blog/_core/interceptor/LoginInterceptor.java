package com.tenco.blog._core.interceptor;

import com.tenco.blog._core.errors.exception.Exception401;
import com.tenco.blog.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component // IoC 대상 (싱글톤 패턴으로 관리)
public class LoginInterceptor implements HandlerInterceptor {

    /**
     * preHandle - 컨트롤러에 들어 가기 전에 동작하는 메서드 이다.
     * return 타입이 boolean 이라서 true --> 컨트롤러 안으로 들어간다.
     *                           / false --> 컨트롤러 안으로 못 들어간다.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        HttpSession session = request.getSession();
        System.out.println("인터셉터 동작 확인 : " + request.getRequestURL());
        User sessionUser = (User) session.getAttribute("sessionUser");
        if (sessionUser == null) {
            throw new Exception401("로그인이 필요한 서비스 입니다.");
            // return false;
        }
        return true;
    }

    // 뷰가 렌더링 되기 전에 콜백 되는 메서드
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    // 뷰가 완전 렌더링 된 후 호출 될 수 있다.
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
