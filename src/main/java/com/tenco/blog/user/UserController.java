package com.tenco.blog.user;

import com.tenco.blog._core.errors.exception.Exception400;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@RequiredArgsConstructor
@Controller
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserRepository userRepository;
    private final HttpSession httpSession;

    @GetMapping("/user/update-form")
    public String updateForm(HttpServletRequest request, HttpSession session) {

        log.info("회원 정보 수정 폼 요청");

        User sessionUser = (User) session.getAttribute("sessionUser");
        request.setAttribute("user", sessionUser);
        return "user/update-form";
    }


    @PostMapping("/user/update")
    public String update(UserRequest.UpdateDTO reqDTO,
                         HttpSession session, HttpServletRequest request) {

        log.info("회원 정보 수정 요청");

        User sessionUser = (User) session.getAttribute("sessionUser");
        reqDTO.validate();
        User updateUser = userRepository.updateById(sessionUser.getId(), reqDTO);
        session.setAttribute("sessionUser", updateUser);
        return "redirect:/user/update-form";
    }


    @GetMapping("/join-form")
    public String joinForm() {

        log.info("회원 가입 폼 요청");

        return "user/join-form";
    }


    @PostMapping("/join")
    public String join(UserRequest.JoinDTO joinDTO, HttpServletRequest request) {

        log.info("회원 가입 기능 요청");
        log.info("사용자 명: {} ", joinDTO.getUsername());
        log.info("사용자 이메일: {} ", joinDTO.getEmail());

        joinDTO.validate();

        User exsistUser = userRepository.findByUsername(joinDTO.getUsername());
        if (exsistUser != null) {
            throw new Exception400("이미 존재하는 사용자명 입니다" + joinDTO.getUsername());
        }
        User user = joinDTO.toEntity();
        userRepository.save(user);
        return "redirect:/login-form";
    }


    @GetMapping("/login-form")
    public String loginForm() {

        log.info("로그인 폼 요청");

        return "user/login-form";
    }


    @PostMapping("/login")
    public String login(UserRequest.LoginDTO loginDTO) {

        log.info("로그인 기능 요청");
        log.info("사용자 명 : {} ", loginDTO.getUsername());

        loginDTO.validate();

        User user = userRepository.findByUsernameAndPassword(loginDTO.getUsername(), loginDTO.getPassword());
        if (user == null) {
            throw new Exception400("사용자 명 또는 비밀번호가 일치하지 않습니다.");
        }
        httpSession.setAttribute("sessionUser", user);
        return "redirect:/";
    }


    @GetMapping("/logout")
    public String logout() {

        log.info("로그아웃");

        httpSession.invalidate();
        return "redirect:/";
    }

}
